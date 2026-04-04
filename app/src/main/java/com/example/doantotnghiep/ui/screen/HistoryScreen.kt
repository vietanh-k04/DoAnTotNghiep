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
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.doantotnghiep.data.local.enum.AlertLevel
import com.example.doantotnghiep.data.remote.LogUiModel
import com.example.doantotnghiep.data.remote.StationConfig
import com.example.doantotnghiep.ui.theme.GlassBg
import com.example.doantotnghiep.ui.theme.SoftBgBottom
import com.example.doantotnghiep.ui.theme.SoftBgTop
import com.example.doantotnghiep.ui.theme.TextDim
import com.example.doantotnghiep.ui.theme.TextWhite
import com.example.doantotnghiep.ui.viewmodel.HistoryViewModel

private const val TAG = "HistoryScreen"

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SoftBgTop, SoftBgBottom)))
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFF0EA5E9)
            )
        } else if (state.error != null) {
            Text(
                text = state.error ?: "Đã có lỗi xảy ra",
                color = Color.Red,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.stations.forEach { station ->
                        val isSelected = station.id == state.selectedStation?.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color(0xFF0EA5E9) else GlassBg)
                                .clickable { viewModel.selectStation(station) }
                        ) {
                            Text(
                                text = station.name ?: "Không xác định",
                                color = if (isSelected) Color.White else TextDim,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                HistoryChartCard(
                    selectedTimeRange = state.selectedTimeRange,
                    selectedStation = state.selectedStation,
                    logs = state.logs,
                    onTimeRangeSelected = { viewModel.selectTimeRange(it) }
                )

                AlertsHistorySection(state.logs)
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(GlassBg)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            val timeRanges = listOf("1 Ngày", "3 Ngày", "7 Ngày")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassBg, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                timeRanges.forEach { time ->
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
                            color = if (isSelected) Color(0xFF0EA5E9) else TextDim,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val latestVal = logs.firstOrNull()?.distanceRaw?.toInt()?.toString() ?: "--"
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$latestVal cm",
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
                        Icons.Rounded.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "Latest",
                        color = Color(0xFF10B981),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                "KHOẢNG CÁCH GẦN NHẤT ${selectedStation?.name?.let { "- $it" } ?: ""}",
                color = TextDim,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    val defaultPoints = listOf(0.5f, 0.5f)
                    val points = if (logs.size >= 2) {
                        val distances = logs.take(10).reversed().map { it.distanceRaw }
                        val maxVal = distances.maxOrNull() ?: 1f
                        val minVal = distances.minOrNull() ?: 0f
                        val range = if (maxVal == minVal) 1f else (maxVal - minVal)
                        distances.map { ((it - minVal) / range).coerceIn(0.1f, 0.9f) }
                    } else {
                        defaultPoints
                    }

                    val stepX = width / (points.size - 1)

                    val path = Path()
                    var lastX = 0f
                    var lastY = height - (points[0] * height)

                    path.moveTo(lastX, lastY)

                    for (i in 1 until points.size) {
                        val currentX = i * stepX
                        val currentY = height - (points[i] * height)

                        val controlPoint1X = (lastX + currentX) / 2
                        val controlPoint1Y = lastY
                        val controlPoint2X = (lastX + currentX) / 2
                        val controlPoint2Y = currentY

                        path.cubicTo(
                            controlPoint1X,
                            controlPoint1Y,
                            controlPoint2X,
                            controlPoint2Y,
                            currentX,
                            currentY
                        )

                        lastX = currentX
                        lastY = currentY
                    }

                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0EA5E9).copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            startY = 0f, endY = height
                        )
                    )

                    drawPath(
                        path = path,
                        color = Color(0xFF007AFF),
                        style = Stroke(width = 8f, cap = StrokeCap.Round)
                    )

                    // Đánh dấu điểm cao nhất trên biểu đồ
                    val maxPointIndex = points.indexOf(points.maxOrNull() ?: 0f)
                    val maxPointX = maxPointIndex * stepX
                    val maxPointY = height - (points[maxPointIndex] * height)

                    drawCircle(
                        color = Color(0xFF0EA5E9).copy(alpha = 0.3f),
                        radius = 24f,
                        center = Offset(maxPointX, maxPointY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 12f,
                        center = Offset(maxPointX, maxPointY)
                    )
                    drawCircle(
                        color = Color(0xFF0EA5E9),
                        radius = 8f,
                        center = Offset(maxPointX, maxPointY)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val timeLabels = listOf("Cũ nhất", "", "", "", "Mới nhất")
                timeLabels.forEach { time ->
                    Text(
                        text = time,
                        fontSize = 11.sp,
                        color = TextDim,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun AlertsHistorySection(alerts: List<LogUiModel>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Lịch sử hoạt động",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Text(
                text = "Xem Tất Cả",
                color = Color(0xFF0EA5E9),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { /* Mở màn hình full lịch sử */ }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (alerts.isEmpty()) {
            Text(
                "Chưa có dữ liệu", 
                color = TextDim, 
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                alerts.forEach { alert ->
                    AlertHistoryItemCard(alert)
                }
            }
        }
    }
}

@Composable
fun AlertHistoryItemCard(alert: LogUiModel) {
    val tintColor = when (alert.level) {
        AlertLevel.CRITICAL -> Color(0xFFEF4444)
        AlertLevel.WARNING -> Color(0xFFF59E0B)
        AlertLevel.SAFE -> Color(0xFF10B981)
    }
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(alert.description, color = TextDim, fontSize = 12.sp)
                Text("Khoảng cách: ${alert.distanceRaw.toInt()}cm", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

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