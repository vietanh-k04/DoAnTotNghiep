package com.example.doantotnghiep.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doantotnghiep.R
import com.example.doantotnghiep.RADIUS_LIMIT
import com.example.doantotnghiep.data.local.StationMapUiModel
import com.example.doantotnghiep.data.local.enum.WaterLevelState
import com.example.doantotnghiep.data.local.state.HomeUiState
import com.example.doantotnghiep.data.remote.NotificationLog
import com.example.doantotnghiep.data.remote.SensorData
import com.example.doantotnghiep.data.remote.StationConfig
import com.example.doantotnghiep.data.repository.FloodRepository
import com.example.doantotnghiep.notification.NotificationHelper
import com.example.doantotnghiep.utils.LocationUtils
import com.example.doantotnghiep.utils.WaterLevelValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
class HomeViewModel @Inject constructor(
    private val repository: FloodRepository,
    private val notificationHelper: NotificationHelper,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _notifications = MutableStateFlow<List<NotificationLog>>(emptyList())
    val notification: StateFlow<List<NotificationLog>> = _notifications

    val unreadCount: StateFlow<Int> = _notifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    private var lastRawWaterLevel = -1.0
    private var lastStatusResId: Int? = null
    private var smoothedLevel = -1.0

    private var observeJob: Job? = null
    private var currentObservedStationId: String? = null
    private var lastObservedConfig: StationConfig? = null

    private var hasAlertedRecalibration = false
    private var hasAlertedObstruction = false

    init {
        repository.syncNotificationLogs()
        viewModelScope.launch {
            repository.getNotificationLogs().collect { _notifications.value = it }
        }
    }

    fun updateLocationAndStations(userLat: Double?, userLng: Double?, stations: List<StationMapUiModel>) {
        viewModelScope.launch {
            if (userLat != null && userLng != null && stations.isNotEmpty()) {
                scanAndSyncData(userLat, userLng, stations)
            } else if (stations.isNotEmpty()) {
                val defaultStation = stations.first()
                scanAndSyncData(
                    defaultStation.stationConfig.latitude ?: 0.0,
                    defaultStation.stationConfig.longitude ?: 0.0,
                    stations
                )
            } else {
                delay(2500L)
                if (_uiState.value.isLocal) setLocalMode(false)
            }
        }
    }

    private fun scanAndSyncData(userLat: Double, userLng: Double, allStations: List<StationMapUiModel>) {
        var closestStation: StationMapUiModel? = null
        var minDistance = Float.MAX_VALUE

        for (station in allStations) {
            val stLat = station.stationConfig.latitude ?: continue
            val stLng = station.stationConfig.longitude ?: continue
            val distance = LocationUtils.calculateDistance(userLat, userLng, stLat, stLng)

            if (distance <= RADIUS_LIMIT && distance < minDistance) {
                minDistance = distance
                closestStation = station
            }
        }

        if (closestStation != null) {
            _uiState.update { it.copy(isLocal = true) }
            val targetStationId = closestStation.stationConfig.id ?: ""
            if (currentObservedStationId != targetStationId) {
                observerStationData(targetStationId)
            }
        } else {
            setLocalMode(false)
        }
    }

    fun setLocalMode(isLocal: Boolean) {
        _uiState.update { it.copy(isLocal = isLocal) }
        if (!isLocal) {
            observeJob?.cancel()
            currentObservedStationId = null
        }
    }

    private fun observerStationData(stationId: String) {
        currentObservedStationId = stationId
        observeJob?.cancel()

        smoothedLevel = -1.0
        lastRawWaterLevel = -1.0

        observeJob = viewModelScope.launch {
            combine(
                repository.observeStationConfig(stationId),
                repository.getRealtimeDatabase(stationId)
            ) { config, sensorData -> config to sensorData }
                .collect { (config, sensorData) ->
                    if (config != lastObservedConfig) {
                        lastRawWaterLevel = -1.0
                        lastObservedConfig = config
                    }

                    if (sensorData == null || config == null) return@collect

                    val offset = config.calibrationOffset ?: 0
                    val rawDist = sensorData.distanceRaw ?: 0
                    val rawWaterLevel = (offset - rawDist).toDouble()

                    val validationState = WaterLevelValidator.validate(rawWaterLevel, lastRawWaterLevel)
                    handleValidation(validationState, rawWaterLevel)

                    val displayLevel = if (rawWaterLevel < 0) 0.0 else rawWaterLevel
                    val waterPercent = if (offset > 0) (displayLevel.toFloat() / offset.toFloat()).coerceIn(0f, 1f) else 0f

                    val status = determineStatus(displayLevel, config)

                    if (lastStatusResId != null && status != lastStatusResId) {
                        if (status == R.string.status_danger || status == R.string.status_warning) {
                            triggerAlert(status, displayLevel)
                        }
                    }
                    lastStatusResId = status

                    val trend = calculateTrend(displayLevel)

                    updateHomeUI(displayLevel, waterPercent, status, trend, sensorData)

                    lastRawWaterLevel = rawWaterLevel
                }
        }
    }

    private fun handleValidation(state: WaterLevelState, currentRaw: Double) {
        when (state) {
            WaterLevelState.ERROR_NEGATIVE -> {
                if (!hasAlertedRecalibration) {
                    _uiState.update { it.copy(showRecalibratePopup = true) }
                    hasAlertedRecalibration = true
                    viewModelScope.launch { delay(5000L); dismissRecalibratePopup() }
                }
            }
            WaterLevelState.ERROR_OBSTRUCTION -> {
                if (!hasAlertedObstruction) {
                    _uiState.update { it.copy(showObstructionPopup = true) }
                    hasAlertedObstruction = true
                    viewModelScope.launch { delay(5000L); dismissObstructionPopup() }
                }
            }
            WaterLevelState.VALID -> {
                hasAlertedRecalibration = false
                if (!WaterLevelValidator.isStabilized(currentRaw, lastRawWaterLevel)) {
                    hasAlertedObstruction = false
                }
            }
        }
    }

    private fun determineStatus(level: Double, config: StationConfig): Int {
        val warning = config.warningThreshold ?: 0.0
        val danger = config.dangerThreshold ?: 0.0
        return when {
            level >= danger -> R.string.status_danger
            level >= warning -> R.string.status_warning
            else -> R.string.status_safe
        }
    }

    private fun calculateTrend(currentLevel: Double): Int {
        if (smoothedLevel < 0) {
            smoothedLevel = currentLevel
            return R.string.dashboard_stable
        }

        smoothedLevel = 0.20 * currentLevel + 0.80 * smoothedLevel
        val diff = currentLevel - smoothedLevel

        return when {
            diff > 0.2 -> R.string.dashboard_rising
            diff < -0.2 -> R.string.dashboard_falling
            else -> R.string.dashboard_stable
        }
    }

    private fun updateHomeUI(level: Double, percent: Float, status: Int, trend: Int, data: SensorData) {
        _uiState.update { currentState ->
            val rawTs = data.timestamp
            val fixedTs = if (rawTs != null && rawTs < 10000000000L) rawTs * 1000 else rawTs ?: currentState.timestamp

            currentState.copy(
                waterLevel = level,
                waterPercent = percent,
                status = status,
                trend = trend,
                temperature = data.temp ?: currentState.temperature,
                humidity = data.humid ?: currentState.humidity,
                rainRaw = data.rainVal ?: currentState.rainRaw,
                timestamp = fixedTs
            )
        }
    }

    private fun triggerAlert(status: Int, level: Double) {
        val titleRes = when(status) {
            R.string.status_danger -> R.string.alert_danger
            R.string.status_warning -> R.string.alert_warning
            else -> R.string.alert_safe
        }
        notificationHelper.sendAlert(
            context.getString(titleRes),
            context.getString(R.string.alert_water_level, String.format("%.1f", level)),
            status
        )
    }

    fun dismissRecalibratePopup() { _uiState.update { it.copy(showRecalibratePopup = false) } }
    fun dismissObstructionPopup() { _uiState.update { it.copy(showObstructionPopup = false) } }
    fun markAsRead(log: NotificationLog) { viewModelScope.launch { repository.markAsRead(log.id) } }
    fun markAllAsRead() { viewModelScope.launch { repository.markAllAsRead() } }
}