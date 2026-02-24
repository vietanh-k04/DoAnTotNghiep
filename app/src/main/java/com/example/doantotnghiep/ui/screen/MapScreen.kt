package com.example.doantotnghiep.ui.screen

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doantotnghiep.R
import com.example.doantotnghiep.data.local.StationMapUiModel
import com.example.doantotnghiep.data.local.Status
import com.example.doantotnghiep.ui.theme.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(stations: List<StationMapUiModel>) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(21.0285, 105.8246), 11f)
    }

    BottomSheetScaffold(
        scaffoldState = rememberBottomSheetScaffoldState(),
        sheetContentColor = NavyGray,
        sheetPeekHeight = 350.dp,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContent = {

        }
    ) { paddingValues ->
        GoogleMap(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true, mapType = MapType.NORMAL),
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            stations.forEach { station ->
                val stationLatLng = LatLng(station.latitude, station.longitude)
                val statusColor = when (station.status) {
                    Status.DANGER -> StatusDanger
                    Status.WARNING  -> StatusWarning
                    else -> StatusSuccess
                }

                Circle(
                    center = stationLatLng,
                    radius = station.coverageRadius,
                    fillColor = statusColor.copy(alpha = 0.2f),
                    strokeColor = statusColor.copy(alpha = 0.5f),
                    strokeWidth = 2f
                )

                Marker(
                    state = MarkerState(position = stationLatLng),
                    title = station.name,
                    snippet = stringResource(R.string.map_currentLevel, station.depth)
                )
            }
        }
    }
}

@Composable
fun StationListContent(stations: List<StationMapUiModel>) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(8.dp))

        Text(stringResource(R.string.map_list_monitor_stations), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(stringResource(R.string.map_count_active_station, stations.size), fontSize = 14.sp, color = Color.Gray)

        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(stations) { item ->
                StationMapItemCard(item)
            }
        }
    }
}

@Composable
fun StationMapItemCard(station: StationMapUiModel) {
    val statusColor = if(station.status == Status.DANGER) StatusDanger else if (station.status == Status.WARNING) StatusWarning else StatusSuccess

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGunmetal)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(painter = painterResource(R.drawable.ic_water_drop), contentDescription = null, tint = statusColor)
                    }

                    Spacer(Modifier.width(12.dp))

                    Column{
                        Text(station.name ?: "", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(statusColor, CircleShape))

                            Spacer(Modifier.width(6.dp))

                            Text(if (station.status == Status.DANGER) "DANGER" else if (station.status == Status.WARNING) "WARNING" else "SAFE",
                                color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.map_value_currentLevel, station.depth), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.map_depth), color = Color.Gray, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text(stringResource(R.string.map_1h_trend), color = Color.Gray, fontSize = 12.sp)
                    val trendText = if (station.trendValue == "RISING") "+${station.trendValue}m ↗" else "${station.trendValue}m ↘"
                    val trendColor = if (station.trendValue == "RISING") Color(0xFFFF3B30) else Color(0xFF34C759)
                    Text(trendText, color = trendColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                MiniTrendChart(trendColor = statusColor)
            }
        }
    }
}

@Composable
fun MiniTrendChart(trendColor: Color) {
    Canvas(modifier = Modifier.width(60.dp).height(20.dp)) {
        val path = Path().apply {
            moveTo(0f, size.height)
            lineTo(size.width, 0f)
        }

        drawPath(path = path, color = trendColor, style = Stroke(width = 3f))

        val fillPath = Path().apply {
            addPath(path)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(fillPath, brush = Brush.verticalGradient(listOf(trendColor.copy(alpha = 0.3f), Color.Transparent)))
    }
}


