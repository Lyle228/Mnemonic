package com.example.mnemonic

import DateUtil.getCurrentDateFormatted
import DateUtil.getCurrentTimeFormatted
import android.content.ContentValues.TAG
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.ViewModelProvider
import com.example.mnemonic.util.GpsUtil.getCurrentUserGpsData
import com.example.mnemonic.weather.api.RetrofitInstance
import com.example.mnemonic.weather.repository.WeatherRepository
import com.example.mnemonic.weather.viewmodel.WeatherViewModel
import com.example.mnemonic.weather.viewmodel.WeatherViewModelFactory
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mnemonic.chatgpt.api.ChatGPTRetrofitInstance
import com.example.mnemonic.chatgpt.repository.ChatGPTRepository
import com.example.mnemonic.chatgpt.viewmodel.ChatGPTViewModel
import com.example.mnemonic.chatgpt.viewmodel.ChatGPTViewModelFactory
import com.example.mnemonic.ui.theme.MnemonicTheme
import com.example.mnemonic.weather.model.PrecipitationType
import com.example.mnemonic.weather.model.WeatherApiRequest
import com.example.mnemonic.weather.model.WeatherFormattedDataPerDay
import com.example.mnemonic.weather.model.WeatherType
import com.example.mnemonic.weather.util.WeatherCoordinateConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onStart(){
        super.onStart()
    }

    private lateinit var weatherViewModel: WeatherViewModel
    private lateinit var chatGPTViewModel: ChatGPTViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initWeatherViewModel()
        loadWeatherData() // 날씨 정보 획득
        initChatGPTViewModel()
        weatherViewModel.weatherFormattedDataPerDayList.observe(this) {
            requestAdviseFromChatGPT() // ChatGPT 분석 진행
        }
        setContent {
            MnemonicTheme {
                Column (
                    Modifier.fillMaxSize()

                ) {
                    MainApp(modifier = Modifier)
                }
            }
        }
    }
    private fun initWeatherViewModel() {
        val weatherApiService = RetrofitInstance.api;
        val repository = WeatherRepository(weatherApiService);
        val viewModelFactory = WeatherViewModelFactory(repository)
        weatherViewModel = ViewModelProvider(this, viewModelFactory)[WeatherViewModel::class.java]
    }
    private fun loadWeatherData() {
        /* 날씨 API 호출을 위한 현재 시간, 날짜 획득 */
        val baseDate = getCurrentDateFormatted("yyyyMMdd")

        /* 날씨 API 호출을 위한 GPS 정보 요청 */
        getCurrentUserGpsData(this) { location ->
            if (location == null) {
                Toast.makeText(this, "GPS 데이터를 불러오는데 실패했습니다", Toast.LENGTH_SHORT).show()
            } else{
                /* 날씨 API 호출 */
                weatherViewModel.getWeatherCurrentThreeDay(location, baseDate, getCurrentTimeFormatted("HH00"));

                /* 지역 이름 정보 요청 */
                weatherViewModel.getLocationName(this, location.latitude, location.longitude)
            }
        }
    }
    private fun initChatGPTViewModel(){
        val chatGPTApiService = ChatGPTRetrofitInstance.api
        val chatGPTRepository = ChatGPTRepository(chatGPTApiService)
        val chatGPTViewModelFactory = ChatGPTViewModelFactory(chatGPTRepository)
        chatGPTViewModel =
            ViewModelProvider(this, chatGPTViewModelFactory)[ChatGPTViewModel::class.java]
    }
    private fun requestAdviseFromChatGPT(){
        /* ChatGPT 호출 */
        chatGPTViewModel.getChatGPTResponse(weatherViewModel.weatherFormattedDataPerDayList.value?.get(0)?: return)
    }
}
