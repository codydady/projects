package com.sd.nithyadharma.cards

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

@Composable
fun DharmaTodayCardContent(
    paramsMap: Map<String, JsonElement>,
    textColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 6.dp,
                end = 6.dp,
                top = 6.dp,
                bottom = 6.dp
            )
    ) {

        // -----------
        // 1. IMAGE
        // -----------
        val imageBase64String = (paramsMap["postImageBytes"] as? JsonPrimitive)?.contentOrNull
        val postTitle = (paramsMap["postTitle"] as? JsonPrimitive)?.contentOrNull ?: "Temple Page"
        val postContent = (paramsMap["postContent"] as? JsonPrimitive)?.contentOrNull ?: "Temple Sthalapuranam"

        DisplayPostImage(imageBase64String, postTitle)

        // ---------
        // 2. TEXT
        // ---------

        Text(
            text = postTitle,
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 8.dp,
                    bottom = 4.dp
                )
        )

        // Parse markdown formatting (headings, bold, italics, http/https links) into AnnotatedString off recomposition
        val linkColor = MaterialTheme.colorScheme.primary
        val annotatedContent = remember(postContent, linkColor) {
            parseMarkdownToAnnotatedString(postContent, linkColor)
        }

        Text(
            text = annotatedContent,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor.copy(alpha = 0.85f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 4.dp
                )
        )
    } // column ends

} // card ends

@Composable
fun DisplayPostImage(imageBase64String: String?, postTitle: String) {
    // Wrap with remember so Base64 decoding & bitmap parsing isn't repeated on every recomposition
    val bitmap = remember(imageBase64String) {
        imageBase64String?.let { base64 ->
            try {
                val bytes = Base64.decode(base64, Base64.NO_WRAP)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) {
                null
            }
        }
    }

    bitmap?.let { bmp ->
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = postTitle,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * Parses Markdown lines (Headings #, ##, ###), inline formatting (**bold**, *italic*),
 * Markdown links [title](http://...), and raw http:// / https:// URLs into a clickable AnnotatedString.
 */
fun parseMarkdownToAnnotatedString(
    text: String,
    linkColor: Color = Color(0xFF1E88E5)
): AnnotatedString {
    if (text.isEmpty()) return AnnotatedString("")

    return buildAnnotatedString {
        val lines = text.split("\n")

        lines.forEachIndexed { index, line ->
            val trimmedLine = line.trim()

            when {
                // H1 Heading (# Title)
                trimmedLine.startsWith("# ") -> {
                    val content = trimmedLine.removePrefix("# ").trim()
                    withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)) {
                        appendInlineFormattedText(content, linkColor)
                    }
                }
                // H2 Heading (## Title)
                trimmedLine.startsWith("## ") -> {
                    val content = trimmedLine.removePrefix("## ").trim()
                    withStyle(SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)) {
                        appendInlineFormattedText(content, linkColor)
                    }
                }
                // H3 - H6 Headings (### Title)
                trimmedLine.startsWith("### ") || trimmedLine.startsWith("#### ") -> {
                    val content = trimmedLine.replace(Regex("^#+\\s*"), "")
                    withStyle(SpanStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold)) {
                        appendInlineFormattedText(content, linkColor)
                    }
                }
                // Regular Body Line
                else -> {
                    appendInlineFormattedText(line, linkColor)
                }
            }

            // Preserve line breaks between lines
            if (index < lines.lastIndex) {
                append("\n")
            }
        }
    }
}

/**
 * Helper to process inline **bold**, *italic*, [label](http/https), and standalone http:// or https:// URLs.
 */
private fun AnnotatedString.Builder.appendInlineFormattedText(line: String, linkColor: Color) {
    // Regex matching:
    // Group 2: **bold**
    // Group 3: *italic*
    // Group 4 & 5: [markdown label](http(s)://url)
    // Group 6: Raw http:// or https:// URLs (ignoring trailing punctuation)
    val pattern = Regex("""(\*\*([^*]+)\*\*|\*([^*]+)\*|\[([^\]]+)\]\(((?i)https?://[^\s)]+)\)|((?i)https?://[^\s<)]+?)(?=[.,!?:;]?(?:\s|\)|$)))""")
    var lastIndex = 0

    val linkStyle = TextLinkStyles(
        style = SpanStyle(
            color = linkColor,
            textDecoration = TextDecoration.Underline
        )
    )

    pattern.findAll(line).forEach { matchResult ->
        val range = matchResult.range
        // Append unformatted text preceding the match
        append(line.substring(lastIndex, range.first))

        val boldContent = matchResult.groups[2]?.value
        val italicContent = matchResult.groups[3]?.value
        val markdownLabel = matchResult.groups[4]?.value
        val markdownUrl = matchResult.groups[5]?.value
        val rawUrl = matchResult.groups[6]?.value

        when {
            boldContent != null -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(boldContent)
                }
            }
            italicContent != null -> {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(italicContent)
                }
            }
            markdownUrl != null && markdownLabel != null -> {
                withLink(LinkAnnotation.Url(url = markdownUrl, styles = linkStyle)) {
                    append(markdownLabel)
                }
            }
            rawUrl != null -> {
                withLink(LinkAnnotation.Url(url = rawUrl, styles = linkStyle)) {
                    append(rawUrl)
                }
            }
        }

        lastIndex = range.last + 1
    }

    if (lastIndex < line.length) {
        append(line.substring(lastIndex))
    }
}