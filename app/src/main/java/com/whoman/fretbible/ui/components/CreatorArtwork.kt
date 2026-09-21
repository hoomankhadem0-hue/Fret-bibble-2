package com.whoman.fretbible.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.whoman.fretbible.R

/**
 * Uses the packaged creator artwork directly instead of decoding a hand-built
 * Base64 bitmap at runtime. This keeps the Profile screen safe on all devices.
 */
@Composable
fun CreatorSketchImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.creator_sketch),
        contentDescription = "Creator sketch",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}
