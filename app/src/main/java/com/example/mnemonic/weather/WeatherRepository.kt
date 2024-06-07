package com.example.mnemonic.weather

import com.example.mnemonic.apikey.ApiKey.Companion.WEATHER_API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

class WeatherRepository(private val weatherApi: WeatherApiService){
    suspend fun getWeather(
        dataType : String, numOfRows : Int, pageNo : Int,
        baseDate : Int, baseTime : Int, nx : String, ny : String) : Response<Weather> {
        return weatherApi.getWeather(dataType, numOfRows, pageNo, baseDate, baseTime, nx, ny)
    }
}