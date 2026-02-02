package com.example.doantotnghiep.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import com.example.doantotnghiep.R
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doantotnghiep.data.model.SensorData
import com.example.doantotnghiep.ui.theme.*

@Composable
fun WaveCard(waterLevel: Double, maxHeight: Double, status: String, lastUpdate: String) {
    val percent = (waterLevel / maxHeight).toFloat().coerceIn(0f, 1f)

    val isDanger = percent > 0.7f
    val statusColor = if (isDanger) StatusDanger else StatusSuccess
    val statusIcon = if(isDanger) Icons.Default.Warning else Icons.Default.CheckCircle

    val waveColor = if (isDanger) StatusDanger else WaterBlue

    Card(
        modifier = Modifier.fillMaxSize().height(350.dp).padding(16.dp).shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(contentColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            WaveAnimation(modifier = Modifier.fillMaxSize(), waterLevel = percent, waveColor = waveColor)

            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.dashboard_current_water_level), color = TextSecondaryLight, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(stringResource(R.string.dashboard_value_current_water_level, waterLevel), color = TextPrimaryLight, fontSize = 64.sp, fontWeight = FontWeight.ExtraBold)

                Surface(
                    color = StatusSuccess.copy(0.2f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
                        Text(stringResource(R.string.dashboard_status, status), color = statusColor, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 4.dp), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Row(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(stringResource(R.string.dashboard_trend), style = MaterialTheme.typography.labelMedium, color = TextSecondaryLight)
                    Text(status, style = MaterialTheme.typography.bodyMedium, color = TextPrimaryLight)
                }

                Spacer(Modifier.weight(1f))

                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.dashboard_last_updated, lastUpdate), style = MaterialTheme.typography.labelMedium, color = TextSecondaryLight)
                    Text(lastUpdate, style = MaterialTheme.typography.bodyMedium, color = TextPrimaryLight)
                }

            }
        }
    }
}

@Composable
fun HybridBadge(isLocal: Boolean) {
    val backgroundColor = if (isLocal) LocalBackground else AreaBackground
    val contentColor = if (isLocal) LocalContent else AreaContent
    val text = if (isLocal) stringResource(R.string.dashboard_local) else stringResource(R.string.dashboard_area)
    val icon = if (isLocal) Icons.Default.GpsFixed else Icons.Default.Public

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = contentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun EnvironmentMetric(icon: ImageVector, label: String, value: String, unit: String) {
    Card(
        modifier = Modifier.padding(8.dp).fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color = BorderLight),
        colors = CardDefaults.cardColors(contentColor = SurfaceLight)
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, label, tint = IconPrimary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value + unit, color = TextPrimaryLight, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(label, color = TextSecondaryLight, fontSize = 12.sp)
        }
    }
}

@Composable
fun EnvironmentSection(sensorData: SensorData?) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.dashboard_environment),
                color = TextPrimaryLight,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            TextButton(onClick = { }) {
                Text("View History", color = TextSelected)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.height(240.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                EnvironmentMetric(icon = Icons.Default.Thermostat, label = stringResource(R.string.dashboard_temperature), value = "${sensorData?.temp ?: "--"}", unit = stringResource(R.string.dashboard_temperature_unit))
                EnvironmentMetric(icon = Icons.Default.WaterDrop, label = stringResource(R.string.dashboard_humidity), value = "${sensorData?.humid ?: "--"}", unit = stringResource(R.string.dashboard_humidity_unit))
                EnvironmentMetric(icon = Icons.Default.Cloud, label = stringResource(R.string.dashboard_precipitation), value = "${sensorData?.rainVal ?: "--"}", unit = stringResource(R.string.dashboard_precipitation_unit))
                EnvironmentMetric(icon = Icons.Default.BatteryChargingFull, label = stringResource(R.string.dashboard_battery), value = "98", unit = stringResource(R.string.dashboard_humidity_unit))

            }
        }
    }
}
