package com.example.doantotnghiep.ui.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doantotnghiep.R
import com.example.doantotnghiep.data.local.StationMapUiModel
import com.example.doantotnghiep.ui.theme.DarkGunmetal

@Composable
fun StationSettingContent(
    station: StationMapUiModel,
    onDismiss: () -> Unit,
    onSave: (Float, Float) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 24.dp).padding(bottom = 16.dp)
    ) {
        Text(stringResource(R.string.map_setting_station), color = DarkGunmetal, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = station.stationConfig.name ?: "", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(24.dp))


    }
}