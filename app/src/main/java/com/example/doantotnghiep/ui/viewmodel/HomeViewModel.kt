package com.example.doantotnghiep.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doantotnghiep.R
import com.example.doantotnghiep.data.model.HomeUiState
import com.example.doantotnghiep.data.model.SensorData
import com.example.doantotnghiep.data.model.StationConfig
import com.example.doantotnghiep.data.repository.FloodRepository
import com.example.doantotnghiep.notification.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: FloodRepository, private val notificationHelper: NotificationHelper, @ApplicationContext private val context: Context) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private var lastWaterLevel = 0.0

    private var lastStatus = R.string.status_safe

    private var percent = 0f

    init {
        observerStationData("station_01")
    }

    private fun observerStationData(stationId: String) {
        viewModelScope.launch {
            val config = repository.getStationConfig(stationId)
            repository.getRealtimeDatabase(stationId).collect { sensorData ->
                val currentLevel = calculateWaterLevel(sensorData, config)

                val status = determineStatus(currentLevel, config)

                if (status != lastStatus) {
                    triggerAlert(status, currentLevel)
                    lastStatus = status
                }

                val trend = calculatorTrend(currentLevel)

                updateUI(currentLevel, status, trend, sensorData)
            }
        }
    }

    private fun calculateWaterLevel(data: SensorData?, config: StationConfig?) : Double {
        val offset = config?.calibrationOffset ?: 0
        val waterLevel = (offset - (data?.distanceRaw ?: 0)).toDouble()
        percent = waterLevel.toFloat() / offset.toFloat()
        return waterLevel

    }

    private fun determineStatus(level: Double, config: StationConfig?) : Int {
        val warning = config?.warningThreshold ?: 0.0
        val danger = config?.dangerThreshold ?: 0.0

        return when {
            level > danger -> R.string.status_danger
            level > warning -> R.string.status_warning
            else -> R.string.status_safe
        }
    }

    private fun calculatorTrend(currentLevel: Double) : Int {
        val trend = when {
            currentLevel > lastWaterLevel + 1.5 -> R.string.dashboard_rising //
            currentLevel < lastWaterLevel - 1.5 -> R.string.dashboard_falling
            else -> R.string.dashboard_stable
        }
        lastWaterLevel = currentLevel
        return trend
    }

    private fun triggerAlert(status: Int, level: Double) {
        val title = when(status) {
            R.string.status_danger -> R.string.alert_danger
            R.string.status_warning -> R.string.alert_warning
            else -> R.string.alert_safe
        }

        notificationHelper.sendAlert(context.getString(title), context.getString(R.string.alert_water_level, level.toString()), status)

        if (status != R.string.status_safe) {
            val logType = if (status == R.string.status_danger) 2 else 1
            viewModelScope.launch {
                repository.updateNotificationLog(context.getString(title), context.getString(R.string.alert_water_level, level.toString()), logType)
            }
        }
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
}