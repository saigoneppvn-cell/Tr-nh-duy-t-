package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndigoDeep
import com.example.ui.theme.LocalMeoMeoPalette
import com.example.ui.theme.PurpleBright
import com.example.ui.theme.PurpleGlow
import com.example.ui.theme.PurplePrimary

@Composable
fun MeoMeoBackgroundWaves(modifier: Modifier = Modifier) {
    val palette = LocalMeoMeoPalette.current
    Box(modifier = modifier.fillMaxSize()) {
        // Top-left wave mesh
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp)
                .align(Alignment.TopStart)
        ) {
            val sx = size.width / 390f
            val sy = size.height / 280f

            val wave1 = Path().apply {
                moveTo(-20f * sx, -20f * sy)
                lineTo(290f * sx, -20f * sy)
                cubicTo(
                    220f * sx, 70f * sy,
                    110f * sx, 110f * sy,
                    -20f * sx, 130f * sy
                )
                close()
            }
            drawPath(
                path = wave1,
                brush = Brush.linearGradient(
                    colors = listOf(
                        palette.waveTopPrimary.copy(alpha = if (palette.isDark) 0.72f else 0.45f),
                        palette.waveTopPrimary.copy(alpha = if (palette.isDark) 0.35f else 0.22f),
                        palette.background.copy(alpha = 0f)
                    ),
                    start = Offset.Zero,
                    end = Offset(320f * sx, 200f * sy)
                )
            )

            val wave2 = Path().apply {
                moveTo(-30f * sx, 90f * sy)
                cubicTo(
                    90f * sx, 70f * sy,
                    210f * sx, 110f * sy,
                    340f * sx, 210f * sy
                )
                lineTo(-30f * sx, 220f * sy)
                close()
            }
            drawPath(
                path = wave2,
                brush = Brush.linearGradient(
                    colors = listOf(
                        palette.waveTopSecondary.copy(alpha = if (palette.isDark) 0.45f else 0.30f),
                        palette.background.copy(alpha = 0f)
                    ),
                    start = Offset(-20f * sx, 40f * sy),
                    end = Offset(280f * sx, 240f * sy)
                )
            )
        }

        // Bottom mesh wave
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .align(Alignment.BottomStart)
                .padding(bottom = 56.dp)
        ) {
            val sx = size.width / 390f
            val sy = size.height / 250f

            val botWave1 = Path().apply {
                moveTo(390f * sx, 20f * sy)
                cubicTo(
                    250f * sx, 40f * sy,
                    160f * sx, 140f * sy,
                    0f, 210f * sy
                )
                lineTo(0f, 250f * sy)
                lineTo(390f * sx, 250f * sy)
                close()
            }
            drawPath(
                path = botWave1,
                brush = Brush.linearGradient(
                    colors = listOf(
                        palette.waveBotPrimary.copy(alpha = if (palette.isDark) 0.65f else 0.40f),
                        palette.waveBotSecondary.copy(alpha = if (palette.isDark) 0.32f else 0.20f),
                        palette.background.copy(alpha = 0f)
                    ),
                    start = Offset(390f * sx, 40f * sy),
                    end = Offset(40f * sx, 240f * sy)
                )
            )

            val botWave2 = Path().apply {
                moveTo(390f * sx, 110f * sy)
                cubicTo(
                    260f * sx, 95f * sy,
                    150f * sx, 165f * sy,
                    20f * sx, 225f * sy
                )
                lineTo(390f * sx, 245f * sy)
                close()
            }
            drawPath(
                path = botWave2,
                brush = Brush.linearGradient(
                    colors = listOf(
                        palette.waveBotSecondary.copy(alpha = if (palette.isDark) 0.55f else 0.35f),
                        palette.background.copy(alpha = 0.08f)
                    ),
                    start = Offset(250f * sx, 90f * sy),
                    end = Offset(10f * sx, 250f * sy)
                )
            )
        }
    }
}

@Composable
fun MeoMeoCatLogo(
    modifier: Modifier = Modifier,
    width: Dp = 112.dp,
    height: Dp = 96.dp
) {
    Box(
        modifier = modifier.size(width = width + 28.dp, height = height + 24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Ambient purple glow matching drop-shadow(0 0 18px rgba(168,85,247,0.55))
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        PurpleGlow.copy(alpha = 0.42f),
                        PurplePrimary.copy(alpha = 0.16f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.minDimension * 0.62f
                )
            )
        }

        Canvas(modifier = Modifier.size(width = width, height = height)) {
            val sx = size.width / 120f
            val sy = size.height / 100f

            // Cat head silhouette
            val headPath = Path().apply {
                moveTo(22f * sx, 65f * sy)
                cubicTo(14f * sx, 55f * sy, 18f * sx, 39f * sy, 30f * sx, 33f * sy)
                lineTo(28f * sx, 16f * sy)
                cubicTo(28f * sx, 13f * sy, 32f * sx, 11f * sy, 35f * sx, 13f * sy)
                lineTo(50f * sx, 25f * sy)
                cubicTo(57f * sx, 23f * sy, 65f * sx, 23f * sy, 72f * sx, 25f * sy)
                lineTo(87f * sx, 13f * sy)
                cubicTo(90f * sx, 11f * sy, 94f * sx, 13f * sy, 94f * sx, 16f * sy)
                lineTo(92f * sx, 33f * sy)
                cubicTo(103f * sx, 39f * sy, 107f * sx, 55f * sy, 99f * sx, 65f * sy)
                cubicTo(91f * sx, 76f * sy, 76f * sx, 82f * sy, 60f * sx, 82f * sy)
                cubicTo(44f * sx, 82f * sy, 29f * sx, 76f * sy, 22f * sx, 65f * sy)
                close()
            }
            drawPath(
                path = headPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        PurpleBright.copy(alpha = 0.92f),
                        PurplePrimary.copy(alpha = 0.90f),
                        IndigoDeep.copy(alpha = 0.92f)
                    ),
                    start = Offset(15f * sx, 20f * sy),
                    end = Offset(95f * sx, 90f * sy)
                )
            )

            // Crisp ear & cheek rim highlight
            val rimPath = Path().apply {
                moveTo(26f * sx, 42f * sy)
                lineTo(28f * sx, 15f * sy)
                cubicTo(28f * sx, 12.5f * sy, 31.5f * sx, 11f * sy, 34f * sx, 12.8f * sy)
                lineTo(48f * sx, 24f * sy)
                cubicTo(56f * sx, 22f * sy, 65f * sx, 22f * sy, 73f * sx, 24f * sy)
                lineTo(87f * sx, 12.8f * sy)
                cubicTo(89.5f * sx, 11f * sy, 93f * sx, 12.5f * sy, 93f * sx, 15f * sy)
                lineTo(95f * sx, 40f * sy)
            }
            drawPath(
                path = rimPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFE9D5FF),
                        Color(0xFF7C3AED).copy(alpha = 0.35f)
                    ),
                    start = Offset(20f * sx, 10f * sy),
                    end = Offset(100f * sx, 85f * sy)
                ),
                style = Stroke(width = 4.2f * sx, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Glowing chin curve
            val chinPath = Path().apply {
                moveTo(23f * sx, 62f * sy)
                cubicTo(31f * sx, 75f * sy, 45f * sx, 81f * sy, 61f * sx, 81f * sy)
                cubicTo(77f * sx, 81f * sy, 91f * sx, 75f * sy, 98f * sx, 63f * sy)
            }
            drawPath(
                path = chinPath,
                color = PurpleGlow,
                style = Stroke(width = 3.0f * sx, cap = StrokeCap.Round)
            )

            // Happy smiling cat eyes
            val leftEye = Path().apply {
                moveTo(36f * sx, 56f * sy)
                cubicTo(39f * sx, 48f * sy, 50f * sx, 48f * sy, 53f * sx, 56f * sy)
            }
            val rightEye = Path().apply {
                moveTo(68f * sx, 56f * sy)
                cubicTo(71f * sx, 48f * sy, 82f * sx, 48f * sy, 85f * sx, 56f * sy)
            }
            drawPath(
                path = leftEye,
                color = Color.White,
                style = Stroke(width = 5.0f * sx, cap = StrokeCap.Round)
            )
            drawPath(
                path = rightEye,
                color = Color.White,
                style = Stroke(width = 5.0f * sx, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun MeoMeoToast(
    message: String?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier
    ) {
        if (message != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xF21E1E26))
                    .border(1.dp, PurpleGlow.copy(alpha = 0.45f), RoundedCornerShape(50))
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
