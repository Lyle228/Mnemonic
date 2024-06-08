package com.example.mnemonic.weather.model

data class WeatherFormattedDataPerDay (
    val weatherFormattedDataPerHourList: MutableList<WeatherFormattedDataPerHour> = mutableListOf(),
    var maximumTemperature: Double = Double.MIN_VALUE,
    var minimumTemperature: Double = Double.MAX_VALUE
)