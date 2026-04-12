package com.example.doantotnghiep.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.doantotnghiep.MOCK_AI_CONFIDENCE_FLOAT
import com.example.doantotnghiep.MOCK_AI_CONFIDENCE_PERCENT
import com.example.doantotnghiep.R
import com.example.doantotnghiep.TIME_FRAMES
import com.example.doantotnghiep.data.local.AiPrediction
import com.example.doantotnghiep.ui.theme.BlueRecorded
import com.example.doantotnghiep.ui.theme.GlassBg
import com.example.doantotnghiep.ui.theme.OrangePredicted
import com.example.doantotnghiep.ui.theme.RedDanger
import com.example.doantotnghiep.ui.theme.TextDim
import com.example.doantotnghiep.ui.theme.TextWhite
import com.example.doantotnghiep.ui.viewmodel.AnalyticUiState
import com.example.doantotnghiep.ui.viewmodel.AnalyticViewModel
import com.example.doantotnghiep.utils.appBackground
import com.example.doantotnghiep.utils.getTrendStatusText
import java.util.Locale

private const val TAG = "AnalyticScreen"

@Composable
fun AnalyticScreen(viewModel: AnalyticViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .appBackground()
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = BlueRecorded
            )
        } else if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = RedDanger,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(20.dp),
                textAlign = TextAlign.Center
            )
        } else {
            AnalyticContent(uiState, onTimeSelected = { viewModel.setTimeFrame(it) })
        }
    }
}

@Composable
private fun AnalyticContent(uiState: AnalyticUiState, onTimeSelected: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (!uiState.isStationActive) Modifier.blur(12.dp) else Modifier)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            TimeSelectorRow(uiState.selectedTime, onTimeSelected)
            ForecastChartCard(uiState)
            AiConfidenceSimpleCard()
            AiPredictionsSection(uiState.predictions, uiState.selectedTime)
        }

        if (!uiState.isStationActive) {
            InactiveStationOverlay()
        }
    }
}

@Composable
private fun InactiveStationOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f)
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                Icons.Rounded.WarningAmber,
                contentDescription = null,
                tint = OrangePredicted,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.INACTIVE_STATION_WARNING),
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TimeSelectorRow(selectedTime: String, onTimeSelected: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TIME_FRAMES.forEach { time ->
            val isSelected = time == selectedTime
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) BlueRecorded else GlassBg)
                    .clickable { onTimeSelected(time) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = time,
                    color = if (isSelected) Color.White else TextDim,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ForecastChartCard(uiState: AnalyticUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(GlassBg)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            ChartHeaderInfo(uiState)
            Spacer(modifier = Modifier.height(30.dp))
            
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                WaterLevelLineChart(uiState)
                
                val floodStageY = 180.dp * (1f - uiState.dangerThresholdPercent)
                Text(
                    text = String.format(Locale.getDefault(), stringResource(R.string.ALARM_LEVEL_FORMAT), uiState.dangerThreshold),
                    color = RedDanger, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = floodStageY - 10.dp)
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(4.dp))
                        .background(GlassBg)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.PAST), fontSize = 12.sp, color = TextDim)
                Text(stringResource(R.string.PRESENT), fontSize = 12.sp, color = BlueRecorded, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.FUTURE), fontSize = 12.sp, color = TextDim)
            }
        }
    }
}

@Composable
private fun ChartHeaderInfo(uiState: AnalyticUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(stringResource(R.string.water_forecast), color = TextDim, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    String.format(Locale.US, "%.1fm", uiState.currentWaterLevel),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.width(12.dp))
                
                val statusColor = if (uiState.isIncreasing) OrangePredicted else BlueRecorded
                val statusText = getTrendStatusText(uiState.isIncreasing, uiState.isDecreasing)
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ShowChart,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(statusText, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(width = 16.dp, height = 3.dp).background(BlueRecorded))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.RECORDED), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextDim)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(width = 16.dp, height = 3.dp)) {
                    drawLine(
                        color = OrangePredicted,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = 4f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.AI_PREDICTION), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextDim)
            }
        }
    }
}

@Composable
private fun WaterLevelLineChart(uiState: AnalyticUiState, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val recordedPoints = uiState.recordedPoints
        val predictedPoints = uiState.predictedPoints
        val totalPoints = recordedPoints.size + predictedPoints.size - 1
        val stepX = if (totalPoints > 1) width / (totalPoints - 1) else width

        val floodStageY = height * (1f - uiState.dangerThresholdPercent)
        drawLine(
            color = RedDanger.copy(alpha = 0.5f),
            start = Offset(0f, floodStageY),
            end = Offset(width, floodStageY),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
        )

        val recordedPath = Path()
        var lastX = 0f
        var lastY = 0f

        if (recordedPoints.isNotEmpty()) {
            recordedPoints.forEachIndexed { index, value ->
                val x = index * stepX
                val y = height - (value * height).coerceIn(0f, height)
                if (index == 0) recordedPath.moveTo(x, y)
                else recordedPath.lineTo(x, y)
                lastX = x
                lastY = y
            }
            drawPath(
                path = recordedPath,
                color = BlueRecorded,
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )

            val fillPath = Path().apply {
                addPath(recordedPath)
                lineTo(lastX, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(BlueRecorded.copy(alpha = 0.3f), Color.Transparent),
                    startY = 0f, endY = height
                )
            )
        }

        if (predictedPoints.isNotEmpty() && recordedPoints.isNotEmpty()) {
            val predictedPath = Path()
            predictedPath.moveTo(lastX, lastY)

            predictedPoints.drop(1).forEachIndexed { index, value ->
                val x = lastX + (index + 1) * stepX
                val y = height - (value * height).coerceIn(0f, height)
                predictedPath.lineTo(x, y)
            }
            drawPath(
                path = predictedPath,
                color = OrangePredicted,
                style = Stroke(
                    width = 8f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
                )
            )
        }

        drawCircle(color = Color.White, radius = 16f, center = Offset(lastX, lastY))
        drawCircle(color = BlueRecorded, radius = 10f, center = Offset(lastX, lastY))
    }
}

@Composable
fun AiConfidenceSimpleCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassBg)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.AI_CONFIDENCE), fontWeight = FontWeight.Bold, color = TextWhite)
                Text(MOCK_AI_CONFIDENCE_PERCENT, fontWeight = FontWeight.ExtraBold, color = BlueRecorded)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { MOCK_AI_CONFIDENCE_FLOAT },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = BlueRecorded,
                trackColor = Color.White.copy(alpha = 0.2f),
            )
        }
    }
}

@Composable
fun AiPredictionsSection(predictions: List<AiPrediction>, selectedTime: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.AI_PREDICTIONS_TITLE), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            
            val timeText = selectedTime.replace("h", " GIỜ").uppercase(Locale.getDefault())
            Text(String.format(Locale.getDefault(), stringResource(R.string.HOURS_AHEAD_FORMAT), timeText), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDim)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            predictions.forEach { item ->
                PredictionItemCard(item)
            }
        }
    }
}

@Composable
fun PredictionItemCard(item: AiPrediction) {
    val borderColor = if (item.isPeak) item.color.copy(alpha = 0.4f) else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassBg)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(8.dp).background(item.color, CircleShape))

            Spacer(modifier = Modifier.width(16.dp))

            Text(item.time, fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 16.sp)

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "${item.level}cm",
                fontWeight = if (item.isPeak) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (item.isPeak) item.color else TextWhite,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(item.color.copy(alpha = 0.1f))
                    .border(BorderStroke(1.dp, item.color.copy(alpha = 0.2f)), RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = item.status,
                    color = item.color,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}