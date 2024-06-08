package com.example.mnemonic.weather.api

import com.example.mnemonic.apikey.ApiKey
import com.example.mnemonic.weather.model.Weather
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("getVilageFcst?serviceKey=${ApiKey.WEATHER_API_KEY}")
    suspend fun getWeather(
        @Query("dataType") dataType : String,
        @Query("numOfRows") numOfRows : Int,
        @Query("pageNo") pageNo : Int,
        @Query("base_date") baseDate : String,
        @Query("base_time") baseTime : String,
        @Query("nx") nx : String,
        @Query("ny") ny : String
    ) : Response<Weather>
}