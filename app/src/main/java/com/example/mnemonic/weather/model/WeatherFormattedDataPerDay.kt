package com.example.mnemonic.weather.model

data class WeatherFormattedDataPerDay (
    val weatherFormattedDataPerHourMap: MutableMap<String, WeatherFormattedDataPerHour> = mutableMapOf(),
    var maximumTemperature: Int = Int.MIN_VALUE,
    var minimumTemperature: Int = Int.MAX_VALUE
)