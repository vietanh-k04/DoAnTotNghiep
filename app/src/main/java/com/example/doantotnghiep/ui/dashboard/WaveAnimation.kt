package com.example.doantotnghiep.ui.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WaveAnimation(
    modifier: Modifier = Modifier,
    waterLevel: Float,
    waveColor: Color,
) {
    val animatedWaterLevel by animateFloatAsState(
        targetValue = waterLevel,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
        label = "water_level_animation"
    )

    val transition = rememberInfiniteTransition(label = "wave_transition")
    val waveShift by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_shift"
    )

    val density = LocalDensity.current
    val waveAmplitude = with(density) { 15.dp.toPx() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val waterHeightY = height * (1 - animatedWaterLevel)

        val path = Path().apply {
            moveTo(0f, height)
            lineTo(0f, waterHeightY)

            for (x in 0..width.toInt() step 5) {
                val relativeX = x / width

                val sineValue = sin(2 * PI * 1.5 * relativeX + waveShift).toFloat()

                val y = waterHeightY + sineValue * waveAmplitude
                lineTo(x.toFloat(), y)
            }

            lineTo(width, height)
            close()
        }

        drawPath(path = path, color = waveColor, style = Fill)
    }
}