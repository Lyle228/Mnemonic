package com.example.mnemonic.weather.viewmodel

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
        val numOfRows = 12 // 기상청 API는 1시간에 12개의 값을 리턴해준다.
        val weatherFormattedDataPerDayList:MutableList<WeatherFormattedDataPerDay> = mutableListOf()

        viewModelScope.launch {
            val weatherFormattedDataPerDay = WeatherFormattedDataPerDay()
            for(day in 1 .. 3) {
                for (hour in 0..23) {
                    val pageNo = (day - 1) * 24 + hour / 3 + 1
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
                    val weatherFormattedDataPerHour = formatResponse(response)
                    weatherFormattedDataPerDay.weatherFormattedDataPerHourList.add(weatherFormattedDataPerHour)

                    val newTemperature = weatherFormattedDataPerHour.temperature
                    if(newTemperature != null){
                        if(weatherFormattedDataPerDay.maximumTemperature < newTemperature){
                            weatherFormattedDataPerDay.maximumTemperature = newTemperature
                        }
                        if(weatherFormattedDataPerDay.minimumTemperature > newTemperature){
                            weatherFormattedDataPerDay.minimumTemperature = newTemperature
                        }
                    }
                }
                weatherFormattedDataPerDayList.add(weatherFormattedDataPerDay)
            }
            _weatherFormattedDataPerDayList.value = weatherFormattedDataPerDayList
        }
    }
    fun getWeatherCurrentThreeDay(weatherRequest: WeatherApiRequest){
        val numOfRows = 12 // 기상청 API는 1시간에 12개의 값을 리턴해준다.
        val weatherFormattedDataPerDayList:MutableList<WeatherFormattedDataPerDay> = mutableListOf()

        viewModelScope.launch {
            val weatherFormattedDataPerDay = WeatherFormattedDataPerDay()
            for(day in 1 .. 3) {
                for (hour in 0..23) {
                    val pageNo = (day - 1) * 24 + hour / 3 + 1
                    val response =
                        repository.getWeather(
                            weatherRequest.dataType,
                            numOfRows,
                            pageNo,
                            weatherRequest.baseDate,
                            "0200",
                            weatherRequest.nx,
                            weatherRequest.ny
                        )
                    val weatherFormattedDataPerHour = formatResponse(response)
                    weatherFormattedDataPerDay.weatherFormattedDataPerHourList.add(weatherFormattedDataPerHour)

                    val newTemperature = weatherFormattedDataPerHour.temperature
                    if(newTemperature != null){
                        if(weatherFormattedDataPerDay.maximumTemperature < newTemperature){
                            weatherFormattedDataPerDay.maximumTemperature = newTemperature
                        }
                        if(weatherFormattedDataPerDay.minimumTemperature > newTemperature){
                            weatherFormattedDataPerDay.minimumTemperature = newTemperature
                        }
                    }
                }
                weatherFormattedDataPerDayList.add(weatherFormattedDataPerDay)
            }
            _weatherFormattedDataPerDayList.value = weatherFormattedDataPerDayList
        }
    }

    private fun formatResponse(response: Response<Weather>): WeatherFormattedDataPerHour {
        val items = response.body()?.response?.body?.items?.item ?: emptyList()

        val weatherFormattedDataPerHour = WeatherFormattedDataPerHour()
        for (item in items){
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
                "TMP" -> weatherFormattedDataPerHour.temperature = item.fcstValue.toDoubleOrNull()
                "TMN" -> weatherFormattedDataPerHour.temperatureLow = item.fcstValue.toDoubleOrNull()
                "TMX" -> weatherFormattedDataPerHour.temperatureHigh = item.fcstValue.toDoubleOrNull()
                "UUU" -> weatherFormattedDataPerHour.eastWestWindSpeed = item.fcstValue.toDoubleOrNull()
                "VVV" -> weatherFormattedDataPerHour.northSouthWindSpeed = item.fcstValue.toDoubleOrNull()
                "WAV" -> weatherFormattedDataPerHour.waveHeight = item.fcstValue.toDoubleOrNull()
                "VEC" -> weatherFormattedDataPerHour.windDirection = item.fcstValue.toDoubleOrNull()
                "WSD" -> weatherFormattedDataPerHour.windSpeed = item.fcstValue.toDoubleOrNull()
            }
        }
        return weatherFormattedDataPerHour
    }
}