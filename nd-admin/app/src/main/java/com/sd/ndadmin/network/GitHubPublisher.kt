package com.sd.ndadmin.network

import android.util.Base64
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PostMetadata(
    val slug: String,
    val type: String = "posts",
    val title: String,
    val author: String = "sriram",
    val date: String,
    val tags: List<String> = listOf("nithyadharma", "gurus"),
    val hide: Boolean = false
)

class GitHubPublisher(
    private val owner: String = "codydady",
    private val repo: String = "projects",
    // Replace this with your actual GitHub PAT (starts with ghp_ or github_pat_)
    private val token: String = "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",
    private val basePath: String = "templepages-github-site/www.templepages.com"
) {
    private val client = OkHttpClient()
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    companion object {
        private const val TAG = "GitHubPublisher"
    }

    suspend fun publishPost(
        slug: String,
        title: String,
        markdownContent: String,
        author: String = "sriram",
        postdate: String? = null, // Custom date optional parameter
        tags: List<String> = listOf("nithyadharma", "templetracker"),
        hide: Boolean = false,
        imageBytes: ByteArray? = null
    ): Boolean = withContext(Dispatchers.IO) {
//        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        // Uses provided date or falls back to today's date
        val postDate = postdate.takeIf { !it.isNullOrBlank() }
            ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        val newEntry = PostMetadata(
            slug = slug,
            type = "posts",
            title = title,
            author = author,
            date = postDate,
            tags = tags,
            hide = hide
        )

        Log.d(TAG, "==========================================================")
        Log.d(TAG, "NEW POST METADATA JSON OBJECT:")
        Log.d(TAG, gson.toJson(newEntry))
        Log.d(TAG, "==========================================================")

        try {
            // 1. Upload Markdown Post
            val postPath = "$basePath/posts/$slug/contents.md"
            Log.d(TAG, "Step 1: Uploading Markdown content to: $postPath")
            val markdownSuccess = uploadFileToGithub(
                path = postPath,
                contentBytes = markdownContent.toByteArray(Charsets.UTF_8),
                commitMessage = "Add contents.md for post: $title"
            )

            if (!markdownSuccess) {
                Log.e(TAG, "❌ STEP 1 FAILED: Could not upload Markdown file at $postPath")
                return@withContext false
            }
            Log.d(TAG, "✅ STEP 1 SUCCESS: Markdown uploaded.")

            // 2. Upload Image (if provided)
            if (imageBytes != null) {
                val imagePath = "$basePath/posts/$slug/img1.jpg"
                Log.d(TAG, "Step 2: Uploading image (${imageBytes.size} bytes) to: $imagePath")
                val imageSuccess = uploadFileToGithub(
                    path = imagePath,
                    contentBytes = imageBytes,
                    commitMessage = "Add image for $slug"
                )
                if (!imageSuccess) {
                    Log.w(TAG, "⚠️ STEP 2 WARNING: Failed to upload image at $imagePath, continuing to update posts.json...")
                } else {
                    Log.d(TAG, "✅ STEP 2 SUCCESS: Image uploaded.")
                }
            }

            // 3. Update posts.json Index
            Log.d(TAG, "Step 3: Updating posts.json index...")
            val jsonSuccess = updatePostsJsonIndex(newEntry)

            if (jsonSuccess) {
                Log.d(TAG, "🎉 ALL STEPS COMPLETED SUCCESSFULLY!")
            } else {
                Log.e(TAG, "❌ STEP 3 FAILED: posts.json update failed.")
            }

            return@withContext jsonSuccess

        } catch (e: Exception) {
            Log.e(TAG, "🔥 UNHANDLED EXCEPTION during publishPost", e)
            return@withContext false
        }
    }

    private fun uploadFileToGithub(path: String, contentBytes: ByteArray, commitMessage: String): Boolean {
        val url = "https://api.github.com/repos/$owner/$repo/contents/$path"
        Log.d(TAG, "uploadFileToGithub -> Fetching SHA for existing file at: $path")
        val existingSha = getFileSha(path)

        val base64Content = Base64.encodeToString(contentBytes, Base64.NO_WRAP)

        val bodyJson = JSONObject().apply {
            put("message", commitMessage)
            put("content", base64Content)
            if (existingSha != null) {
                put("sha", existingSha)
            }
        }

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/vnd.github.v3+json")
            .put(bodyJson.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val code = response.code
            val responseBodyStr = response.body?.string() ?: ""

            Log.d(TAG, "uploadFileToGithub PUT Response Code: $code")
            Log.d(TAG, "uploadFileToGithub PUT Response Body:\n$responseBodyStr")

            if (code == 401) {
                Log.e(TAG, "🔑 AUTH ERROR: Invalid or expired GitHub Personal Access Token. Verify token in GitHubPublisher.kt")
            }

            return response.isSuccessful
        }
    }

    private fun updatePostsJsonIndex(newPost: PostMetadata): Boolean {
        val jsonPath = "$basePath/posts.json"
        val url = "https://api.github.com/repos/$owner/$repo/contents/$jsonPath"

        val (currentSha, currentContentRaw) = getFileShaAndContent(jsonPath)

        val postsList: MutableList<PostMetadata> = if (!currentContentRaw.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<MutableList<PostMetadata>>() {}.type
                gson.fromJson(currentContentRaw, type) ?: mutableListOf()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse existing posts.json string into GSON model!", e)
                mutableListOf()
            }
        } else {
            mutableListOf()
        }

        postsList.removeAll { it.slug == newPost.slug }
        postsList.add(0, newPost)

        val updatedJsonString = gson.toJson(postsList)

//        Log.d(TAG, "==========================================================")
//        Log.d(TAG, "=== FULL INDEX posts.json PAYLOAD TO BE UPLOADED ===")
//        Log.d(TAG, updatedJsonString)
//        Log.d(TAG, "==========================================================")

        val base64Content = Base64.encodeToString(updatedJsonString.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

        val bodyJson = JSONObject().apply {
            put("message", "Update posts.json for ${newPost.slug}")
            put("content", base64Content)
            if (currentSha != null) {
                put("sha", currentSha)
            }
        }

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/vnd.github.v3+json")
            .put(bodyJson.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val code = response.code
            val responseBodyStr = response.body?.string() ?: ""

            Log.d(TAG, "updatePostsJsonIndex PUT Response Code: $code")
            Log.d(TAG, "updatePostsJsonIndex PUT Response Body:\n$responseBodyStr")

            return response.isSuccessful
        }
    }

    private fun getFileSha(path: String): String? {
        return getFileShaAndContent(path).first
    }

    private fun getFileShaAndContent(path: String): Pair<String?, String?> {
        val url = "https://api.github.com/repos/$owner/$repo/contents/$path"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/vnd.github.v3+json")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.w(TAG, "getFileShaAndContent GET failed with HTTP ${response.code} for path: $path")
                return Pair(null, null)
            }
            val jsonString = response.body?.string() ?: return Pair(null, null)
            val jsonObject = JSONObject(jsonString)

            val sha = jsonObject.optString("sha", null)
            val encodedContent = jsonObject.optString("content", "").replace("\n", "").replace("\r", "")
            val decodedContent = if (encodedContent.isNotEmpty()) {
                try {
                    String(Base64.decode(encodedContent, Base64.DEFAULT), Charsets.UTF_8)
                } catch (e: Exception) {
                    null
                }
            } else null

            return Pair(sha, decodedContent)
        }
    }
}