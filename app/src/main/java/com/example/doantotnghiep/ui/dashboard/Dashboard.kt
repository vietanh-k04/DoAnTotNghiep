package com.example.doantotnghiep.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doantotnghiep.R
import com.example.doantotnghiep.data.remote.SensorData
import com.example.doantotnghiep.data.local.WaveCardUiModel
import com.example.doantotnghiep.ui.theme.*
import com.example.doantotnghiep.utils.*

@Composable
fun WaveCard(data: WaveCardUiModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GlassBg)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.ic_ocean_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().alpha(0.4f),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
            )

            WaveAnimation(modifier = Modifier.fillMaxSize(), waterLevel = data.waterPercent, waveColor = data.waveColor)

            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.dashboard_current_water_level), color = TextDim, fontSize = 24.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.padding(bottom = 10.dp))
                Text(stringResource(R.string.dashboard_value_current_water_level, data.waterLevel), color = TextWhite, fontSize = 64.sp, fontWeight = FontWeight.ExtraBold)

                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassBg)
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(data.statusIcon, contentDescription = null, tint = data.statusColor, modifier = Modifier.size(16.dp))
                        Text(stringResource(R.string.dashboard_status, stringResource(data.status)), color = data.statusColor, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 4.dp), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Row(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(stringResource(R.string.dashboard_trend), style = MaterialTheme.typography.labelMedium, color = TextDim, modifier = Modifier.padding(bottom = 5.dp))
                    Text(stringResource(data.trend), style = MaterialTheme.typography.bodyLarge, color = TextWhite, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.weight(1f))

                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.dashboard_last_updated), style = MaterialTheme.typography.labelMedium, color = TextDim, modifier = Modifier.padding(bottom = 5.dp))
                    Text(data.lastUpdated, style = MaterialTheme.typography.bodyLarge, color = TextWhite, fontWeight = FontWeight.Bold)
                }

            }
        }
    }
}

@Composable
fun HybridBadge(isLocal: Boolean) {
    val text = if (isLocal) stringResource(R.string.dashboard_local) else stringResource(R.string.dashboard_area)
    val icon = if (isLocal) Icons.Default.GpsFixed else Icons.Default.Public

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GlassBg)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextWhite,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = TextWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun EnvironmentMetric(icon: ImageVector, iconTint: Color, label: String, value: String, unit: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassBg)
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GlassBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(value + unit, color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(label, color = TextDim, fontSize = 12.sp)
        }
    }
}

@Composable
fun EnvironmentSection(sensorData: SensorData?) {
    val rainRaw = sensorData?.rainVal ?: 1024

    val rainInfor = getRainStatus(rainRaw)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.dashboard_environment),
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            TextButton(onClick = { }) {
                Text(stringResource(R.string.dashboard_history), color = TextWhite)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    EnvironmentMetric(
                        icon = Icons.Default.Thermostat,
                        iconTint = Orange,
                        label = stringResource(R.string.dashboard_temperature),
                        value = stringResource(R.string.dashboard_value, sensorData?.temp ?: ""),
                        unit = stringResource(R.string.dashboard_temperature_unit)
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    EnvironmentMetric(
                        icon = Icons.Default.WaterDrop,
                        iconTint = WaterBlue,
                        label = stringResource(R.string.dashboard_humidity),
                        value = stringResource(R.string.dashboard_value, sensorData?.humid ?: "--"),
                        unit = stringResource(R.string.dashboard_humidity_unit)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    EnvironmentMetric(
                        icon = rainInfor.first,
                        iconTint = rainInfor.third,
                        label = stringResource(R.string.dashboard_weather),
                        value = rainInfor.second,
                        unit = ""
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    EnvironmentMetric(
                        icon = Icons.Default.BatteryChargingFull,
                        iconTint = StatusSuccess,
                        label = stringResource(R.string.dashboard_battery),
                        value = "98",
                        unit = stringResource(R.string.dashboard_humidity_unit)
                    )
                }
            }
        }
    }
}
