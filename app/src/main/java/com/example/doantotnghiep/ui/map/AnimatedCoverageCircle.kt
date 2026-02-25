package com.example.doantotnghiep.ui.map

import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle

@Composable
fun AnimatedCoverageCircle(stationLatLng: LatLng, coverageRadius: Double, statusColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")

    val animatedRadius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = coverageRadius.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadiusAnimation"
    )

    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AlphaAnimation"
    )

    Circle(
        center = stationLatLng,
        radius = coverageRadius,
        fillColor = statusColor.copy(alpha = 0.05f),
        strokeColor = statusColor.copy(alpha = 0.2f),
        strokeWidth = 2f
    )

    Circle(
        center = stationLatLng,
        radius = animatedRadius.toDouble(),
        fillColor = statusColor.copy(alpha = animatedAlpha),
        strokeColor = Color.Transparent,
        strokeWidth = 0f
    )
}