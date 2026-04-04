package com.example.doantotnghiep.data.service

import com.example.doantotnghiep.data.response.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("forecast.json")
    suspend fun getWeatherForecast(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("days") days: Int = 5,
        @Query("aqi") aqi: String = "yes",
        @Query("lang") lang: String = "vi"
    ): Response<WeatherResponse>
}