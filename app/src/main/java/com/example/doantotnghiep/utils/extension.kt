package com.example.doantotnghiep.utils

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.doantotnghiep.R
import com.example.doantotnghiep.data.local.WaveCardUiModel
import com.example.doantotnghiep.data.local.enum.Status
import com.example.doantotnghiep.data.local.enum.Trend
import com.example.doantotnghiep.data.local.state.HomeUiState
import com.example.doantotnghiep.ui.theme.DangerColor
import com.example.doantotnghiep.ui.theme.GoodColor
import com.example.doantotnghiep.ui.theme.SoftBgBottom
import com.example.doantotnghiep.ui.theme.SoftBgTop
import com.example.doantotnghiep.ui.theme.StatusDanger
import com.example.doantotnghiep.ui.theme.StatusSuccess
import com.example.doantotnghiep.ui.theme.StatusWarning
import com.example.doantotnghiep.ui.theme.TextSelected
import com.example.doantotnghiep.ui.theme.WarningColor
import com.example.doantotnghiep.ui.theme.WaterBlue
import com.example.doantotnghiep.ui.theme.Yellow

fun Modifier.appBackground(): Modifier = this.background(
    Brush.verticalGradient(listOf(SoftBgTop, SoftBgBottom))
)

fun AnimatedContentTransitionScope<Boolean>.homeTransitionSpec(duration: Int): ContentTransform {
    val easing = FastOutSlowInEasing
    return if (targetState) {
        (scaleIn(
            animationSpec = tween(duration, easing = easing),
            initialScale = 0.0f,
            transformOrigin = TransformOrigin(0.9f, 0.85f)
        ) + fadeIn(
            animationSpec = tween(duration, easing = easing)
        )).togetherWith(
            fadeOut(animationSpec = tween(duration / 2, easing = easing))
        ).apply { targetContentZIndex = 1f }
    } else {
        fadeIn(
            animationSpec = tween(duration, easing = easing)
        ).togetherWith(
            scaleOut(
                animationSpec = tween(duration, easing = easing),
                targetScale = 0.0f,
                transformOrigin = TransformOrigin(0.9f, 0.85f)
            ) + fadeOut(
                animationSpec = tween(duration, easing = easing)
            )
        ).apply { targetContentZIndex = -1f }
    }
}

@Composable
fun HomeUiState.toWaveCardUiModel(): WaveCardUiModel {
    val timeDisplay = formatTimeAgo(this.timestamp)
    return WaveCardUiModel(
        waterLevel = this.waterLevel,
        maxHeight = this.maxHeight,
        status = this.status,
        trend = this.trend,
        lastUpdated = timeDisplay,
        waterPercent = this.waterPercent
    )
}

@Composable
fun getRainStatus(rainVal: Int): Triple<ImageVector, String, Color> {
    return when {
        rainVal > 900 -> Triple(Icons.Default.WbSunny, stringResource(R.string.dashboard_dry), Yellow)
        rainVal in 600..900 -> Triple(Icons.Default.Cloud, stringResource(R.string.dashboard_light_rain), TextSelected)
        rainVal in 300..599 -> Triple(Icons.Default.Umbrella, stringResource(R.string.dashboard_moderate_rain), WaterBlue)
        else -> Triple(Icons.Default.Thunderstorm, stringResource(R.string.dashboard_heavy_rain), StatusDanger)
    }
}

val WaveCardUiModel.statusColor: Color
    @Composable get() = when (status) {
        R.string.status_danger -> StatusDanger
        R.string.status_warning -> StatusWarning
        else -> StatusSuccess
    }

val WaveCardUiModel.statusIcon: ImageVector
    @Composable get() = when (status) {
        R.string.status_danger -> Icons.Default.Dangerous
        R.string.status_warning -> Icons.Default.Warning
        else -> Icons.Default.CheckCircle
    }

val WaveCardUiModel.waveColor: Color
    @Composable get() = when (status) {
        R.string.status_danger -> StatusDanger
        R.string.status_warning -> StatusWarning
        else -> WaterBlue
    }

fun statusColor(status: Status) : Color {
    return when(status) {
        Status.DANGER -> StatusDanger
        Status.WARNING  -> StatusWarning
        Status.OFFLINE -> Color.Gray
        else -> StatusSuccess
    }
}

fun trendColor(trend: Trend) : Color {
    return when(trend) {
        Trend.RISING -> StatusDanger
        Trend.FALLING -> StatusSuccess
        else -> Color.Gray
    }
}

@Composable
fun trendText(trend: Trend) : String {
    return when(trend) {
        Trend.RISING -> stringResource(R.string.map_rising)
        Trend.FALLING -> "Đang giảm"
        else -> stringResource(R.string.map_stable)
    }
}

@Composable
fun statusText(status: Status) : String {
    return when(status) {
        Status.DANGER -> stringResource(R.string.status_danger)
        Status.WARNING  -> stringResource(R.string.status_warning)
        Status.OFFLINE -> "Ngưng hoạt động"
        else -> stringResource(R.string.status_safe)
    }
}

@Composable
fun statusIcon(status: Status) : ImageVector {
    return when (status) {
        Status.DANGER -> Icons.Default.Dangerous
        Status.WARNING -> Icons.Default.Warning
        Status.OFFLINE -> Icons.Default.Info
        else -> Icons.Default.CheckCircle
    }
}

fun getLottieWeatherResource(iconUrl: String): Int {
    val isDay = iconUrl.contains("/day/")
    val code = iconUrl.substringAfterLast("/").substringBefore(".png").toIntOrNull() ?: 113

    return when (code) {
        113 -> if (isDay) R.raw.clear_day else R.raw.clear_night
        116 -> if (isDay) R.raw.partly_cloudy_day else R.raw.partly_cloudy_night
        119 -> R.raw.cloudy
        122 -> R.raw.overcast
        143 -> R.raw.mist
        176 -> if (isDay) R.raw.partly_cloudy_day_rain else R.raw.partly_cloudy_night_rain
        179 -> if (isDay) R.raw.partly_cloudy_day_snow else R.raw.partly_cloudy_night_snow
        181 -> if (isDay) R.raw.partly_cloudy_day_sleet else R.raw.partly_cloudy_night_sleet
        185 -> if (isDay) R.raw.partly_cloudy_day_drizzle else R.raw.partly_cloudy_night_drizzle
        200 -> if (isDay) R.raw.thunderstorms_day else R.raw.thunderstorms_night
        227 -> R.raw.wind_snow
        230 -> R.raw.extreme_snow
        248 -> if (isDay) R.raw.fog_day else R.raw.fog_night
        260 -> R.raw.extreme_fog
        263, 266 -> R.raw.drizzle
        281, 284 -> R.raw.extreme_drizzle
        293 -> if (isDay) R.raw.partly_cloudy_day_rain else R.raw.partly_cloudy_night_rain
        296, 299, 302 -> R.raw.rain
        305, 308 -> R.raw.extreme_rain
        311, 317 -> R.raw.sleet
        314, 320 -> R.raw.extreme_sleet
        323 -> if (isDay) R.raw.partly_cloudy_day_snow else R.raw.partly_cloudy_night_snow
        326, 329, 332 -> R.raw.snow
        335, 338 -> R.raw.extreme_snow
        350, 374 -> R.raw.hail
        353 -> if (isDay) R.raw.partly_cloudy_day_rain else R.raw.partly_cloudy_night_rain
        356, 359 -> R.raw.extreme_rain
        362 -> R.raw.sleet
        365 -> R.raw.extreme_sleet
        368 -> R.raw.snow
        371 -> R.raw.extreme_snow
        377 -> R.raw.extreme_hail
        386 -> if (isDay) R.raw.thunderstorms_day_rain else R.raw.thunderstorms_night_rain
        389 -> R.raw.thunderstorms_extreme_rain
        392 -> if (isDay) R.raw.thunderstorms_day_snow else R.raw.thunderstorms_night_snow
        395 -> R.raw.thunderstorms_extreme_snow
        else -> if (isDay) R.raw.clear_day else R.raw.clear_night
    }
}

@Composable
fun MiniTrendChart(trendColor: Color, points: List<Float> = emptyList()) {
    Canvas(modifier = Modifier.width(60.dp).height(20.dp)) {
        if (points.isEmpty()) {
            val path = Path().apply {
                moveTo(0f, size.height / 2)
                lineTo(size.width, size.height / 2)
            }
            drawPath(path = path, color = trendColor, style = Stroke(width = 3f))
            val fillPath = Path().apply {
                addPath(path)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(fillPath, brush = Brush.verticalGradient(listOf(trendColor.copy(alpha = 0.3f), Color.Transparent)))
        } else {
            val stepX = size.width / (points.size - 1).coerceAtLeast(1)
            val path = Path()
            var lastX = 0f
            var lastY = 0f
            
            points.forEachIndexed { index, value ->
                val x = index * stepX
                val y = size.height - (value * size.height).coerceIn(0f, size.height)
                if (index == 0) path.moveTo(x, y)
                else path.lineTo(x, y)
                lastX = x
                lastY = y
            }
            
            drawPath(path = path, color = trendColor, style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            
            val fillPath = Path().apply {
                addPath(path)
                lineTo(lastX, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(fillPath, brush = Brush.verticalGradient(listOf(trendColor.copy(alpha = 0.3f), Color.Transparent)))
        }
    }
}

@Composable
fun WindCompassIcon(modifier: Modifier = Modifier, iconColor: Color) {
    Canvas(modifier = modifier) {
        drawCircle(color = iconColor.copy(0.2f), style = Stroke(width = 4f))
        drawCircle(
            color = iconColor, radius = 4f, center = Offset(size.width / 2, 4f)
        )
    }
}

@Composable
fun PressureArcIcon(modifier: Modifier = Modifier, pressureMb: Float, iconColor: Color) {
    Canvas(modifier = modifier) {
        drawArc(
            color = iconColor.copy(0.2f),
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )
        val sweep = ((pressureMb - 950f) / 100f).coerceIn(0f, 1f) * 270f
        drawArc(
            color = iconColor,
            startAngle = 135f,
            sweepAngle = if (sweep > 0) sweep else 100f,
            useCenter = false,
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun SunPathGraphic(modifier: Modifier = Modifier, pathColor: Color, sunColor: Color) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(0f, size.height)
            quadraticTo(size.width / 2, -size.height, size.width, size.height)
        }
        drawPath(path, color = pathColor, style = Stroke(width = 4f))
        drawCircle(
            color = sunColor, radius = 12f, center = Offset(size.width / 2, 0f)
        )
    }
}

fun getAqiColor(index: Int) = when {
    index <= 2 -> GoodColor
    index <= 4 -> WarningColor
    else -> DangerColor
}

fun getUvColor(index: Float) = when {
    index >= 6f -> DangerColor
    index >= 3f -> WarningColor
    else -> GoodColor
}

fun getUvSubtitle(index: Float) = when {
    index >= 6f -> "Mức độ cao"
    index >= 3f -> "Mức trung bình"
    else -> "Mức độ thấp"
}

fun getHumiditySubtitle(humidity: Int) = when {
    humidity >= 70 -> "Mức ẩm cao, oi bức"
    humidity >= 40 -> "Mức độ thoải mái"
    else -> "Không khí hanh khô"
}

fun getDewPointSubtitle(dewPoint: Number) = when {
    dewPoint.toDouble() > 20.0 -> "Cảm giác ngột ngạt"
    else -> "Khô ráo, thoải mái"
}

fun getVisibilitySubtitle(visibility: Number) = when {
    visibility.toDouble() >= 10.0 -> "Trời quang, tầm nhìn tốt"
    else -> "Tầm nhìn hạn chế"
}

fun String.cleanLocationName(): String {
    return try {
        if (this.contains("Ã") || this.contains("Ä")) {
            String(this.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8).removeAccents()
        } else {
            this.removeAccents()
        }
    } catch (_: Exception) {
        this.removeAccents()
    }
}