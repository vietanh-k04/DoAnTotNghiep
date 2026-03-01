package com.example.doantotnghiep.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.doantotnghiep.R
import com.example.doantotnghiep.data.local.*
import com.example.doantotnghiep.ui.theme.*

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