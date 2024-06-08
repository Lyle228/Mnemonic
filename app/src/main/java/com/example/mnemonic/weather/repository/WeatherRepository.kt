package com.example.mnemonic.weather.repository

import com.example.mnemonic.weather.api.WeatherApiService
import com.example.mnemonic.weather.model.Weather
import retrofit2.Response

class WeatherRepository(private val weatherApi: WeatherApiService){
    suspend fun getWeather(
        dataType : String, numOfRows : Int, pageNo : Int,
        baseDate : String, baseTime : String, nx : String, ny : String) : Response<Weather> {
        return weatherApi.getWeather(dataType, numOfRows, pageNo, baseDate, baseTime, nx, ny)
    }
}