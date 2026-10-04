package com.rifsxd.ksunext.ui.component

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

data class BackgroundSettings(
    val uri: String? = null,
    val fillScreen: Boolean = true,
    val dimAlpha: Float = 0f
)

@Composable
fun AppBackground(settings: BackgroundSettings, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val baseColor = MaterialTheme.colorScheme.background

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(baseColor)
        )

        val uri = settings.uri ?: return@Box

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(Uri.parse(uri))
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = if (settings.fillScreen) ContentScale.Crop else ContentScale.Fit
        )

        val dim = settings.dimAlpha.coerceIn(0f, 1f)
        if (dim > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = dim))
            )
        }
    }
}
