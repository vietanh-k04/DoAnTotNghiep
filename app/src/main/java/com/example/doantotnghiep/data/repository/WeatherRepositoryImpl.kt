package com.example.doantotnghiep.data.repository

import android.content.Context
import android.location.Geocoder
import com.example.doantotnghiep.API_KEY
import com.example.doantotnghiep.data.remote.AqiData
import com.example.doantotnghiep.data.remote.AstroData
import com.example.doantotnghiep.data.remote.DailyWeather
import com.example.doantotnghiep.data.remote.HourlyWeather
import com.example.doantotnghiep.data.remote.RunningCondition
import com.example.doantotnghiep.data.remote.WeatherData
import com.example.doantotnghiep.data.remote.WeatherHeader
import com.example.doantotnghiep.data.remote.WeatherMetrics
import com.example.doantotnghiep.data.response.WeatherResponse
import com.example.doantotnghiep.data.service.WeatherService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@Suppress("DEPRECATION")
class WeatherRepositoryImpl @Inject constructor(
    private val apiService: WeatherService,
    @ApplicationContext private val context: Context
) : WeatherRepository {
    override suspend fun getWeatherData(location: String): Result<WeatherData> {
        return try {
            val response = apiService.getWeatherForecast(
                apiKey = API_KEY,
                location = location
            )

            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!

                var customLocationName: String? = null
                try {
                    if (location.contains(",")) {
                        val parts = location.split(",")
                        val lat = parts[0].toDoubleOrNull()
                        val lon = parts[1].toDoubleOrNull()
                        
                        if (lat != null && lon != null) {
                            val geocoder = Geocoder(context, Locale("vi", "VN"))
                            val addresses = geocoder.getFromLocation(lat, lon, 1)
                            
                            if (!addresses.isNullOrEmpty()) {
                                val address = addresses[0]
                                val district = address.subAdminArea
                                val city = address.adminArea
                                
                                if (!district.isNullOrBlank()) {
                                    customLocationName = district
                                    if (!city.isNullOrBlank() && city != district) {
                                        customLocationName += ", $city"
                                    }
                                } else if (!city.isNullOrBlank()) {
                                    customLocationName = city
                                } else if (!address.locality.isNullOrBlank()) {
                                    customLocationName = address.locality
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                }

                Result.success(mapDtoToUiModel(dto, customLocationName))
            } else {
                Result.failure(Exception("Lỗi API: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapDtoToUiModel(dto: WeatherResponse, customLocationName: String?): WeatherData {
        val todayForecast = dto.forecast.forecastDay.firstOrNull()
        
        val header = WeatherHeader(
            locationName = customLocationName ?: dto.location.name,
            currentTemp = dto.current.tempC.toInt(),
            conditionText = dto.current.condition.text,
            conditionIconUrl = "https:${dto.current.condition.icon}",
            highTemp = todayForecast?.day?.maxTempC?.toInt() ?: 0,
            lowTemp = todayForecast?.day?.minTempC?.toInt() ?: 0,
            feelsLike = dto.current.feelsLikeC.toInt()
        )
        
        val allHours = dto.forecast.forecastDay.flatMap { it.hour }
        val currentHourPrefix = dto.location.localtime.substringBefore(":")
        val startIndex = allHours.indexOfFirst { it.time.startsWith(currentHourPrefix) }.takeIf { it >= 0 } ?: 0

        val currentHourDto = allHours.getOrNull(startIndex)
        val currentChanceOfRain = currentHourDto?.chanceOfRain ?: todayForecast?.day?.dailyChanceOfRain ?: 0

        val hourlyForecast = allHours.drop(startIndex).take(24).map { hourDto ->
            HourlyWeather(
                time = hourDto.time.substringAfter(" "),
                temp = hourDto.tempC.toInt(),
                iconUrl = "https:${hourDto.condition.icon}",
                chanceOfRain = hourDto.chanceOfRain
            )
        }
        
        val dailyForecast = dto.forecast.forecastDay.mapIndexed { index, dayDto ->
            val dayName = if (index == 0) "Hôm nay" else getDayOfWeek(dayDto.date)
            val chanceOfRain = if (index == 0) "$currentChanceOfRain%" else "${dayDto.day.dailyChanceOfRain}%"
            val iconUrl = if (index == 0) "https:${dto.current.condition.icon}" else "https:${dayDto.day.condition.icon}"
            
            DailyWeather(
                dayName = dayName,
                chanceOfRain = chanceOfRain,
                highTemp = "${dayDto.day.maxTempC.toInt()}°",
                lowTemp = "${dayDto.day.minTempC.toInt()}°",
                iconUrl = iconUrl
            )
        }
        
        val aqiIndex = dto.current.airQuality?.usEpaIndex ?: 1
        val aqi = AqiData(
            status = if (aqiIndex <= 2) "Tốt" else if (aqiIndex <= 4) "Trung bình" else "Không tốt",
            progress = aqiIndex / 6f,
            aqiIndex = aqiIndex
        )
        
        val metrics = WeatherMetrics(
            uvIndex = dto.current.uv.toFloat(),
            humidity = dto.current.humidity,
            windKph = dto.current.windKph.toFloat(),
            windDirection = dto.current.windDir,
            dewPoint = todayForecast?.hour?.firstOrNull()?.dewpointC?.toInt() ?: 0,
            pressureMb = dto.current.pressureMb.toFloat(),
            visibilityKm = dto.current.visKm.toFloat()
        )
        
        val astro = AstroData(
            sunrise = todayForecast?.astro?.sunrise ?: "--:--",
            sunset = todayForecast?.astro?.sunset ?: "--:--",
            moonrise = todayForecast?.astro?.moonrise ?: "--:--",
            moonset = todayForecast?.astro?.moonset ?: "--:--"
        )
        
        val isGoodToRun = dto.current.tempC in 18.0..28.0 && (todayForecast?.day?.dailyChanceOfRain ?: 0) < 30
        val runningCondition = RunningCondition(
            status = if (isGoodToRun) "Tốt" else "Không thuận lợi",
            description = if (isGoodToRun) "Thời tiết khá để chạy bộ ngay lúc này" else "Nên cân nhắc chạy trong nhà"
        )

        return WeatherData(header, hourlyForecast, dailyForecast, aqi, metrics, astro, runningCondition)
    }
    
    private fun getDayOfWeek(dateString: String): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = format.parse(dateString)
            if (date != null) {
                val outFormat = SimpleDateFormat("EEE", Locale("vi", "VN"))
                outFormat.format(date).replaceFirstChar { it.uppercase() }
            } else {
                dateString
            }
        } catch (_: Exception) {
            dateString
        }
    }
}