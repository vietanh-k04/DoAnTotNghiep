package com.example.doantotnghiep.data.repository

import com.example.doantotnghiep.data.remote.WeatherData

interface WeatherRepository {
    suspend fun getWeatherData(location: String): Result<WeatherData>
}