package com.example.doantotnghiep.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doantotnghiep.R
import com.example.doantotnghiep.data.model.HomeUiState
import com.example.doantotnghiep.data.repository.FloodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.doantotnghiep.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: FloodRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private var lastWaterLevel = 0.0

    init {
        observerStationData()
    }

    private fun observerStationData() {
        viewModelScope.launch {
            repository.getRealtimeDatabase("station_01").collect { sensorData ->

                val config = repository.getStationConfig("station_01")

                val offset = config?.calibrationOffset ?: 240
                val currentLevel = (offset - (sensorData?.distanceRaw ?: 0)).toDouble()

                val newTrend = when {
                    currentLevel > lastWaterLevel + 2 -> R.string.dashboard_rising
                    currentLevel < lastWaterLevel - 2 -> R.string.dashboard_falling
                    else -> R.string.dashboard_stable
                }
                lastWaterLevel = currentLevel

                _uiState.update { currentState ->
                    currentState.copy(
                        waterLevel = currentLevel,
                        trend = newTrend,
                        rainRaw = sensorData?.rainVal ?: 1024,
                        temperature = sensorData?.temp ?: 0.0,
                        humidity = sensorData?.humid ?: 0.0,
                        lastUpdated = formatTimestamp(sensorData?.timestamp ?: 0L)
                    )
                }
            }
        }
    }
}