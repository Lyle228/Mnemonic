package com.example.mnemonic.weather.api

import com.example.mnemonic.MainConstants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(MainConstants.WEATHER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}