package com.sd.nithyadharma.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import com.sd.nithyadharma.dao.PostOfDayRepository

@Composable
fun DharmaTodayCardContent(
    post: PostOfDayRepository.PostOfDay,
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

        // -------------------------------------------------
        // 1. IMAGE
        // -------------------------------------------------

        post.imageBytes?.let { bytes ->

            val bitmap = BitmapFactory
                .decodeByteArray(
                    bytes,
                    0,
                    bytes.size
                )

            bitmap?.let {

                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = post.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(
                            RoundedCornerShape(8.dp)
                        ),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // -------------------------------------------------
        // 2. TEXT
        // -------------------------------------------------

        Text(
            text = post.title,
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

        Text(
            text = post.contentsMarkdown,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor.copy(alpha = 0.85f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 4.dp
                )
        )
    }
}