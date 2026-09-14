package com.rifsxd.ksunext.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import java.util.Locale

private fun hsvColor(h: Float, s: Float, v: Float) = Color.hsv(h, s, v)

@Composable
fun ColorPickerDialog(
    initial: Color,
    onDismiss: () -> Unit,
    onConfirm: (Color) -> Unit
) {
    val startHsv = remember(initial) {
        FloatArray(3).also { android.graphics.Color.colorToHSV(initial.toArgb(), it) }
    }

    var hue by remember { mutableFloatStateOf(startHsv[0]) }
    var sat by remember { mutableFloatStateOf(startHsv[1]) }
    var value by remember { mutableFloatStateOf(startHsv[2]) }

    val picked = hsvColor(hue, sat, value)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(picked) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(picked)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = String.format(Locale.ROOT, "#%06X", picked.toArgb() and 0xFFFFFF),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    fun update(pos: Offset) {
                                        sat = (pos.x / size.width).coerceIn(0f, 1f)
                                        value = 1f - (pos.y / size.height).coerceIn(0f, 1f)
                                    }
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    down.consume()
                                    update(down.position)
                                    drag(down.id) { change ->
                                        change.consume()
                                        update(change.position)
                                    }
                                }
                            }
                    ) {
                        drawRect(
                            brush = Brush.horizontalGradient(
                                listOf(Color.White, hsvColor(hue, 1f, 1f))
                            )
                        )
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black)
                            )
                        )
                        val cx = sat * size.width
                        val cy = (1f - value) * size.height
                        drawCircle(Color.White, radius = 11f, center = Offset(cx, cy))
                        drawCircle(picked, radius = 8f, center = Offset(cx, cy))
                    }
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                fun update(x: Float) {
                                    hue = ((x / size.width) * 360f).coerceIn(0f, 360f)
                                }
                                val down = awaitFirstDown(requireUnconsumed = false)
                                down.consume()
                                update(down.position.x)
                                drag(down.id) { change ->
                                    change.consume()
                                    update(change.position.x)
                                }
                            }
                        }
                ) {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            (0..6).map { hsvColor(it * 60f, 1f, 1f) }
                        )
                    )
                    val x = (hue / 360f) * size.width
                    drawCircle(Color.White, radius = 13f, center = Offset(x, size.height / 2f))
                    drawCircle(
                        hsvColor(hue, 1f, 1f),
                        radius = 10f,
                        center = Offset(x, size.height / 2f)
                    )
                }
            }
        }
    )
}

@Composable
fun RainbowSwatch(
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                Brush.sweepGradient((0..6).map { hsvColor(it * 60f, 0.75f, 0.95f) })
            )
            .padding(0.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
