package com.example.doantotnghiep.ui.map

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.rounded.Dangerous
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doantotnghiep.R
import com.example.doantotnghiep.data.local.StationMapUiModel
import com.example.doantotnghiep.ui.theme.BrightGray
import com.example.doantotnghiep.ui.theme.DarkGunmetal
import com.example.doantotnghiep.ui.theme.GhostWhite
import com.example.doantotnghiep.ui.theme.StatusDanger
import com.example.doantotnghiep.ui.theme.StatusWarning
import com.example.doantotnghiep.ui.theme.VividBlue

@Composable
fun StationSettingContent(
    station: StationMapUiModel,
    onDismiss: () -> Unit,
    onSave: (String, Float, Float, Float) -> Unit,
    onRequestLocationUpdate: () -> Unit
) {
    var nameValue by remember(station.stationConfig.id) {
        mutableStateOf(station.stationConfig.name ?: "")
    }

    var offsetValue by remember(station.stationConfig.id) {
        mutableStateOf(station.stationConfig.calibrationOffset ?: 0)
    }

    var warningValue by remember(station.stationConfig.id) {
        mutableStateOf(station.stationConfig.warningThreshold?.toFloat() ?: 0f)
    }

    var dangerValue by remember(station.stationConfig.id) {
        mutableStateOf(station.stationConfig.dangerThreshold?.toFloat() ?: 0f)
    }

    var isFetchingLocation by remember { mutableStateOf(false) }

    LaunchedEffect(station.stationConfig.latitude, station.stationConfig.longitude) {
        isFetchingLocation = false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 24.dp)
            .padding(bottom = 16.dp)
    ) {
        Text(stringResource(R.string.map_setting_station), color = DarkGunmetal, fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nameValue,
            onValueChange = {nameValue = it},
            label = {Text(stringResource(R.string.map_station_name))},
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))



        ThresholdItem(
            title = stringResource(R.string.map_warning_station),
            value = warningValue,
            onValueChange = {
                if(it < dangerValue) warningValue = it
            },
            icon = Icons.Rounded.Warning,
            color = StatusWarning
        )

        Spacer(modifier = Modifier.height(16.dp))

        ThresholdItem(
            title = stringResource(R.string.map_danger_station),
            value = dangerValue,
            onValueChange = {
                if(it > warningValue) dangerValue = it
            },
            icon = Icons.Rounded.Dangerous,
            color = StatusDanger
        )

        Surface(
            onClick = {
                if(!isFetchingLocation) {
                    isFetchingLocation = true
                    onRequestLocationUpdate()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            color = GhostWhite,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if(isFetchingLocation) VividBlue else BrightGray)
        ) {

        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Text(stringResource(R.string.map_cancel), color = DarkGunmetal)
            }

            Button(
                onClick = { onSave(nameValue, offsetValue.toFloat(),warningValue ,dangerValue) },
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.map_save), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun ThresholdItem(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    icon: ImageVector,
    color: Color,
    maxRange: Float = 10f
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(40.dp)
                .background(color.copy(alpha = 0.1f), CircleShape)
                .border(1.dp, color.copy(0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.size(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

                Text(text = String.format("%.1fm", value), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
            }

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = 0f..maxRange,
                colors = SliderDefaults.colors(
                    thumbColor = color,
                    activeTrackColor = color,
                    inactiveTrackColor = color.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth().height(36.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(R.string.map_0m), fontSize = 10.sp, color = Color.Gray)
                Text(text = stringResource(R.string.map_max_range, maxRange.toInt()), fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}