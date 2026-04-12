package com.example.doantotnghiep.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doantotnghiep.R
import com.example.doantotnghiep.data.local.StationMapUiModel
import com.example.doantotnghiep.ui.theme.GlassBg
import com.example.doantotnghiep.ui.theme.Orange
import com.example.doantotnghiep.ui.theme.TextDim
import com.example.doantotnghiep.ui.theme.TextWhite
import com.example.doantotnghiep.ui.theme.WaterBlue
import com.example.doantotnghiep.utils.getRainStatus
import com.example.doantotnghiep.utils.statusColor
import com.example.doantotnghiep.utils.statusIcon
import com.example.doantotnghiep.utils.statusText

@Composable
fun StationDetailContent(station: StationMapUiModel, onSettingClick: () -> Unit, onHistoryClick: () -> Unit) {
    val statusColor = statusColor(station.status)
    val rainInfor = getRainStatus(station.sensorData.rainVal ?: 0)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).padding(bottom = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = station.stationConfig.name ?: "", fontSize = 24.sp, color = TextWhite, fontWeight = FontWeight.Bold)

            Surface(
                color = GlassBg,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = statusIcon(station.status), contentDescription = null, tint = statusColor, modifier = Modifier.size(14.dp))

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(text = statusText(station.status), color = statusColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = TextDim, modifier = Modifier.size(16.dp))

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = stringResource(R.string.map_location, station.stationConfig.latitude ?: 0.0, station.stationConfig.longitude ?: 0.0),
                color = TextDim,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailMetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.WaterDrop,
                iconColor = WaterBlue,
                value = stringResource(R.string.map_value_currentLevel, station.sensorData.distanceRaw ?: 0.0),
                label = stringResource(R.string.map_current_level)
            )

            DetailMetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Thermostat,
                iconColor = Orange,
                value = stringResource(R.string.map_temperature, station.sensorData.temp ?: 0.0),
                label = stringResource(R.string.dashboard_temperature)
            )

            DetailMetricCard(
                modifier = Modifier.weight(1f),
                icon = rainInfor.first,
                iconColor = rainInfor.third,
                value = rainInfor.second,
                label = stringResource(R.string.dashboard_weather)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onHistoryClick,
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WaterBlue),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(text = stringResource(R.string.map_view_history), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)

                Spacer(modifier = Modifier.width(8.dp))

                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassBg)
                    .clickable { onSettingClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = TextWhite)
            }
        }
    }
}

@Composable
fun DetailMetricCard(
    modifier: Modifier,
    icon: ImageVector,
    iconColor: Color,
    value: String,
    label: String
) {
    Box(
        modifier = modifier
            .height(130.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(GlassBg)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(36.dp).background(iconColor.copy(0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(value, color = TextWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(label, color = TextDim, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
