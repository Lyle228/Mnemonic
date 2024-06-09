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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class WeatherViewModel (private val repository: WeatherRepository) : ViewModel() {
    private val _weatherFormattedDataPerDayList: MutableLiveData<MutableList<WeatherFormattedDataPerDay>> = MutableLiveData()
    val weatherFormattedDataPerDayList: LiveData<MutableList<WeatherFormattedDataPerDay>>
        get() = _weatherFormattedDataPerDayList
    fun getWeatherCurrentThreeDay(dataType : String, baseDate : String, nx : String, ny : String){
        viewModelScope.launch {
            val numOfRows = 12 * 24 * 3// 기상청 API는 1시간에 12개의 값을 리턴해준다.
            val pageNo = 1
            val response = withContext(Dispatchers.IO) {
                repository.getWeather(
                    dataType,
                    numOfRows,
                    pageNo,
                    baseDate,
                    "0200",
                    nx,
                    ny
                )
            }
            val formattedData = withContext(Dispatchers.Default) {
                _weatherFormattedDataPerDayList.value
            }
            _weatherFormattedDataPerDayList.value = formattedData
        }
    }
    fun getWeatherCurrentThreeDay(weatherRequest: WeatherApiRequest){
        viewModelScope.launch {
            val numOfRows = 12 * 24 * 3// 기상청 API는 1시간에 12개의 값을 리턴해준다.
            val response = withContext(Dispatchers.IO) {
                val pageNo = 1
                repository.getWeather(
                    weatherRequest.dataType,
                    numOfRows,
                    pageNo,
                    weatherRequest.baseDate,
                    "0200",
                    weatherRequest.nx,
                    weatherRequest.ny
                )
            }
            val formattedData = withContext(Dispatchers.Default) {
                formatResponse(response)
            }
            _weatherFormattedDataPerDayList.value = formattedData
        }
    }

    private fun formatResponse(response: Response<Weather>): MutableList<WeatherFormattedDataPerDay> {
        val items = response.body()?.response?.body?.items?.item ?: emptyList()
        val weatherFormattedDataPerDayList = mutableListOf<WeatherFormattedDataPerDay>()
        var weatherFormattedDataPerDay = WeatherFormattedDataPerDay()
        var weatherFormattedDataPerHour = WeatherFormattedDataPerHour()
        var preDate = "start"
        var preTime = "start"
        for (item in items){
            if(preTime == "start"){
                preTime = item.fcstTime
            }
            if(preDate == "start"){
                preDate = item.fcstDate
            }
            /*
            Json의 정보를 기본적으로 1시간 단위로 나눠 저장한다.
            1시간 동안의 정보를 WeatherFormattedDataPerHour이라고 저장하며 예측 시간이 바꼈으면,
            해당 WeatherFormattedDataPerHour는 weatherFormattedDataPerDay의 WeatherFormattedDataPerHourMap에 (key : 예측시간) 추가하고
            WeatherFormattedDataPerHour는 초기화해 새로운 시간 정보를 저장할 수 있도록 한다.

            날짜가 변경됐을 때는 해당 weatherFormattedDataPerDay를 weatherFormattedDataPerDayList에 추가하고
            새로운 날짜의 정보를 담을 수 있도록 weatherFormattedDataPerDay를 초기화해준다.
            */
            if(preTime != item.fcstTime) {
                if (preDate != item.fcstDate) {
                    weatherFormattedDataPerDay.weatherFormattedDataPerHourMap[preTime] = weatherFormattedDataPerHour
                    weatherFormattedDataPerDayList.add(weatherFormattedDataPerDay)
                    weatherFormattedDataPerHour = WeatherFormattedDataPerHour()
                    weatherFormattedDataPerDay = WeatherFormattedDataPerDay()
                    preTime = item.fcstTime
                    preDate = item.fcstDate
                } else{
                    weatherFormattedDataPerDay.weatherFormattedDataPerHourMap[preTime] = weatherFormattedDataPerHour
                    weatherFormattedDataPerHour = WeatherFormattedDataPerHour()
                    preTime = item.fcstTime
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