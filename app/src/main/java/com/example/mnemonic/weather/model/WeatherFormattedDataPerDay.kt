package com.example.mnemonic.weather.model

data class WeatherFormattedDataPerDay (
    val weatherFormattedDataPerHourList: MutableList<WeatherFormattedDataPerHour> = mutableListOf(),
    var maximumTemperature: Int = Int.MIN_VALUE,
    var minimumTemperature: Int = Int.MAX_VALUE
)