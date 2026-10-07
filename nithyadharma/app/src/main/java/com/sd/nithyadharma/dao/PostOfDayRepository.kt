package com.sd.nithyadharma.dao

import android.util.Log
import androidx.core.text.HtmlCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class PostOfDay(
    val title: String,
    val slug: String,
    val date: String? = null,
    val author: String? = null,
    val excerpt: String? = null,
    val tags: List<String> = emptyList(),
    val contentsMarkdown: String,
    val imageUrl: String? = null,
    val imageBytes: ByteArray?
)

class PostOfDayRepository() {

    private val baseUrl = "https://www.templepages.com"

    /**
     * Fetches posts.json, finds the post matching [slug], populates all available
     * metadata fields from the JSON object, downloads contents.md, updates the StateFlow,
     * and returns the PostOfDay object.
     */
    suspend fun getPostBySlug(slug: String): PostOfDay? = withContext(Dispatchers.IO) {
        Log.i("PostOfDayRepository", "getPostBySlug entry to find post of the day")

        try {
            val jsonUrl = "$baseUrl/posts.json"
            val jsonText = URL(jsonUrl).readText()
            val postsArray = JSONArray(jsonText)

            var matchedObject: JSONObject? = null

            // Search for matching slug in posts.json
            for (i in 0 until postsArray.length()) {
                val item = postsArray.getJSONObject(i)
                val itemSlug = item.optString("slug", "")
                if (itemSlug.equals(slug, ignoreCase = true)) {
                    matchedObject = item
                    break
                }
            }

            // Extract all post metadata from matched JSON object
            val finalSlug = matchedObject?.optString("slug", slug) ?: slug
            val title = matchedObject?.optString("title", "Temple Page") ?: "Temple Page"
            val date = matchedObject?.optString("date", null)
            val author = matchedObject?.optString("author", null)
            val excerpt = matchedObject?.optString("excerpt", null)

            // Parse tags list if present
            val tagsList = mutableListOf<String>()
            matchedObject?.optJSONArray("tags")?.let { tagsArray ->
                for (j in 0 until tagsArray.length()) {
                    tagsList.add(tagsArray.getString(j))
                }
            }

            // Download markdown contents for the specific slug
            val markdownUrl = "$baseUrl/posts/$finalSlug/contents.md"
            val rawMarkdown = runCatching { URL(markdownUrl).readText() }.getOrDefault("")
            val markdownContent = rawMarkdown.cleanAndNormalizeMarkdown()

            // Image URL construction
            val imageUrl = "$baseUrl/posts/$finalSlug/img1.jpg"
            val imageBytes = downloadBytesOrNull(imageUrl)

            val post = PostOfDay(
                title = title,
                slug = finalSlug,
                date = date,
                author = author,
                excerpt = excerpt,
                tags = tagsList,
                contentsMarkdown = markdownContent,
                imageUrl = imageUrl,
                imageBytes = imageBytes
            )

            Log.i("PostOfDayRepository", "post of the day found ${post.slug} in templepages.com, yay!!")
            return@withContext post
        }
        catch (e: Exception) {
            e.printStackTrace()
            Log.e("PostOfDayRepository", "error while trying to find post of the day", e)
            return@withContext null
        }
    }

    private fun downloadBytesOrNull(
        urlString: String
    ): ByteArray? {
        return try {
            val connection = URL(urlString).openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 10_000
                connection.readTimeout = 15_000

                if (connection.responseCode !in 200..299) {
                    return null
                }
                connection.inputStream.use { it.readBytes() }
            }
            finally {
                connection.disconnect()
            }
        }
        catch (_: Exception) {
            null
        }
    } // downloadBytesOrNull ends


    /**
     * Cleans HTML markup into structured Markdown notation, converts HTML links (http/https) to Markdown,
     * preserves literal newlines, and resolves HTML entities (&nbsp;, &amp;, &#39;, &quot;, etc.).
     */
    fun String.cleanAndNormalizeMarkdown(): String {
        if (this.isEmpty()) return ""

        return this
            // 1. Convert Line & Paragraph Breaks to literal newlines (\n)
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)<p[^>]*>"), "")
            .replace(Regex("(?i)</p>"), "\n\n")

            // 2. Convert HTML anchor tags <a href="http(s)://...">text</a> to Markdown links [text](url)
            .replace(Regex("(?i)<a\\s+[^>]*href=[\"']([^\"']*)[\"'][^>]*>(.*?)</a>"), "[$2]($1)")

            // 3. Convert Headings (H1 to H6 -> Markdown #)
            .replace(Regex("(?i)<h1[^>]*>(.*?)</h1>"), "\n# $1\n\n")
            .replace(Regex("(?i)<h2[^>]*>(.*?)</h2>"), "\n## $1\n\n")
            .replace(Regex("(?i)<h3[^>]*>(.*?)</h3>"), "\n### $1\n\n")
            .replace(Regex("(?i)<h4[^>]*>(.*?)</h4>"), "\n#### $1\n\n")

            // 4. Convert Bold and Italic tags to Markdown delimiters BEFORE removing tags
            .replace(Regex("(?i)<(b|strong)[^>]*>(.*?)</\\1>"), "**$2**")
            .replace(Regex("(?i)<(i|em)[^>]*>(.*?)</\\1>"), "*$2*")

            // 5. Strip any remaining unsupported/unknown HTML tags
            .replace(Regex("<[^>]*>"), "")

            // 6. Protect literal newlines before HtmlCompat.fromHtml() so it doesn't swallow them
            .replace("\n", "___NEWLINE_TOKEN___")

            // 7. Decode all HTML entities (&nbsp;, &amp;, &#39;, &quot;, &#xxx;, etc.)
            .let { raw ->
                HtmlCompat.fromHtml(raw, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
            }

            // 8. Restore preserved newlines
            .replace("___NEWLINE_TOKEN___", "\n")

            // 9. Normalize non-breaking space characters (\u00A0) produced by &nbsp;
            .replace('\u00A0', ' ')

            // 10. Cap excessive consecutive blank lines at 2 max
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }
}