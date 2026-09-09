package com.sd.nithyadharma.dao

import android.util.Log
import androidx.core.text.HtmlCompat
import com.sd.nithyadharma.util.CommonFunctions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object PostOfDayRepository {

    private const val WEBSITE = "https://templepages.com/"

    private const val BASE_URL = WEBSITE + "posts/"

    private const val POST_JSON_FILE = WEBSITE + "posts.json"

    private val json = Json {
        ignoreUnknownKeys = true
    }

    // --------------------------------------------------------
    // JSON structure
    // --------------------------------------------------------

    @Serializable
    data class PostIndexEntry(
        val slug: String,
        val type: String,
        val title: String,
        val author: String,
        val date: String,
        val tags: List<String> = emptyList(),
        val hide: Boolean? = false // Handles missing key, null, or boolean values
    )

    // --------------------------------------------------------
    // Result used by the UI
    // --------------------------------------------------------

    data class PostOfDay(
        val slug: String,
        val type: String,
        val title: String,
        val author: String,
        val date: LocalDate,
        val tags: List<String>,
        val contentsMarkdown: String,
        val imageBytes: ByteArray?,
        val isHidden: Boolean = false // Default to false if omitted
    )

    private val _postOfDay = MutableStateFlow<PostOfDay?>(null)
    val postOfDay: StateFlow<PostOfDay?> = _postOfDay.asStateFlow()

    private var loadedDate: LocalDate? = null

    suspend fun refresh() {
        Log.i("PostOfDayRepository", "posts json ${POST_JSON_FILE}, baseurl ${BASE_URL}")

        val today = CommonFunctions.getCurrentDate()

        // Skip re-fetching if we've ALREADY checked for today (regardless of null or post)
        // Or keep it strict: only skip if we successfully loaded a post for today
        if (loadedDate == today && _postOfDay.value != null) {
            Log.i("PostOfDayRepository", "refresh skipped: already fetched valid post for today")
            return
        }

        Log.i("PostOfDayRepository", "refresh proceeding to fetch post")
        val post = getPostOfDay(today)
//        Log.i("PostOfDayRepository", "after post obtained: $post")

        _postOfDay.value = post

        // Mark today as checked so we don't spam network calls on this calendar date
        loadedDate = today
    }

    // --------------------------------------------------------
    // Main function
    // --------------------------------------------------------

    suspend fun getPostOfDay(
        today: LocalDate = LocalDate.now()
    ): PostOfDay? = withContext(Dispatchers.IO) {
    Log.i("PostOfDayRepository", "getPostOfDay entry")

        return@withContext try {
            // --------------------------------------------
            // 1. Download posts.json
            // --------------------------------------------

            val indexJson = downloadText(POST_JSON_FILE)
            Log.i("PostOfDayRepository", "getPostOfDay after downloadtext")

            val posts = json.decodeFromString<List<PostIndexEntry>>(indexJson)

            // --------------------------------------------
            // 2. Find today's post
            // --------------------------------------------

            val unpaddedFormatter = DateTimeFormatter.ofPattern("yyyy-M-d")
//            val targetDate = LocalDate.of(2024,4, 11) // Or any date: LocalDate.parse("2026-08-24")
            val targetDate = today // Or any date: LocalDate.parse("2026-08-24")

            val post = posts.firstOrNull { item ->
                runCatching {
                    LocalDate.parse(item.date, unpaddedFormatter) == targetDate
                }.getOrDefault(false)
            } ?: return@withContext null

            Log.i("PostOfDayRepository", "post of the day found ${post.date} in templepages.com, yay!!")

            // --------------------------------------------
            // 3. Build URLs
            // --------------------------------------------
            Log.i("PostOfDayRepository", "framin urls")

            val postBaseUrl = BASE_URL + post.slug + "/"

            val contentsUrl = postBaseUrl + "contents.md"

            val imageUrl = postBaseUrl + "img1.jpg"

            Log.i("PostOfDayRepository", "framin urls contentsUrl ${contentsUrl}, imageUrl ${imageUrl}")

            // --------------------------------------------
            // 4. Download contents
            // --------------------------------------------

            val contents = downloadText(contentsUrl)

            // lets strip the unnecessary tags
            val formattedSpannableContent = HtmlCompat.fromHtml(
                contents,
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            // --------------------------------------------
            // 5. Download image
            //
            // Image is optional. If it doesn't exist,
            // the post can still be displayed.
            // --------------------------------------------

            val imageBytes = downloadBytesOrNull(imageUrl)

            // --------------------------------------------
            // 6. Return complete post
            // --------------------------------------------

            PostOfDay(
                slug = post.slug,
                type = post.type,
                title = post.title,
                author = post.author,
                date = today,
                tags = post.tags,
                contentsMarkdown = formattedSpannableContent.toString(),
                imageBytes = imageBytes
            )

        } catch (e: Exception) {

            // No post / network problem / bad JSON /
            // missing contents etc.
            //
            // We deliberately don't let this disturb
            // the rest of the application.
            Log.e("PostOfDayRepository", "error while trying to find post of the day", e)

            null
        }
    }

    // ========================================================
    // HTTP helpers
    // ========================================================

    private fun downloadText(
        urlString: String
    ): String {

        val connection = URL(urlString).openConnection() as HttpURLConnection

        try {

            connection.requestMethod = "GET"
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000

            connection.setRequestProperty(
                "Accept",
                "text/plain, application/json"
            )

            val responseCode = connection.responseCode

            if (responseCode !in 200..299) {
                throw Exception(
                    "HTTP $responseCode: $urlString"
                )
            }

            return connection
                .inputStream
                .bufferedReader()
                .use { it.readText() }

        } finally {
            connection.disconnect()
        }
    }

    private fun downloadBytesOrNull(
        urlString: String
    ): ByteArray? {

        return try {

            val connection =
                URL(urlString).openConnection()
                        as HttpURLConnection

            try {

                connection.requestMethod = "GET"

                connection.connectTimeout = 10_000

                connection.readTimeout = 15_000

                if (connection.responseCode !in 200..299) {
                    return null
                }

                connection.inputStream
                    .use { it.readBytes() }

            } finally {

                connection.disconnect()
            }

        } catch (_: Exception) {

            null
        }
    }
}