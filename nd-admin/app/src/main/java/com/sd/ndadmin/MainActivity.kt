package com.sd.ndadmin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import android.speech.RecognizerIntent

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.unit.sp

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sd.ndadmin.data.LikeRepository
import com.sd.ndadmin.network.GitHubPublisher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

// --- Custom Dark Ivory Palette ---
private val DeepBackground = Color(0xFF121212)
private val SurfaceCard = Color(0xFF1E1E24)
private val WarmIvory = Color(0xFFFFFFF0)
private val SoftIvory = Color(0xFFF5F5DC)
private val MutedIvory = Color(0xFFD1CCC0)
private val AccentGold = Color(0xFFE5C07B)

private val DarkIvoryColorScheme = darkColorScheme(
    primary = AccentGold,
    onPrimary = DeepBackground,
    surface = SurfaceCard,
    onSurface = SoftIvory,
    background = DeepBackground,
    onBackground = SoftIvory,
    onSurfaceVariant = MutedIvory,
    outline = Color(0xFF8C887B)
)

@Composable
fun DarkIvoryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkIvoryColorScheme,
        content = content
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DarkIvoryTheme {
                NDAdminApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NDAdminApp() {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
//        topBar = {
//            TopAppBar(
//                title = { Text("nd-admin", color = SoftIvory) },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = SurfaceCard,
//                    titleContentColor = SoftIvory
//                )
//            )
//        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceCard
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("Likes") },
                    icon = { Text("❤️") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("Publish") },
                    icon = { Text("📝") }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (selectedTab == 0) {
                LikesScreen()
            } else {
                PublishScreen()
            }
        }
    }
}

@Composable
fun LikesScreen() {
    val repository = remember { LikeRepository() }
    val likesList by repository.likesFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val listState = rememberLazyListState()

    // 1. Automatically scroll to the top item whenever a new entry arrives
    LaunchedEffect(likesList.firstOrNull()?.id) {
        if (likesList.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    if (likesList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No likes recorded yet.", color = MutedIvory)
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            items(likesList, key = { it.id }) { item ->
                val formattedTime = remember(item.timestamp) {
                    item.timestamp?.toDate()?.let { date ->
                        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(date)
                    } ?: "Just now"
                }

                // 2. Check if the entry is within 5 minutes old (300,000 ms)
                val isRecent = remember(item.timestamp) {
                    val itemMillis = item.timestamp?.toDate()?.time ?: System.currentTimeMillis()
                    (System.currentTimeMillis() - itemMillis) < (5 * 60 * 1000)
                }

                // Highlight color for new items vs default SurfaceCard
                val cardBgColor = if (isRecent) Color(0xFF574A2B) else SurfaceCard

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        // 3. Smaller font sizes for all lines
                        Text(
                            text = "${item.user} liked ${item.cardType}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = SoftIvory
                        )
                        if (item.contentId.isNotBlank()) {
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "Item ID: ${item.contentId}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MutedIvory
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PublishScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Helper for today's date
//    val todayFormatted = remember {
//        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
//    }
    val tomorrowFormatted = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    // Form Fields
    var postTitle by remember { mutableStateOf("") }
    var customSlug by remember { mutableStateOf("") }
    var isSlugManuallyEdited by remember { mutableStateOf(false) }
    var postDate by remember { mutableStateOf(tomorrowFormatted) } // Pre-filled with today's date
    var author by remember { mutableStateOf("sriram") }
    var tagsInput by remember { mutableStateOf("nithyadharma, thevaram") }
    var isHidden by remember { mutableStateOf(false) }
    var postContent by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isPublishing by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
    }

    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            if (!spokenText.isNullOrEmpty()) {
                postContent = if (postContent.isEmpty()) spokenText else "$postContent $spokenText"
            }
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AccentGold,
        unfocusedBorderColor = Color(0xFF3E3E42),
        focusedLabelColor = AccentGold,
        unfocusedLabelColor = MutedIvory,
        focusedTextColor = SoftIvory,
        unfocusedTextColor = SoftIvory
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Title Input
            OutlinedTextField(
                value = postTitle,
                onValueChange = { newTitle ->
                    postTitle = newTitle
                    // Auto-fill slug as you type UNLESS manually edited
                    if (!isSlugManuallyEdited) {
                        customSlug = newTitle.lowercase(Locale.ROOT)
                            .replace(Regex("[^a-z0-9]+"), "-")
                            .trim('-')
                    }
                },
                label = { Text("Post Title") },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Slug Input
            OutlinedTextField(
                value = customSlug,
                onValueChange = { newSlug ->
                    customSlug = newSlug
                    isSlugManuallyEdited = true // Stop auto-syncing once modified manually
                },
                label = { Text("Slug") },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Editable Date Input (Defaults to today: YYYY-MM-DD)
            OutlinedTextField(
                value = postDate,
                onValueChange = { postDate = it },
                label = { Text("Publish Date (YYYY-MM-DD)") },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Author Input
            OutlinedTextField(
                value = author,
                onValueChange = { author = it },
                label = { Text("Author") },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tags Input
            OutlinedTextField(
                value = tagsInput,
                onValueChange = { tagsInput = it },
                label = { Text("Tags (comma separated)") },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Hide Checkbox Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isHidden,
                    onCheckedChange = { isHidden = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AccentGold,
                        uncheckedColor = MutedIvory
                    )
                )
                Text(
                    text = "Hide Post from Index",
                    color = SoftIvory,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content Input
            OutlinedTextField(
                value = postContent,
                onValueChange = { postContent = it },
                label = { Text("Post Content (Markdown)") },
                colors = textFieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Image Picker & Dictation Row
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard, contentColor = SoftIvory)
                ) {
                    Text(if (selectedImageUri == null) "Pick Image" else "Change Image")
                }

                Button(
                    onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        }
                        speechLauncher.launch(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard, contentColor = SoftIvory)
                ) {
                    Text("🎙️ Dictate")
                }
            }

            selectedImageUri?.let { uri ->
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(model = uri, contentDescription = "Selected Image", modifier = Modifier.size(100.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Publish Button
            Button(
                enabled = !isPublishing && postTitle.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGold,
                    contentColor = DeepBackground
                ),
                // Inside MainActivity.kt -> PublishScreen() -> Button onClick

                onClick = {
                    isPublishing = true
                    scope.launch(Dispatchers.IO) {
                        val parsedTags = tagsInput.split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }

                        // Resize image to 640x480 before reading bytes
                        val imageBytes = selectedImageUri?.let { uri ->
                            processAndResizeImage(context, uri, 640, 480)
                        }

                        val publisher = GitHubPublisher()

//                        val markdownBody = if (selectedImageUri != null) {
//                            "# $postTitle\n\n$postContent\n\n![Image](img1.jpg)"
//                        } else {
//                            "# $postTitle\n\n$postContent"
//                        }
                        // adding a couple of newlines bwtween pic and image
                        val markdownBody = "\n$postContent"


                        val finalSlug = customSlug.ifBlank {
                            postTitle.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]+"), "-").trim('-')
                        }

                        val success = publisher.publishPost(
                            slug = finalSlug,
                            title = postTitle.trim(),
                            markdownContent = markdownBody,
                            author = author.ifBlank { "sriram" },
                            postdate = postDate.ifBlank { tomorrowFormatted },
                            tags = if (parsedTags.isNotEmpty()) parsedTags else listOf("nithyadharma"),
                            hide = isHidden,
                            imageBytes = imageBytes
                        )

                        withContext(Dispatchers.Main) {
                            isPublishing = false
                            if (success) {
                                Toast.makeText(context, "Published to GitHub Pages!", Toast.LENGTH_LONG).show()
                                postTitle = ""
                                customSlug = ""
                                isSlugManuallyEdited = false
                                postDate = tomorrowFormatted
                                postContent = ""
                                selectedImageUri = null
                            } else {
                                Toast.makeText(context, "Failed to publish.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            ) {
                if (isPublishing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = DeepBackground)
                } else {
                    Text("Publish Post")
                }
            }
        }
    }
} // publish function ends

private fun processAndResizeImage(
    context: Context,
    uri: Uri,
    targetWidth: Int = 640,
    targetHeight: Int = 480
): ByteArray? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null
            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
            val outputStream = ByteArrayOutputStream()

            // Compress to JPEG with 85% quality
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)

            // Clean up bitmap memory
            if (scaledBitmap != originalBitmap) {
                originalBitmap.recycle()
            }
            scaledBitmap.recycle()

            outputStream.toByteArray()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}