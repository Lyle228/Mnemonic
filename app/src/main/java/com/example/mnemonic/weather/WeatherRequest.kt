package com.example.mnemonic.weather

data class WeatherRequest(
    val dataType: String = "JSON",
    val numOfRows: Int = 14,
    val pageNo: Int = 1,
    val baseDate: String,
    val baseTime: String,
    val nx: String = "37",
    val ny: String = "127"
)