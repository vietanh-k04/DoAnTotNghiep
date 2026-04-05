package com.example.doantotnghiep.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doantotnghiep.R
import com.example.doantotnghiep.RADIUS_LIMIT
import com.example.doantotnghiep.data.local.StationMapUiModel
import com.example.doantotnghiep.data.local.state.HomeUiState
import com.example.doantotnghiep.data.remote.NotificationLog
import com.example.doantotnghiep.data.remote.SensorData
import com.example.doantotnghiep.data.remote.StationConfig
import com.example.doantotnghiep.data.repository.FloodRepository
import com.example.doantotnghiep.notification.NotificationHelper
import com.example.doantotnghiep.utils.LocationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: FloodRepository, private val notificationHelper: NotificationHelper, @ApplicationContext private val context: Context) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _notifications = MutableStateFlow<List<NotificationLog>>(emptyList())

    val notification: StateFlow<List<NotificationLog>> = _notifications

    val unreadCount: StateFlow<Int> = _notifications.map { list ->
        list.count {!it.isRead}
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    private var lastWaterLevel = -1.0
    private var lastStatus: Int? = null

    private var smoothedLevel = -1.0

    private var percent = 0f

    private var observeJob: Job? = null
    private var currentObservedStationId: String? = null

    private var hasAlertedRecalibration = false
    private var hasAlertedObstruction = false

    init {
        repository.syncNotificationLogs()
        viewModelScope.launch {
            repository.getNotificationLogs().collect { _notifications.value = it }
        }
    }

    fun scanAndSyncData(userLat: Double, userLng: Double, allStations: List<StationMapUiModel>) {
        var closetStation: StationMapUiModel? = null
        var minDistance = Float.MAX_VALUE

        for(station in allStations) {
            val stLat = station.stationConfig.latitude ?: continue
            val stLng = station.stationConfig.longitude ?: continue

            val distance = LocationUtils.calculateDistance(userLat, userLng, stLat, stLng)

            if(distance <= RADIUS_LIMIT && distance < minDistance) {
                minDistance = distance
                closetStation = station
            }
        }

        if(closetStation != null) {
            _uiState.update { currentState ->
                currentState.copy(isLocal = true)
            }
            val targetStationId = closetStation.stationConfig.id ?: ""
            if (currentObservedStationId != targetStationId) {
                observerStationData(targetStationId)
            }
        } else {
            setLocalMode(false)
        }
    }

    fun setLocalMode(isLocal: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(isLocal = isLocal)
        }
        if (!isLocal) {
            observeJob?.cancel()
            currentObservedStationId = null
        }
    }

    private fun observerStationData(stationId: String) {
        currentObservedStationId = stationId
        observeJob?.cancel()
        smoothedLevel = -1.0
        lastWaterLevel = -1.0
        observeJob = viewModelScope.launch {
            combine(
                repository.observeStationConfig(stationId),
                repository.getRealtimeDatabase(stationId)
            ) { config, sensorData ->
                Pair(config, sensorData)
            }.collect { (config, sensorData) ->
                val offset = config?.calibrationOffset ?: 0
                val rawWaterLevel = (offset - (sensorData?.distanceRaw ?: 0)).toDouble()
                val currentLevel = calculateWaterLevel(sensorData, config)

                var isInvalid = false
                val validationState = com.example.doantotnghiep.utils.WaterLevelValidator.validate(rawWaterLevel, lastWaterLevel)

                when (validationState) {
                    com.example.doantotnghiep.utils.WaterLevelState.ERROR_NEGATIVE -> {
                        isInvalid = true
                        if (!hasAlertedRecalibration) {
                            _uiState.update { it.copy(showRecalibratePopup = true) }
                            hasAlertedRecalibration = true
                        }
                    }
                    com.example.doantotnghiep.utils.WaterLevelState.ERROR_OBSTRUCTION -> {
                        isInvalid = true
                        if (!hasAlertedObstruction) {
                            _uiState.update { it.copy(showObstructionPopup = true) }
                            hasAlertedObstruction = true
                        }
                    }
                    com.example.doantotnghiep.utils.WaterLevelState.VALID -> {
                        hasAlertedRecalibration = false
                        if (com.example.doantotnghiep.utils.WaterLevelValidator.isStabilized(rawWaterLevel, lastWaterLevel)) {
                            hasAlertedObstruction = false
                        }
                    }
                }

                if (isInvalid) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            temperature = sensorData?.temp ?: currentState.temperature,
                            humidity = sensorData?.humid ?: currentState.humidity,
                            rainRaw = sensorData?.rainVal ?: currentState.rainRaw,
                            timestamp = sensorData?.timestamp ?: currentState.timestamp
                        )
                    }
                    return@collect
                }

                val status = determineStatus(currentLevel, config)

                if (lastStatus == null) {
                    lastStatus = status
                } else if (status != lastStatus) {
                    if (status == R.string.status_danger || status == R.string.status_warning) {
                        triggerAlert(status, currentLevel)
                    }
                    lastStatus = status
                }

                val trend = calculatorTrend(currentLevel)

                updateUI(currentLevel, status, trend, sensorData)
                lastWaterLevel = rawWaterLevel
            }
        }
    }

    fun dismissRecalibratePopup() {
        _uiState.update { it.copy(showRecalibratePopup = false) }
    }

    fun dismissObstructionPopup() {
        _uiState.update { it.copy(showObstructionPopup = false) }
    }

    private fun calculateWaterLevel(data: SensorData?, config: StationConfig?) : Double {
        val offset = config?.calibrationOffset ?: 0
        val waterLevel =  (offset - (data?.distanceRaw ?: 0)).toDouble()
        val waterLevelPos = if(waterLevel < 0) 0.0 else waterLevel
        percent = if (offset > 0) waterLevelPos.toFloat() / offset.toFloat() else 0f
        return waterLevelPos
    }

    private fun determineStatus(level: Double, config: StationConfig?) : Int {
        val warning = config?.warningThreshold ?: 0.0
        val danger = config?.dangerThreshold ?: 0.0

        return when {
            level >= danger -> R.string.status_danger
            level >= warning -> R.string.status_warning
            else -> R.string.status_safe
        }
    }

    private fun calculatorTrend(currentLevel: Double) : Int {
        if (smoothedLevel < 0) {
            smoothedLevel = currentLevel
            return R.string.dashboard_stable
        }

        // Sử dụng EMA (Exponential Moving Average) để theo dõi xu hướng tức thời
        // alpha = 0.25 giúp làm mượt các dao động nhỏ, phản ứng nhanh với test kéo trượt liên tục
        smoothedLevel = 0.25 * currentLevel + 0.75 * smoothedLevel

        // Tính độ lệch giữa mức nước thật hiện tại và đường trung bình
        val diff = currentLevel - smoothedLevel

        return when {
            diff > 0.3 -> R.string.dashboard_rising
            diff < -0.3 -> R.string.dashboard_falling
            else -> R.string.dashboard_stable
        }
    }

    private fun triggerAlert(status: Int, level: Double) {
        val title = when(status) {
            R.string.status_danger -> R.string.alert_danger
            R.string.status_warning -> R.string.alert_warning
            else -> R.string.alert_safe
        }
        notificationHelper.sendAlert(context.getString(title), context.getString(R.string.alert_water_level, level.toString()), status)
    }

    private fun updateUI(level: Double, status: Int, trend: Int, data: SensorData?) {
        _uiState.update { currentState ->
            currentState.copy(
                waterLevel = level,
                status = status,
                trend = trend,
                rainRaw = data?.rainVal ?: 1024,
                temperature = data?.temp ?: 0.0,
                humidity = data?.humid ?: 0.0,
                timestamp = data?.timestamp ?: 0L,
                waterPercent = percent
            )
        }
    }

    fun markAsRead(log: NotificationLog) {
        viewModelScope.launch { repository.markAsRead(log.id) }
    }

    fun markAllAsRead() {
        viewModelScope.launch { repository.markAllAsRead() }
    }
}