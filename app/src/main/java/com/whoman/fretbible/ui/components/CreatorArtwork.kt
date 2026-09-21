package com.whoman.fretbible.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

private const val CREATOR_SKETCH_B64 =
    CREATOR_CHUNK_0 + CREATOR_CHUNK_1 + CREATOR_CHUNK_2 + CREATOR_CHUNK_3 +
    CREATOR_CHUNK_4 + CREATOR_CHUNK_5 + CREATOR_CHUNK_6 + CREATOR_CHUNK_7 +
    CREATOR_CHUNK_8 + CREATOR_CHUNK_9

@Composable
fun CreatorSketchImage(modifier: Modifier = Modifier) {
    val image = remember {
        val bytes = Base64.decode(CREATOR_SKETCH_B64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
    }
    Image(
        bitmap = image,
        contentDescription = "Creator sketch",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}
