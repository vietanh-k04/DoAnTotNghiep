package com.example.doantotnghiep.ui.dialog

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.rounded.Dangerous
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.doantotnghiep.DANGER_THRESHOLD
import com.example.doantotnghiep.R
import com.example.doantotnghiep.WARNING_THRESHOLD
import com.example.doantotnghiep.data.local.StationMapUiModel
import com.example.doantotnghiep.data.local.enum.CalibrationState
import com.example.doantotnghiep.ui.theme.GlassBg
import com.example.doantotnghiep.ui.theme.StatusDanger
import com.example.doantotnghiep.ui.theme.StatusWarning
import com.example.doantotnghiep.ui.theme.TextDim
import com.example.doantotnghiep.ui.theme.TextWhite
import com.example.doantotnghiep.ui.theme.VividBlue
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private const val TAG = "StationSettingDialog"

enum class LocationFetchState {
    IDLE, FETCHING, SUCCESS, ERROR
}

@Composable
fun StationSettingDialog(
    station: StationMapUiModel,
    onDismiss: () -> Unit,
    onSave: (String, Float, Float, Float, Double?, Double?, (Boolean) -> Unit) -> Unit,
    onRequestOffsetUpdate: () -> Unit
) {
    val context = LocalContext.current
    var nameValue by remember(station.stationConfig.id) {
        mutableStateOf(station.stationConfig.name ?: "")
    }

    var offsetValue by remember(station.stationConfig.id) {
        mutableIntStateOf(station.stationConfig.calibrationOffset ?: 0)
    }

    var warningValue by remember(station.stationConfig.id) {
        mutableFloatStateOf(station.stationConfig.warningThreshold?.toFloat() ?: 0f)
    }

    var dangerValue by remember(station.stationConfig.id) {
        mutableFloatStateOf(station.stationConfig.dangerThreshold?.toFloat() ?: 0f)
    }

    var latitudeValue by remember(station.stationConfig.id) {
        mutableStateOf(station.stationConfig.latitude)
    }

    var longitudeValue by remember(station.stationConfig.id) {
        mutableStateOf(station.stationConfig.longitude)
    }

    var locationFetchState by remember { mutableStateOf(LocationFetchState.IDLE) }
    var fetchedLatitude by remember { mutableStateOf<Double?>(null) }
    var fetchedLongitude by remember { mutableStateOf<Double?>(null) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                          permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (!granted) {
                locationFetchState = LocationFetchState.ERROR
            }
        }
    )

    LaunchedEffect(locationFetchState) {
        if (locationFetchState == LocationFetchState.FETCHING) {
            val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            
            if (hasPermission) {
                try {
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        CancellationTokenSource().token
                    ).addOnSuccessListener { currLocation ->
                        if (currLocation != null) {
                            fetchedLatitude = currLocation.latitude
                            fetchedLongitude = currLocation.longitude
                            locationFetchState = LocationFetchState.SUCCESS
                        } else {
                            locationFetchState = LocationFetchState.ERROR
                        }
                    }.addOnFailureListener {
                        locationFetchState = LocationFetchState.ERROR
                    }
                } catch (_: SecurityException) {
                    locationFetchState = LocationFetchState.ERROR
                }
            } else {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }


    var calibrationState by remember { mutableStateOf(CalibrationState.IDLE) }
    var calibrationValues by remember { mutableStateOf(listOf<Int>()) }
    var finalCalibratedValue by remember { mutableIntStateOf(0) }

    DisposableEffect(calibrationState) {
        var listener: ValueEventListener? = null
        var ref: com.google.firebase.database.DatabaseReference? = null

        if (calibrationState == CalibrationState.MEASURING) {
            val database = FirebaseDatabase.getInstance()
            ref = database.getReference("stations/${station.stationConfig.id}/data")
            
            var isFirstRead = true
            var lastTimestamp: Long? = null

            listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (calibrationState != CalibrationState.MEASURING) return

                    val distanceRaw = snapshot.child("distanceRaw").getValue(Int::class.java)
                    val timestamp = snapshot.child("timestamp").getValue(Long::class.java)

                    if (distanceRaw != null) {
                        if (isFirstRead) {
                            isFirstRead = false
                            lastTimestamp = timestamp
                            return
                        }

                        if (lastTimestamp != null && lastTimestamp == timestamp) return
                        lastTimestamp = timestamp

                        calibrationValues = calibrationValues + distanceRaw

                        if (calibrationValues.size >= 5) {
                            val min = calibrationValues.minOrNull() ?: 0
                            val max = calibrationValues.maxOrNull() ?: 0

                            if (max - min <= 2) {
                                val mode = calibrationValues.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: distanceRaw
                                finalCalibratedValue = mode
                                calibrationState = CalibrationState.SUCCESS
                            } else {
                                calibrationState = CalibrationState.ERROR
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    calibrationState = CalibrationState.ERROR
                }
            }
            ref.addValueEventListener(listener)
        }

        onDispose {
            if (ref != null && listener != null) {
                ref.removeEventListener(listener)
            }
        }
    }

    var isSaving by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.map_setting_station), 
                color = TextWhite, 
                fontSize = 22.sp, 
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = nameValue,
                onValueChange = {nameValue = it},
                label = {Text(stringResource(R.string.map_station_name))},
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = VividBlue,
                    unfocusedBorderColor = TextDim,
                    focusedLabelColor = VividBlue,
                    unfocusedLabelColor = TextDim,
                    cursorColor = TextWhite
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            val isMeasuring = calibrationState == CalibrationState.MEASURING
            Surface(
                onClick = {
                    if (calibrationState == CalibrationState.IDLE) {
                        calibrationValues = emptyList()
                        calibrationState = CalibrationState.MEASURING
                        onRequestOffsetUpdate()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                color = GlassBg,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if(isMeasuring) VividBlue else Color.Transparent)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Height, contentDescription = null, tint = if(isMeasuring) VividBlue else TextDim)

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isMeasuring) stringResource(R.string.map_is_fetching_offset) else stringResource(R.string.map_fetch_offset),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = if(isMeasuring) VividBlue else TextWhite
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = stringResource(R.string.map_max_range, offsetValue),
                            fontSize = 13.sp,
                            color = TextDim
                        )
                    }

                    if(isMeasuring) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = VividBlue, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = null, tint = TextDim)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            ThresholdItem(
                title = stringResource(R.string.map_warning_station),
                value = warningValue,
                onValueChange = {
                    if(it <= dangerValue) warningValue = it
                },
                icon = Icons.Rounded.Warning,
                color = StatusWarning,
                maxRange = offsetValue.toFloat()
            )

            Spacer(modifier = Modifier.height(16.dp))

            ThresholdItem(
                title = stringResource(R.string.map_danger_station),
                value = dangerValue,
                onValueChange = {
                    if(it >= warningValue) dangerValue = it
                },
                icon = Icons.Rounded.Dangerous,
                color = StatusDanger,
                maxRange = offsetValue.toFloat()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                onClick = {
                    if (locationFetchState == LocationFetchState.IDLE) {
                        locationFetchState = LocationFetchState.FETCHING
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                color = GlassBg,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (locationFetchState == LocationFetchState.FETCHING) VividBlue else Color.Transparent)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = if (locationFetchState == LocationFetchState.FETCHING) VividBlue else TextDim)
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (locationFetchState == LocationFetchState.FETCHING) stringResource(R.string.map_is_fetching_location) else stringResource(R.string.map_fetch_location),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (locationFetchState == LocationFetchState.FETCHING) VividBlue else TextWhite
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = stringResource(R.string.map_location_on_value, latitudeValue ?: 0.0, longitudeValue ?: 0.0),
                            fontSize = 13.sp,
                            color = TextDim
                        )
                    }

                    if(locationFetchState == LocationFetchState.FETCHING) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = VividBlue, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = null, tint = TextDim)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                    enabled = !isSaving
                ) {
                    Text(stringResource(R.string.cancel), color = if(isSaving) TextDim else TextWhite)
                }

                Button(
                    onClick = {
                        isSaving = true
                        onSave(nameValue, offsetValue.toFloat(), warningValue, dangerValue, latitudeValue, longitudeValue) { success ->
                            isSaving = false
                            if(success) onDismiss()
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VividBlue),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSaving
                ) {
                    if(isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.map_save), fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        when (calibrationState) {
            CalibrationState.MEASURING -> {
                CalibrationMeasuringDialog(
                    progressCount = calibrationValues.size,
                    onCancel = { calibrationState = CalibrationState.IDLE }
                )
            }
            CalibrationState.SUCCESS -> {
                CalibrationSuccessDialog(
                    finalCalibratedValue = finalCalibratedValue,
                    onCancel = { calibrationState = CalibrationState.IDLE },
                    onUpdate = {
                        offsetValue = finalCalibratedValue 
                        warningValue = finalCalibratedValue * WARNING_THRESHOLD
                        dangerValue = finalCalibratedValue * DANGER_THRESHOLD
                        calibrationState = CalibrationState.IDLE
                    }
                )
            }
            CalibrationState.ERROR -> {
                CalibrationErrorDialog(
                    onCancel = { calibrationState = CalibrationState.IDLE },
                    onRetry = {
                        calibrationValues = emptyList()
                        calibrationState = CalibrationState.MEASURING
                    }
                )
            }
            else -> {}
        }

        when (locationFetchState) {
            LocationFetchState.FETCHING -> {
                LocationFetchingDialog(onCancel = { locationFetchState = LocationFetchState.IDLE })
            }
            LocationFetchState.SUCCESS -> {
                LocationSuccessDialog(
                    lat = fetchedLatitude ?: 0.0,
                    lng = fetchedLongitude ?: 0.0,
                    onCancel = { locationFetchState = LocationFetchState.IDLE },
                    onUpdate = {
                        latitudeValue = fetchedLatitude
                        longitudeValue = fetchedLongitude
                        locationFetchState = LocationFetchState.IDLE
                    }
                )
            }
            LocationFetchState.ERROR -> {
                LocationErrorDialog(
                    onCancel = { locationFetchState = LocationFetchState.IDLE },
                    onRetry = {
                        locationFetchState = LocationFetchState.FETCHING
                    }
                )
            }
            else -> {}
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
    maxRange: Float
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

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDim)

                Text(text = String.format("%.1f cm", value), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
            }
            
            Spacer(modifier = Modifier.height(4.dp))

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = 0f..maxRange,
                colors = SliderDefaults.colors(
                    thumbColor = color,
                    activeTrackColor = color,
                    inactiveTrackColor = color.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth().height(24.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(R.string.map_0m), fontSize = 10.sp, color = TextDim)
                Text(text = stringResource(R.string.map_max_range, maxRange.toInt()), fontSize = 10.sp, color = TextDim)
            }
        }
    }
}