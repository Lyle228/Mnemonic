package com.example.mnemonic.weather.repository

import android.location.Location
import com.example.mnemonic.weather.api.WeatherApiService
import com.example.mnemonic.weather.model.Weather
import com.example.mnemonic.weather.model.WeatherApiRequest
import com.example.mnemonic.weather.util.WeatherCoordinateConverter
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class WeatherRepository(private val weatherApi: WeatherApiService){
    suspend fun getWeather(
        dataType : String, numOfRows : Int, pageNo : Int,
        baseDate : String, baseTime : String, nx : String, ny : String) : Response<Weather> {
        return weatherApi.getWeather(dataType, numOfRows, pageNo, baseDate, baseTime, nx, ny)
    }

    fun convertToApiParams(location: Location, date: String, time: String): WeatherApiRequest {
        var convertedDate = date
        when (time) {
            "0000", "0100" -> {
                val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                val originalDate = LocalDate.parse(date, formatter)
                convertedDate = originalDate.minusDays(1).format(formatter)
            }
        }
        val converter = WeatherCoordinateConverter()
        val convertedLocation = converter.convertToXy(location.latitude, location.longitude)
        return WeatherApiRequest(baseDate = convertedDate, nx = convertedLocation.nx.toString(), ny = convertedLocation.ny.toString())
    }
}