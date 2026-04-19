package com.example.doantotnghiep.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doantotnghiep.data.remote.WeatherUiState
import com.example.doantotnghiep.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(val repository: WeatherRepository) : ViewModel() {
    private val TAG = "WeatherViewModel"

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var currentLocationQuery: String? = null
    private var lastFetchTime: Long = 0

    fun fetchWeather(location: String, forceRefresh: Boolean = false) {
        val currentTime = System.currentTimeMillis()
        if (!forceRefresh && currentLocationQuery == location && _uiState.value is WeatherUiState.Success) {
            if (currentTime - lastFetchTime < 15 * 60 * 1000) {
                Log.d(TAG, "Data is up to date, skip fetching")
                return
            }
        }
        
        currentLocationQuery = location
        lastFetchTime = currentTime

        viewModelScope.launch(Dispatchers.IO) {
            if (_uiState.value !is WeatherUiState.Success) {
                _uiState.value = WeatherUiState.Loading
            }
            Log.d(TAG, "Fetching weather data for: $location")

            val result = repository.getWeatherData(location)

            result.fold(
                onSuccess = { weatherData ->
                    Log.d(TAG, "Fetch success: ${weatherData.header.locationName}")
                    _uiState.value = WeatherUiState.Success(weatherData)
                },
                onFailure = { error ->
                    Log.e(TAG, "Fetch failed: ${error.message}", error)
                    _uiState.value = WeatherUiState.Error(
                        error.message ?: "Có lỗi xảy ra khi tải dữ liệu thời tiết"
                    )
                }
            )
        }
    }
}
