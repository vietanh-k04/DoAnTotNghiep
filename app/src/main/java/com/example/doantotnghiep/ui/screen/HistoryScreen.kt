package com.example.doantotnghiep.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.doantotnghiep.DEFAULT_STATION_HEIGHT
import com.example.doantotnghiep.INACTIVE_TIMEOUT_MS
import com.example.doantotnghiep.MAX_CHART_POINTS
import com.example.doantotnghiep.R
import com.example.doantotnghiep.TIME_RANGES
import com.example.doantotnghiep.data.local.enum.AlertLevel
import com.example.doantotnghiep.data.local.state.HistoryScreenState
import com.example.doantotnghiep.data.local.state.LogUiModel
import com.example.doantotnghiep.data.remote.StationConfig
import com.example.doantotnghiep.ui.theme.GlassBg
import com.example.doantotnghiep.ui.theme.StatusDanger
import com.example.doantotnghiep.ui.theme.StatusSuccess
import com.example.doantotnghiep.ui.theme.StatusWarning
import com.example.doantotnghiep.ui.theme.TextDim
import com.example.doantotnghiep.ui.theme.TextWhite
import com.example.doantotnghiep.ui.theme.WaterBlue
import com.example.doantotnghiep.ui.viewmodel.HistoryViewModel
import com.example.doantotnghiep.utils.appBackground
import com.example.doantotnghiep.utils.getRainStatus
import com.example.doantotnghiep.utils.getTrendInfo
import java.util.Locale

private const val TAG = "HistoryScreen"

@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .appBackground()
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = WaterBlue
                )
            }
            state.error != null -> {
                Text(
                    text = state.error ?: stringResource(R.string.ERROR_OCCURRED),
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                HistoryContent(
                    state = state,
                    onStationSelected = { viewModel.selectStation(it) },
                    onTimeRangeSelected = { viewModel.selectTimeRange(it) }
                )
            }
        }
    }
}

@Composable
private fun HistoryContent(
    state: HistoryScreenState,
    onStationSelected: (StationConfig) -> Unit,
    onTimeRangeSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        StationTabsRow(
            stations = state.stations,
            selectedStationId = state.selectedStation?.id,
            onStationSelected = onStationSelected
        )

        HistoryChartCard(
            selectedTimeRange = state.selectedTimeRange,
            selectedStation = state.selectedStation,
            logs = state.logs,
            onTimeRangeSelected = onTimeRangeSelected
        )

        AlertsHistorySection(state.logs, state.selectedStation)
    }
}

@Composable
private fun StationTabsRow(
    stations: List<StationConfig>,
    selectedStationId: String?,
    onStationSelected: (StationConfig) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        stations.forEach { station ->
            val isSelected = station.id == selectedStationId
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) WaterBlue else GlassBg)
                    .clickable { onStationSelected(station) }
            ) {
                Text(
                    text = station.name ?: stringResource(R.string.UNKNOWN),
                    color = if (isSelected) Color.White else TextDim,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun HistoryChartCard(
    selectedTimeRange: String,
    selectedStation: StationConfig?,
    logs: List<LogUiModel>,
    onTimeRangeSelected: (String) -> Unit
) {
    val nowMillis = System.currentTimeMillis()
    val latestLogTime = logs.firstOrNull()?.timestamp ?: 0L
    val isInactive = latestLogTime > 0 && (nowMillis - latestLogTime) > INACTIVE_TIMEOUT_MS

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(GlassBg)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            TimeRangeSelectorRow(selectedTimeRange, onTimeRangeSelected)

            Spacer(modifier = Modifier.height(24.dp))

            TrendHeader(selectedStation, logs, isInactive)

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                if (logs.isNotEmpty()) {
                    HistoryWaterLevelChart(logs, selectedStation, isInactive)
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            stringResource(R.string.NO_DATA_IN_RANGE),
                            color = TextDim,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ChartFooter(logs, isInactive, latestLogTime)
        }
    }
}

@Composable
private fun TimeRangeSelectorRow(selectedTimeRange: String, onTimeRangeSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlassBg, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TIME_RANGES.forEach { time ->
            val isSelected = time == selectedTimeRange
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) GlassBg else Color.Transparent)
                    .clickable { onTimeRangeSelected(time) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = time,
                    color = if (isSelected) WaterBlue else TextDim,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun TrendHeader(
    selectedStation: StationConfig?,
    logs: List<LogUiModel>,
    isInactive: Boolean
) {
    val stationHeight = selectedStation?.calibrationOffset?.toFloat() ?: DEFAULT_STATION_HEIGHT

    val latestDistance = logs.firstOrNull()?.distanceRaw
    val previousDistance = logs.getOrNull(1)?.distanceRaw

    val latestWaterLevel = latestDistance?.let { (stationHeight - it).coerceAtLeast(0f).toInt() }
    val previousWaterLevel = previousDistance?.let { (stationHeight - it).coerceAtLeast(0f).toInt() }

    val (trendText, trendColor, trendIcon) = getTrendInfo(latestWaterLevel, previousWaterLevel, isInactive)

    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = "${latestWaterLevel ?: "--"} cm",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextWhite
        )
        Spacer(modifier = Modifier.width(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                trendIcon,
                contentDescription = null,
                tint = trendColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                trendText,
                color = trendColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
    
    val stationNameSuffix = selectedStation?.name?.let { "- $it" } ?: ""
    val titleText = if (isInactive) "${stringResource(R.string.LAST_RECORDED_LEVEL)} $stationNameSuffix"
                    else "${stringResource(R.string.CURRENT_LEVEL)} $stationNameSuffix"
    
    Text(
        titleText,
        color = TextDim,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun HistoryWaterLevelChart(
    logs: List<LogUiModel>,
    selectedStation: StationConfig?,
    isInactive: Boolean
) {
    val stationHeight = selectedStation?.calibrationOffset?.toFloat() ?: DEFAULT_STATION_HEIGHT

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        val dataAreaWidth = if (isInactive) width * 0.75f else width

        val maxPoints = MAX_CHART_POINTS
        val sampledLogs = if (logs.size > maxPoints) {
            val step = logs.size.toDouble() / maxPoints
            (0 until maxPoints).map { i -> logs[(i * step).toInt()] }
        } else {
            logs
        }
        
        val validLogs = sampledLogs.reversed()
        
        val actualStartTime = logs.lastOrNull()?.timestamp ?: 0L
        val actualEndTime = logs.firstOrNull()?.timestamp ?: 0L
        val timeSpan = actualEndTime - actualStartTime

        val path = Path()
        var lastX = 0f
        var lastY = 0f
        var latestPointX = 0f
        var latestPointY = 0f
        var firstValidPoint = true
        var firstX = 0f

        validLogs.forEachIndexed { index, log ->
            val t = log.timestamp
            val x = if (timeSpan > 0) {
                val ratio = (t - actualStartTime).toFloat() / timeSpan.toFloat()
                (ratio * dataAreaWidth).coerceIn(0f, dataAreaWidth)
            } else {
                (index.toFloat() / (validLogs.size - 1).coerceAtLeast(1)) * dataAreaWidth
            }

            val waterLevel = (stationHeight - log.distanceRaw).coerceAtLeast(0f)
            val normalized = if (stationHeight > 0) waterLevel / stationHeight else 0f
            val y = height - (normalized.coerceIn(0.1f, 0.9f) * height)

            if (firstValidPoint) {
                path.moveTo(x, y)
                lastX = x
                lastY = y
                firstX = x
                firstValidPoint = false
            } else {
                val controlPoint1X = (lastX + x) / 2
                val controlPoint1Y = lastY
                val controlPoint2X = (lastX + x) / 2
                val controlPoint2Y = y
                path.cubicTo(
                    controlPoint1X, controlPoint1Y,
                    controlPoint2X, controlPoint2Y,
                    x, y
                )
                lastX = x
                lastY = y
            }
            latestPointX = x
            latestPointY = y
        }

        val fillPath = Path().apply {
            addPath(path)
            lineTo(latestPointX, height)
            lineTo(firstX, height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    WaterBlue.copy(alpha = 0.3f),
                    Color.Transparent
                ),
                startY = 0f, endY = height
            )
        )

        if (isInactive && latestPointX < width) {
            val dashedLine = Path().apply {
                moveTo(latestPointX, height)
                lineTo(width, height)
            }
            drawPath(
                path = dashedLine,
                color = Color.Gray.copy(alpha = 0.5f),
                style = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )
        }

        drawPath(
            path = path,
            color = WaterBlue,
            style = Stroke(width = 8f, cap = StrokeCap.Round)
        )

        drawCircle(
            color = WaterBlue.copy(alpha = 0.3f),
            radius = 24f,
            center = Offset(latestPointX, latestPointY)
        )
        drawCircle(
            color = Color.White,
            radius = 12f,
            center = Offset(latestPointX, latestPointY)
        )
        drawCircle(
            color = WaterBlue,
            radius = 8f,
            center = Offset(latestPointX, latestPointY)
        )
    }
}

@Composable
private fun ChartFooter(logs: List<LogUiModel>, isInactive: Boolean, latestLogTime: Long) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (logs.isNotEmpty()) {
            val oldest = logs.last()
            val newest = logs.first()

            Column(
                modifier = Modifier.align(Alignment.CenterStart),
                horizontalAlignment = Alignment.Start
            ) {
                Text(stringResource(R.string.OLDEST), fontSize = 11.sp, color = TextDim, fontWeight = FontWeight.SemiBold)
                Text(oldest.time, fontSize = 10.sp, color = TextDim)
            }

            if (isInactive && latestLogTime > 0) {
                Column(
                    modifier = Modifier.align(BiasAlignment(0.5f, 0f)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(R.string.STOPPED), fontSize = 11.sp, color = StatusDanger, fontWeight = FontWeight.SemiBold)
                    Text(newest.time, fontSize = 10.sp, color = StatusDanger)
                }

                Column(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(stringResource(R.string.PRESENT), fontSize = 11.sp, color = TextDim, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.NOW), fontSize = 10.sp, color = TextDim)
                }
            } else {
                Column(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(stringResource(R.string.NEWEST), fontSize = 11.sp, color = TextDim, fontWeight = FontWeight.SemiBold)
                    Text(newest.time, fontSize = 10.sp, color = TextDim)
                }
            }
        } else {
            Text(
                stringResource(R.string.NO_DATA),
                fontSize = 11.sp, 
                color = TextDim, 
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }
    }
}

@Composable
fun AlertsHistorySection(alerts: List<LogUiModel>, selectedStation: StationConfig?) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.HISTORY_ACTIVITY),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (alerts.isEmpty()) {
            Text(
                stringResource(R.string.NO_DATA),
                color = TextDim, 
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                alerts.take(15).forEach { alert ->
                    AlertHistoryItemCard(alert, selectedStation)
                }
            }
        }
    }
}

@Composable
fun AlertHistoryItemCard(alert: LogUiModel, selectedStation: StationConfig?) {
    val tintColor = when (alert.level) {
        AlertLevel.CRITICAL -> StatusDanger
        AlertLevel.WARNING -> StatusWarning
        AlertLevel.SAFE -> StatusSuccess
    }

    val statusRain = getRainStatus(alert.rainVal.toInt())
    val icon = when (alert.level) {
        AlertLevel.SAFE -> Icons.Default.CheckCircle
        else -> Icons.Default.WarningAmber
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassBg)
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(tintColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    alert.title,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.TEMP_LABEL), color = TextDim, fontSize = 12.sp)
                    Text("${alert.temp}°C", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.HUMID_LABEL), color = TextDim, fontSize = 12.sp)
                    Text("${alert.humid}%", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.RAIN_LABEL), color = TextDim, fontSize = 12.sp)
                    Text(statusRain.second, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))

                val stationHeight = selectedStation?.calibrationOffset?.toFloat() ?: DEFAULT_STATION_HEIGHT
                val waterLevel = (stationHeight - alert.distanceRaw).coerceAtLeast(0f)
                
                Text(
                    String.format(Locale.getDefault(), stringResource(R.string.WATER_LEVEL_FORMAT), waterLevel.toInt()),
                    color = tintColor, 
                    fontSize = 13.sp, 
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    alert.date,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(alert.time, color = TextDim, fontSize = 12.sp)
            }
        }
    }
}