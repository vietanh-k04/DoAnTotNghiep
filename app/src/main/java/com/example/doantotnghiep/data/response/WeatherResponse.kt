package com.example.doantotnghiep.data.response

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("location") val location: LocationDto,
    @SerializedName("current") val current: CurrentDto,
    @SerializedName("forecast") val forecast: ForecastDto
)

data class LocationDto(
    @SerializedName("name") val name: String,
    @SerializedName("localtime") val localtime: String
)

data class CurrentDto(
    @SerializedName("temp_c") val tempC: Double,
    @SerializedName("condition") val condition: ConditionDto,
    @SerializedName("wind_kph") val windKph: Double,
    @SerializedName("wind_dir") val windDir: String,
    @SerializedName("pressure_mb") val pressureMb: Double,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("feelslike_c") val feelsLikeC: Double,
    @SerializedName("vis_km") val visKm: Double,
    @SerializedName("uv") val uv: Double,
    @SerializedName("air_quality") val airQuality: AirQualityDto?
)

data class ConditionDto(
    @SerializedName("text") val text: String,
    @SerializedName("icon") val icon: String
)

data class AirQualityDto(
    @SerializedName("us-epa-index") val usEpaIndex: Int?
)

data class ForecastDto(
    @SerializedName("forecastday") val forecastDay: List<ForecastDayDto>
)

data class ForecastDayDto(
    @SerializedName("date") val date: String,
    @SerializedName("day") val day: DayDto,
    @SerializedName("astro") val astro: AstroDto,
    @SerializedName("hour") val hour: List<HourDto>
)

data class DayDto(
    @SerializedName("maxtemp_c") val maxTempC: Double,
    @SerializedName("mintemp_c") val minTempC: Double,
    @SerializedName("daily_chance_of_rain") val dailyChanceOfRain: Int,
    @SerializedName("condition") val condition: ConditionDto
)

data class AstroDto(
    @SerializedName("sunrise") val sunrise: String,
    @SerializedName("sunset") val sunset: String,
    @SerializedName("moonrise") val moonrise: String,
    @SerializedName("moonset") val moonset: String
)

data class HourDto(
    @SerializedName("time") val time: String,
    @SerializedName("temp_c") val tempC: Double,
    @SerializedName("condition") val condition: ConditionDto,
    @SerializedName("chance_of_rain") val chanceOfRain: Int,
    @SerializedName("dewpoint_c") val dewpointC: Double
)