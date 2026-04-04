package com.example.doantotnghiep.data.remote

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val weatherData: WeatherData) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

data class WeatherData(
    val header: WeatherHeader,
    val hourlyForecast: List<HourlyWeather>,
    val dailyForecast: List<DailyWeather>,
    val aqi: AqiData,
    val metrics: WeatherMetrics,
    val astro: AstroData,
    val runningCondition: RunningCondition
)

data class WeatherHeader(
    val locationName: String,
    val currentTemp: Int,
    val conditionText: String,
    val conditionIconUrl: String,
    val highTemp: Int,
    val lowTemp: Int,
    val feelsLike: Int
)

data class HourlyWeather(
    val time: String,
    val temp: Int,
    val iconUrl: String,
    val chanceOfRain: Int
)

data class DailyWeather(
    val dayName: String,
    val chanceOfRain: String,
    val highTemp: String,
    val lowTemp: String,
    val iconUrl: String
)

data class AqiData(
    val status: String,
    val progress: Float,
    val aqiIndex: Int
)

data class WeatherMetrics(
    val uvIndex: Float,
    val humidity: Int,
    val windKph: Float,
    val windDirection: String,
    val dewPoint: Int,
    val pressureMb: Float,
    val visibilityKm: Float
)

data class AstroData(
    val sunrise: String,
    val sunset: String,
    val moonrise: String,
    val moonset: String
)

data class RunningCondition(
    val status: String,
    val description: String
)