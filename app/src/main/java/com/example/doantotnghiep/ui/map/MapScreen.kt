package com.example.doantotnghiep.ui.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.window.Dialog
import com.example.doantotnghiep.R
import com.example.doantotnghiep.data.local.StationMapUiModel
import com.example.doantotnghiep.ui.dialog.PasswordDialog
import com.example.doantotnghiep.ui.dialog.StationSettingDialog
import com.example.doantotnghiep.ui.theme.DarkGunmetal
import com.example.doantotnghiep.ui.theme.EerieBlack
import com.example.doantotnghiep.ui.theme.NavyGray
import com.example.doantotnghiep.ui.theme.OffWhite
import com.example.doantotnghiep.ui.theme.SoftBgBottom
import com.example.doantotnghiep.ui.theme.SoftBgTop
import com.example.doantotnghiep.utils.statusColor
import com.example.doantotnghiep.utils.statusText
import com.example.doantotnghiep.utils.trendColor
import com.example.doantotnghiep.utils.trendText
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    stations: List<StationMapUiModel>,
    isLocationGranted: Boolean,
    userLocation: LatLng?,
    onUpdateStationConfig: (String, String, Int, Double, Double, (Boolean) -> Unit) -> Unit = { _, _, _, _, _, _ -> }
) {
    var selectedStation by remember { mutableStateOf<StationMapUiModel?>(null) }

    var showPasswordPrompt by remember { mutableStateOf(false) }

    var showSettingsSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val defaultLocation = LatLng(21.0285, 105.8246)

    val coroutineScope = rememberCoroutineScope()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 11f)
    }

    LaunchedEffect(userLocation) {
        if (userLocation != null) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(userLocation, 14f),
                durationMs = 1500
            )
        }
    }

    LaunchedEffect(stations) {
        selectedStation?.let { currentStation ->
            val freshStation = stations.find { it.stationConfig.id == currentStation.stationConfig.id }
            if (freshStation != null) {
                selectedStation = freshStation
            }
        }
    }

    BottomSheetScaffold(
        scaffoldState = rememberBottomSheetScaffoldState(),
        sheetContentColor = OffWhite,
        sheetPeekHeight = 250.dp,
        sheetDragHandle = { BottomSheetDefaults.DragHandle() },
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContent = {
            Box(modifier = Modifier.fillMaxHeight(0.5f)) {
                StationListContent(
                    stations = stations,
                    onStationSingleClicked = { station ->
                        selectedStation = station
                    },
                    onStationDoubleClicked = { station ->
                        if(station.stationConfig.latitude != 0.0 && station.stationConfig.longitude != 0.0) {
                            val targetLatLng = LatLng(station.stationConfig.latitude ?: 0.0, station.stationConfig.longitude ?: 0.0)

                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(targetLatLng, 15f),
                                    durationMs = 2000
                                )
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        GoogleMap(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = isLocationGranted, mapType = MapType.NORMAL),
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            stations.forEach { station ->
                val stationLatLng = LatLng(station.stationConfig.latitude ?: 0.0, station.stationConfig.longitude ?: 0.0)
                val statusColor = statusColor(station.status)

                AnimatedCoverageCircle(
                    stationLatLng = stationLatLng,
                    coverageRadius = station.coverageRadius,
                    statusColor = statusColor
                )

                Marker(
                    state = MarkerState(position = stationLatLng),
                    title = station.stationConfig.name,
                    snippet = stringResource(R.string.map_currentLevel, station.sensorData.distanceRaw ?: 0.0)
                )
            }
        }
    }

    if(selectedStation != null && !showPasswordPrompt && !showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = {selectedStation = null},
            sheetState = sheetState,
            containerColor = Color.Transparent,
            dragHandle = null
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(listOf(SoftBgTop, SoftBgBottom)),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(4.dp)
                            .background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                    )
                    StationDetailContent(station = selectedStation!!, onSettingClick = {
                        showPasswordPrompt = true
                    })
                }
            }
        }
    }

    if (showPasswordPrompt && selectedStation != null) {
        Dialog(onDismissRequest = { showPasswordPrompt = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent
            ) {
                Box(modifier = Modifier.background(Brush.verticalGradient(listOf(SoftBgTop, SoftBgBottom)))) {
                    PasswordDialog(
                        onDismiss = { showPasswordPrompt = false },
                        correctHash = selectedStation!!.stationConfig.deviceKey ?: "",
                        onVerifySuccess = {
                            showPasswordPrompt = false
                            showSettingsSheet = true
                        }
                    )
                }
            }
        }
    }

    if (showSettingsSheet && selectedStation != null) {
        Dialog(onDismissRequest = { showSettingsSheet = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent
            ) {
                Box(modifier = Modifier.background(Brush.verticalGradient(listOf(SoftBgTop, SoftBgBottom)))) {
                    StationSettingDialog(
                        station = selectedStation!!,
                        onDismiss = { showSettingsSheet = false },
                        onSave = { newName, newOffset, newWarning, newDanger, onComplete ->
                            onUpdateStationConfig(
                                selectedStation!!.stationConfig.id ?: "",
                                newName,
                                newOffset.toInt(),
                                newWarning.toDouble(),
                                newDanger.toDouble(),
                                onComplete
                            )
                        },
                        onRequestLocationUpdate = {},
                        onRequestOffsetUpdate = {}
                    )
                }
            }
        }
    }
}

@Composable
fun StationListContent(
    stations: List<StationMapUiModel>,
    onStationSingleClicked: (StationMapUiModel) -> Unit,
    onStationDoubleClicked: (StationMapUiModel) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.map_list_monitor_stations),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = EerieBlack
                )
                Text(
                    text = stringResource(R.string.map_count_active_station, stations.size),
                    fontSize = 14.sp,
                    color = NavyGray
                )

                Spacer(Modifier.height(4.dp))
            }
        }

        items(stations) { item ->
            StationMapItemCard(
                station = item,
                onSingleClick = { onStationSingleClicked(item) },
                onDoubleClick = { onStationDoubleClicked(item) }
            )
        }
    }
}

@Composable
fun StationMapItemCard(station: StationMapUiModel, onSingleClick: () -> Unit?, onDoubleClick: () -> Unit?) {
    val statusColor = statusColor(station.status)

    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(
            onClick = { onSingleClick() },
            onDoubleClick = { onDoubleClick() }
        ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.4f))
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
                        Text(station.stationConfig.name ?: "", color = DarkGunmetal, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(statusColor, CircleShape))

                            Spacer(Modifier.width(6.dp))

                            Text(statusText(station.status),
                                color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.map_value_currentLevel, station.sensorData.distanceRaw ?: 0.0), color = DarkGunmetal, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.map_current_level), color = DarkGunmetal, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text(stringResource(R.string.map_1h_trend), color = Color.Gray, fontSize = 12.sp)
                    val trendText = trendText(station.trendValue)
                    val trendColor = trendColor(station.trendValue)
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