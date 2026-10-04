package com.rifsxd.ksunext.ui.component

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest

data class BackgroundSettings(
    val uri: String? = null,
    val isVideo: Boolean = false,
    val fillScreen: Boolean = true,
    val dimAlpha: Float = 0f
)

val LocalBackgroundSettings = staticCompositionLocalOf { BackgroundSettings() }

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

        if (settings.isVideo) {
            VideoBackground(
                uri = uri,
                fillScreen = settings.fillScreen,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(Uri.parse(uri))
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = if (settings.fillScreen) ContentScale.Crop else ContentScale.Fit
            )
        }

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

@Composable
private fun VideoBackground(
    uri: String,
    fillScreen: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exoPlayer = remember(uri) {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 0f
                playWhenReady = true
                setMediaItem(MediaItem.fromUri(Uri.parse(uri)))
                prepare()
            }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = false
                player = exoPlayer
                resizeMode = if (fillScreen) {
                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                } else {
                    AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            }
        },
        update = { view ->
            view.resizeMode = if (fillScreen) {
                AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            } else {
                AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        }
    )
}
