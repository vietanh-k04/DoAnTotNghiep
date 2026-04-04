package com.example.doantotnghiep.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.doantotnghiep.R
import com.example.doantotnghiep.data.local.WaveCardUiModel
import com.example.doantotnghiep.data.local.enum.Status
import com.example.doantotnghiep.data.local.enum.Trend
import com.example.doantotnghiep.data.local.state.HomeUiState
import com.example.doantotnghiep.ui.theme.StatusDanger
import com.example.doantotnghiep.ui.theme.StatusSuccess
import com.example.doantotnghiep.ui.theme.StatusWarning
import com.example.doantotnghiep.ui.theme.TextSelected
import com.example.doantotnghiep.ui.theme.WaterBlue
import com.example.doantotnghiep.ui.theme.Yellow

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
        else -> StatusSuccess
    }
}

fun trendColor(trend: Trend) : Color {
    return when(trend) {
        Trend.RISING -> StatusDanger
        else -> StatusSuccess
    }
}

@Composable
fun trendText(trend: Trend) : String {
    return when(trend) {
        Trend.RISING -> stringResource(R.string.map_rising)
        else -> stringResource(R.string.map_stable)
    }
}

@Composable
fun statusText(status: Status) : String {
    return when(status) {
        Status.DANGER -> stringResource(R.string.status_danger)
        Status.WARNING  -> stringResource(R.string.status_warning)
        else -> stringResource(R.string.status_safe)
    }
}

@Composable
fun statusIcon(status: Status) : ImageVector {
    return when (status) {
        Status.DANGER -> Icons.Default.Dangerous
        Status.WARNING -> Icons.Default.Warning
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