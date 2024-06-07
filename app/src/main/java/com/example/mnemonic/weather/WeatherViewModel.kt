package com.example.mnemonic.weather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Response

class WeatherViewModel (private val repository: WeatherRepository) : ViewModel() {
    private val _weatherResponse : MutableLiveData<Response<Weather>> = MutableLiveData()
    val weatherResponse get() = _weatherResponse
    fun getWeather(dataType : String, numOfRows : Int, pageNo : Int,
                   baseDate : String, baseTime : String, nx : String, ny : String){
        viewModelScope.launch {
            val response = repository.getWeather(dataType, numOfRows, pageNo, baseDate, baseTime, nx, ny)
            _weatherResponse.value = response
        }
    }
    fun getWeather(weatherRequest: WeatherRequest){
        viewModelScope.launch {
            val response = repository.getWeather(weatherRequest.dataType, weatherRequest.numOfRows, weatherRequest.pageNo,
                weatherRequest.baseDate, weatherRequest.baseTime, weatherRequest.nx, weatherRequest.ny)
            _weatherResponse.value = response
        }
    }
}