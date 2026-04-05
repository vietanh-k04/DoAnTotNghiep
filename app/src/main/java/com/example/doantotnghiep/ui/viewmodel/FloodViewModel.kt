package com.example.doantotnghiep.ui.viewmodel

import androidx.lifecycle.ViewModel

class FloodViewModel : ViewModel() {
    /*private val mlHelper = FloodPredictionHelper()

    private val _predictedWaterLevel = MutableStateFlow<Float?>(null)
    val predictedWaterLevel: StateFlow<Float?> = _predictedWaterLevel

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun calculateFloodRisk(data: List<HourlyData>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resultCm = mlHelper.predictFutureWaterLevel(data)
                _predictedWaterLevel.value = resultCm
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }*/
}