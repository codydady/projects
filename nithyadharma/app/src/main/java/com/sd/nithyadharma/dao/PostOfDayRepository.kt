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
        Log.i("PostOfDayRepository", "getPostBySlug entry to find post of the day for slug: $slug")

        try {
            // Append timestamp query parameter to defeat CDN / HTTP cache for posts.json[cite: 3]
            val jsonUrl = "$baseUrl/posts.json?t=${System.currentTimeMillis()}"
            val jsonText = fetchTextFresh(jsonUrl)

            if (jsonText.isNullOrBlank()) {
                Log.e("PostOfDayRepository", "Failed to retrieve fresh posts.json")
                return@withContext null
            }

            val postsArray = JSONArray(jsonText)
            var matchedObject: JSONObject? = null

            // Search for matching slug in posts.json[cite: 3]
            for (i in 0 until postsArray.length()) {
                val item = postsArray.getJSONObject(i)
                val itemSlug = item.optString("slug", "")
                if (itemSlug.equals(slug, ignoreCase = true)) {
                    matchedObject = item
                    break
                }
            }

            if (matchedObject == null) {
                Log.e("PostOfDayRepository", "Slug '$slug' not found in freshly fetched posts.json")
                return@withContext null
            }

            // Extract all post metadata from matched JSON object[cite: 3]
            val finalSlug = matchedObject.optString("slug", slug)
            val title = matchedObject.optString("title", "Temple Page")
            val date = matchedObject.optString("date", null)
            val author = matchedObject.optString("author", null)
            val excerpt = matchedObject.optString("excerpt", null)

            // Parse tags list if present[cite: 3]
            val tagsList = mutableListOf<String>()
            matchedObject.optJSONArray("tags")?.let { tagsArray ->
                for (j in 0 until tagsArray.length()) {
                    tagsList.add(tagsArray.getString(j))
                }
            }

            // Download markdown contents for the specific slug (bypassing cache)[cite: 3]
            val markdownUrl = "$baseUrl/posts/$finalSlug/contents.md?t=${System.currentTimeMillis()}"
            val rawMarkdown = fetchTextFresh(markdownUrl)

            if (rawMarkdown.isNullOrBlank()) {
                Log.e(
                    "PostOfDayRepository",
                    "Post content unavailable for slug: $finalSlug"
                )
                return@withContext null
            }

            val markdownContent = rawMarkdown.cleanAndNormalizeMarkdown()

            // Image is optional. Download bytes bypassing HTTP cache.[cite: 3]
            val imageUrl = "$baseUrl/posts/$finalSlug/img1.jpg?t=${System.currentTimeMillis()}"
            val imageBytes = downloadBytesOrNull(imageUrl)

            if (imageBytes == null) {
                Log.w(
                    "PostOfDayRepository",
                    "Image unavailable for slug: $finalSlug"
                )
            }

            val post = PostOfDay(
                title = title,
                slug = finalSlug,
                date = date,
                author = author,
                excerpt = excerpt,
                tags = tagsList,
                contentsMarkdown = markdownContent,
                imageUrl = "$baseUrl/posts/$finalSlug/img1.jpg",
                imageBytes = imageBytes
            )

            Log.i("PostOfDayRepository", "post of the day found ${post.slug} in templepages.com, yay!!")
            return@withContext post
        }
        catch (e: Exception) {
            Log.e("PostOfDayRepository", "error while trying to find post of the day", e)
            return@withContext null
        }
    }

    /**
     * Executes a fresh HTTP GET request explicitly bypassing all internal, proxy, and CDN caches.
     */
    private fun fetchTextFresh(urlString: String): String? {
        return try {
            val connection = URL(urlString).openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 10_000
                connection.readTimeout = 15_000
                connection.useCaches = false
                connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate")
                connection.setRequestProperty("Pragma", "no-cache")
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android)")

                if (connection.responseCode !in 200..299) {
                    Log.e("PostOfDayRepository", "HTTP ${connection.responseCode} error fetching: $urlString")
                    return null
                }
                connection.inputStream.bufferedReader().use { it.readText() }
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            Log.e("PostOfDayRepository", "Network error fetching text from: $urlString", e)
            null
        }
    }

    private fun downloadBytesOrNull(urlString: String): ByteArray? {
        return try {
            val connection = URL(urlString).openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 10_000
                connection.readTimeout = 15_000
                connection.useCaches = false
                connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate")
                connection.setRequestProperty("Pragma", "no-cache")

                if (connection.responseCode !in 200..299) {
                    return null
                }
                connection.inputStream.use { it.readBytes() }
            } finally {
                connection.disconnect()
            }
        } catch (_: Exception) {
            Log.i("PostOfDayRepository", "downloadBytesOrNull, error while trying to fetch image bytes")
            null
        }
    }

    /**
     * Cleans HTML markup into structured Markdown notation, converts HTML links (http/https) to Markdown,
     * preserves literal newlines, and resolves HTML entities (&nbsp;, &amp;, &#39;, &quot;, etc.).[cite: 3]
     */
    fun String.cleanAndNormalizeMarkdown(): String {
        if (this.isEmpty()) return ""

        return this
            // 1. Convert Line & Paragraph Breaks to literal newlines (\n)[cite: 3]
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)<p[^>]*>"), "")
            .replace(Regex("(?i)</p>"), "\n\n")

            // 2. Convert HTML anchor tags <a href="http(s)://...">text</a> to Markdown links [text](url)[cite: 3]
            .replace(Regex("(?i)<a\\s+[^>]*href=[\"']([^\"']*)[\"'][^>]*>(.*?)</a>"), "[$2]($1)")

            // 3. Convert Headings (H1 to H6 -> Markdown #)[cite: 3]
            .replace(Regex("(?i)<h1[^>]*>(.*?)</h1>"), "\n# $1\n\n")
            .replace(Regex("(?i)<h2[^>]*>(.*?)</h2>"), "\n## $1\n\n")
            .replace(Regex("(?i)<h3[^>]*>(.*?)</h3>"), "\n### $1\n\n")
            .replace(Regex("(?i)<h4[^>]*>(.*?)</h4>"), "\n#### $1\n\n")

            // 4. Convert Bold and Italic tags to Markdown delimiters BEFORE removing tags[cite: 3]
            .replace(Regex("(?i)<(b|strong)[^>]*>(.*?)</\\1>"), "**$2**")
            .replace(Regex("(?i)<(i|em)[^>]*>(.*?)</\\1>"), "*$2*")

            // 5. Strip any remaining unsupported/unknown HTML tags[cite: 3]
            .replace(Regex("<[^>]*>"), "")

            // 6. Protect literal newlines before HtmlCompat.fromHtml() so it doesn't swallow them[cite: 3]
            .replace("\n", "___NEWLINE_TOKEN___")

            // 7. Decode all HTML entities (&nbsp;, &amp;, &#39;, &quot;, &#xxx;, etc.)[cite: 3]
            .let { raw ->
                HtmlCompat.fromHtml(raw, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
            }

            // 8. Restore preserved newlines[cite: 3]
            .replace("___NEWLINE_TOKEN___", "\n")

            // 9. Normalize non-breaking space characters (\u00A0) produced by &nbsp;[cite: 3]
            .replace('\u00A0', ' ')

            // 10. Cap excessive consecutive blank lines at 2 max[cite: 3]
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }
}