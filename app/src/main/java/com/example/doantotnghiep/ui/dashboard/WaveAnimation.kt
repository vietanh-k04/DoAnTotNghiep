package com.example.doantotnghiep.ui.dashboard

import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doantotnghiep.R
import com.example.doantotnghiep.ui.theme.SurfaceLight
import com.example.doantotnghiep.ui.theme.TextPrimaryLight
import com.example.doantotnghiep.ui.theme.TextSecondaryLight
import com.example.doantotnghiep.ui.theme.WaterBlue
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WaveAnimation(
    modifier: Modifier,
    waterLevel: Float,
    waveColor: Color,
) {
    val transition = rememberInfiniteTransition(label = "wave_transition")

    val waveShift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_shift"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val waterHeightY = height * (1 - waterLevel)

        val path = Path().apply {
            moveTo(0f, height)
            lineTo(0f, waterHeightY)

            for(x in 0..width.toInt() step 10) {
                val waveAmplitude = 25f
                val y = waterHeightY + sin((x / 60f) + waveShift) * waveAmplitude
                lineTo(x.toFloat(), y)
            }

            lineTo(width, waterHeightY)
            lineTo(width, height)
            close()
        }

        drawPath(path = path, color = waveColor, style = Fill)
    }
}
