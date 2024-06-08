package com.example.mnemonic.weather.model

data class WeatherFormattedDataPerHour(
    var precipitationProbability: Int? = 0,
    var precipitationType: PrecipitationType? = PrecipitationType.None,
    var precipitationAmount: Double? = 0.0,
    var humidity: Double? = 0.0,
    var snowfallAmount: Int? = 0,
    var weatherType: WeatherType? = WeatherType.Sunny,
    var temperature: Int? = 0,
    var temperatureLow: Double? = 0.0,  // no use
    var temperatureHigh: Double? = 0.0, // no use
    var eastWestWindSpeed: Double? = 0.0,
    var northSouthWindSpeed: Double? = 0.0,
    var waveHeight: Double? = 0.0,
    var windDirection: Double? = 0.0,
    var windSpeed: Double? = 0.0,
    var baseDate: String? = "",
    var baseTime: String? = "",
    var fcstDate: String? = "",
    var fcstTime: String? = "",
    var category: String? = ""
)

enum class WeatherType {
    Sunny, // 맑음
    cloudy, // 구름 많음
    cloudyWeather// 흐림
}

enum class PrecipitationType() {
    None,
    Rain,
    SnowRain,
    Snow,
    Shower
}