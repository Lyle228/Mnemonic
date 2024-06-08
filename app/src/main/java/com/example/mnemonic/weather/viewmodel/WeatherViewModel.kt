package com.example.mnemonic.weather.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mnemonic.weather.repository.WeatherRepository
import com.example.mnemonic.weather.model.PrecipitationType
import com.example.mnemonic.weather.model.Weather
import com.example.mnemonic.weather.model.WeatherApiRequest
import com.example.mnemonic.weather.model.WeatherFormattedDataPerDay
import com.example.mnemonic.weather.model.WeatherFormattedDataPerHour
import com.example.mnemonic.weather.model.WeatherType
import kotlinx.coroutines.launch
import retrofit2.Response

class WeatherViewModel (private val repository: WeatherRepository) : ViewModel() {
    private val _weatherFormattedDataPerDayList: MutableLiveData<MutableList<WeatherFormattedDataPerDay>> = MutableLiveData()
    val weatherFormattedDataPerDayList: LiveData<MutableList<WeatherFormattedDataPerDay>>
        get() = _weatherFormattedDataPerDayList
    fun getWeatherCurrentThreeDay(dataType : String, baseDate : String, nx : String, ny : String){
        val numOfRows = 12 * 24 * 3// 기상청 API는 1시간에 12개의 값을 리턴해준다.
        viewModelScope.launch {
            val pageNo = 1
            val response =
                repository.getWeather(
                    dataType,
                    numOfRows,
                    pageNo,
                    baseDate,
                    "0200",
                    nx,
                    ny
                )
            _weatherFormattedDataPerDayList.value = formatResponse(response)
        }
    }
    fun getWeatherCurrentThreeDay(weatherRequest: WeatherApiRequest){
        val numOfRows = 12 * 24 * 3// 기상청 API는 1시간에 12개의 값을 리턴해준다.
        viewModelScope.launch {
            Log.d("test", "${weatherRequest.nx} ${weatherRequest.ny}")
            val pageNo = 1
            val response =
                repository.getWeather(
                    weatherRequest.dataType,
                    numOfRows,
                    pageNo,
                    weatherRequest.baseDate,
                    "0200",
                    "57",
                    "127"
                )
            _weatherFormattedDataPerDayList.value = formatResponse(response)
        }
    }

    private fun formatResponse(response: Response<Weather>): MutableList<WeatherFormattedDataPerDay> {
        val items = response.body()?.response?.body?.items?.item ?: emptyList()
        val weatherFormattedDataPerDayList = mutableListOf<WeatherFormattedDataPerDay>()
        var weatherFormattedDataPerDay = WeatherFormattedDataPerDay()
        var weatherFormattedDataPerHour = WeatherFormattedDataPerHour()
        var preHour = "start"
        var preDate = "start"
        for (item in items){
            if(preHour == "start" && preDate == "start"){
                preHour = item.fcstTime
                preDate = item.fcstDate
            }
            if(item.fcstTime != preHour ){
                if(preDate != item.fcstDate) {
                    weatherFormattedDataPerDay.weatherFormattedDataPerHourList.add(
                        weatherFormattedDataPerHour
                    )
                    weatherFormattedDataPerDayList.add(weatherFormattedDataPerDay)
                    weatherFormattedDataPerHour = WeatherFormattedDataPerHour()
                    weatherFormattedDataPerDay = WeatherFormattedDataPerDay()
                    preHour = item.fcstTime
                    preDate = item.fcstDate
                } else {
                    weatherFormattedDataPerDay.weatherFormattedDataPerHourList.add(
                        weatherFormattedDataPerHour
                    )
                    weatherFormattedDataPerHour = WeatherFormattedDataPerHour()
                    preHour = item.fcstTime
                }
            }
            weatherFormattedDataPerHour.category = item.category
            weatherFormattedDataPerHour.baseDate = item.baseDate
            weatherFormattedDataPerHour.baseTime = item.baseTime
            weatherFormattedDataPerHour.fcstDate = item.fcstDate
            weatherFormattedDataPerHour.fcstTime = item.fcstTime

            when(item.category){
                "POP" -> weatherFormattedDataPerHour.precipitationProbability = item.fcstValue.toIntOrNull()
                "PTY" -> {
                    val value = item.fcstValue.toIntOrNull()
                    when(value){
                        0 -> weatherFormattedDataPerHour.precipitationType = PrecipitationType.None
                        1 -> weatherFormattedDataPerHour.precipitationType = PrecipitationType.Rain
                        2 -> weatherFormattedDataPerHour.precipitationType = PrecipitationType.SnowRain
                        3 -> weatherFormattedDataPerHour.precipitationType = PrecipitationType.Snow
                        4 -> weatherFormattedDataPerHour.precipitationType = PrecipitationType.Shower
                        else -> weatherFormattedDataPerHour.precipitationType = null
                    }
                }
                "PCP" -> weatherFormattedDataPerHour.precipitationAmount = item.fcstValue.toDoubleOrNull()
                "REH" -> weatherFormattedDataPerHour.humidity = item.fcstValue.toDoubleOrNull()
                "SNO" -> weatherFormattedDataPerHour.snowfallAmount = item.fcstValue.toIntOrNull()
                "SKY" -> {
                    val value = item.fcstValue.toIntOrNull()
                    when(value) {
                        1 -> weatherFormattedDataPerHour.weatherType = WeatherType.Sunny
                        3 -> weatherFormattedDataPerHour.weatherType = WeatherType.cloudy
                        4 -> weatherFormattedDataPerHour.weatherType = WeatherType.cloudyWeather
                    }
                }
                "TMP" -> {
                    val value = item.fcstValue.toIntOrNull()
                    Log.d("test", "$value ${item.fcstDate} ${item.fcstTime}")
                    if(value != null) {
                        weatherFormattedDataPerHour.temperature = value
                        if (value < weatherFormattedDataPerDay.minimumTemperature) {
                            weatherFormattedDataPerDay.minimumTemperature = value
                        }
                        if (value > weatherFormattedDataPerDay.maximumTemperature) {
                            weatherFormattedDataPerDay.maximumTemperature = value
                        }
                    }
                }
                "TMN" -> weatherFormattedDataPerHour.temperatureLow = item.fcstValue.toDoubleOrNull()
                "TMX" -> weatherFormattedDataPerHour.temperatureHigh = item.fcstValue.toDoubleOrNull()
                "UUU" -> weatherFormattedDataPerHour.eastWestWindSpeed = item.fcstValue.toDoubleOrNull()
                "VVV" -> weatherFormattedDataPerHour.northSouthWindSpeed = item.fcstValue.toDoubleOrNull()
                "WAV" -> weatherFormattedDataPerHour.waveHeight = item.fcstValue.toDoubleOrNull()
                "VEC" -> weatherFormattedDataPerHour.windDirection = item.fcstValue.toDoubleOrNull()
                "WSD" -> weatherFormattedDataPerHour.windSpeed = item.fcstValue.toDoubleOrNull()
            }
        }
        return weatherFormattedDataPerDayList
    }
}