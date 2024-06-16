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
                    Modifier.fillMaxWidth()
                ) {
                    TodayWeatherInformation(modifier = Modifier, weatherViewModel)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(60.dp)) {
                        ForecastWeatherInformation(viewModel = weatherViewModel, dayLater = 1, modifier = Modifier.weight(1f))
                        ForecastWeatherInformation(viewModel = weatherViewModel, dayLater = 2, modifier = Modifier.weight(1f))
                    }
                    CautionMessage(viewModel = chatGPTViewModel)
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
                val weatherRequest = convertToApiParams(location, baseDate, getCurrentTimeFormatted("HH00"))
                /* 날씨 API 호출 */
                weatherViewModel.getWeatherCurrentThreeDay(weatherRequest)

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
        val chatGPTQuestions =
            convertToChatGPTQuestions(weatherViewModel.weatherFormattedDataPerDayList.value?.get(0)?: return)
        chatGPTViewModel.getChatGPTResponse(chatGPTQuestions)
    }
    private fun convertToChatGPTQuestions(weatherFormattedDataPerDay: WeatherFormattedDataPerDay): String{
        var question = ""
        for(weatherFormattedData in weatherFormattedDataPerDay.weatherFormattedDataPerHourMap){
            val weatherInfo = weatherFormattedData.value
            question += "${weatherFormattedData.key}시 에는"
            question += "강수확률 ${weatherInfo.precipitationProbability}%이고, "
            when(weatherInfo.precipitationType) {
                PrecipitationType.Rain -> question += "비가 예보 돼있고 강수량은 ${weatherInfo.precipitationAmount}로 예상돼."
                PrecipitationType.Snow -> question += "눈이 예보 돼있고 적설량은 ${weatherInfo.snowfallAmount}로 예상돼."
                PrecipitationType.SnowRain -> question += "눈과 비가 예보 돼있어."
                PrecipitationType.Shower -> question += "소나기 예보 돼있고 강수량은 ${weatherInfo.precipitationAmount}로 예상돼."
                else -> when(weatherInfo.weatherType){
                    WeatherType.Sunny -> question += "맑은 날씨야."
                    WeatherType.cloudy-> question += "구름이 조금 많아."
                    WeatherType.cloudyWeather -> question += "흐린 날씨야."
                    else -> question += "날씨가 예보 돼있지 않아."
                }
            }
            question += "습도는 ${weatherInfo.humidity}%이고, 기온은 ${weatherInfo.temperature}도야. "
            question += "바람 세기는 ${weatherInfo.windSpeed}m/s야. "

        }
        question += "이 정보를 바탕으로 의미가 있는 분석을 해서 오늘 하루를 시작하는 나에게 100자 이하의 조언을 존댓말로 해줘."
        Log.d(TAG, "convertToChatGPTQuestions: $question")
        return question
    }
    private fun convertToApiParams(location: Location, date: String, time: String ): WeatherApiRequest {
        var convertedDate = date
        when (time) {
            "0000", "0100" -> {
                val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                val originalDate = LocalDate.parse(date, formatter)
                convertedDate = originalDate.minusDays(1).format(formatter)
            }
        }
        val converter = WeatherCoordinateConverter()
        val convertedLocation = converter.convertToXy(location.latitude, location.longitude)
        return WeatherApiRequest(baseDate = convertedDate, nx = convertedLocation.nx.toString(), ny = convertedLocation.ny.toString())
    }
}

/*
fun convertToApiParams(location: Location, date: String, time: String ): WeatherRequestForThreeDay{
    var convertedTime = "0500"
    var convertedDate = date
    when (time) {
        "0200", "0300", "0400" -> convertedTime = "0200"
        "0500", "0600", "0700" -> convertedTime = "0500"
        "0800", "0900", "1000" -> convertedTime = "0800"
        "1100", "1200", "1300" -> convertedTime = "1100"
        "1400", "1500", "1600" -> convertedTime = "1400"
        "1700", "1800", "1900" -> convertedTime = "1700"
        "2000", "2100", "2200" -> convertedTime = "2000"
        "2300" -> convertedTime = "2300"
        "0000", "0100" -> {
            convertedTime = "2300"
            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
            val originalDate = LocalDate.parse(date, formatter)
            convertedDate = originalDate.minusDays(1).format(formatter)
        }
    }
    return WeatherRequestForThreeDay(baseDate = convertedDate, nx = location.latitude.toInt().toString(), ny = location.longitude.toInt().toString())
}
*/