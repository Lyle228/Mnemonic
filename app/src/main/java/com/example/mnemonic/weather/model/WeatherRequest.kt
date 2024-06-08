package com.example.mnemonic.weather.model

data class WeatherApiRequest(
    val dataType: String = "JSON",
    val numOfRows: Int = 12,
    val pageNo: Int = 1,
    val baseDate: String,
    val nx: String = "37",
    val ny: String = "127"
)