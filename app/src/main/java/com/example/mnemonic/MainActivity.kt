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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.lifecycle.ViewModelProvider
import com.example.mnemonic.util.GpsUtil.getCurrentUserGpsData
import com.example.mnemonic.weather.api.RetrofitInstance
import com.example.mnemonic.weather.repository.WeatherRepository
import com.example.mnemonic.weather.viewmodel.WeatherViewModel
import com.example.mnemonic.weather.viewmodel.WeatherViewModelFactory
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mnemonic.ui.theme.MnemonicTheme
import com.example.mnemonic.weather.model.WeatherApiRequest
import com.example.mnemonic.weather.model.WeatherFormattedDataPerDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onStart(){
        super.onStart()
    }

    private lateinit var viewModel: WeatherViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val weatherApiService = RetrofitInstance.api;
        val repository = WeatherRepository(weatherApiService);
        val viewModelFactory = WeatherViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[WeatherViewModel::class.java]

        /* 날씨 API 호출을 위한 현재 시간, 날짜 획득 */
        val baseDate = getCurrentDateFormatted("yyyyMMdd")

        /* 날씨 API 호출을 위한 GPS 정보 요청 */
        getCurrentUserGpsData(this) { location ->
            if (location == null) {
                Toast.makeText(this, "GPS 데이터를 불러오는데 실패했습니다", Toast.LENGTH_SHORT).show()
            } else{
                val weatherRequest = convertToApiParams(location, baseDate, getCurrentTimeFormatted("HH00"))
                /* 날씨 API 호출 */
                viewModel.getWeatherCurrentThreeDay(weatherRequest)
            }
        }
        setContent {
            MnemonicTheme {
                Test(modifier = Modifier, viewModel)
            }
        }
    }
}

fun convertToApiParams(location: Location, date: String, time: String ): WeatherApiRequest {
    var convertedDate = date
    when (time) {
        "0000", "0100" -> {
            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
            val originalDate = LocalDate.parse(date, formatter)
            convertedDate = originalDate.minusDays(1).format(formatter)
        }
    }
    return WeatherApiRequest(baseDate = convertedDate, nx = location.latitude.toInt().toString(), ny = location.longitude.toInt().toString())
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
/*
@Composable
fun WeatherArea(weatherViewModel : WeatherViewModel, modifier: Modifier = Modifier) {
    var testValue = weatherViewModel.weatherResponse
    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Row(modifier = Modifier.padding(24.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "zz")
            }
            ElevatedButton(
                onClick = { weatherViewModel.getWeather("JSON", 14, 1, 20240602, 1100, "63", "89") }
            ) {
                Text("Show more")
            }
        }
    }
}
*/
/*
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MnemonicTheme {
        //WeatherArea()
    }
}*/

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MnemonicTheme {
        val weatherFormattedDataPerDay: WeatherFormattedDataPerDay = WeatherFormattedDataPerDay(maximumTemperature = 30.0, minimumTemperature = 25.0)
        //Test(modifier = Modifier, weatherFormattedDataPerDay)
    }
}

@Composable
fun Test(modifier: Modifier = Modifier, viewModel: WeatherViewModel){
    val weatherFormattedDataPerDayList by viewModel.weatherFormattedDataPerDayList.observeAsState()
    val maxTemperature = weatherFormattedDataPerDayList?.getOrNull(0)?.maximumTemperature ?: 0.0
    val minTemperature = weatherFormattedDataPerDayList?.getOrNull(0)?.minimumTemperature ?: 0.0
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "서울",
            fontSize = 17.sp
        )
        Text(
            text = "19°C",
            fontSize = 30.sp
        )
        Text(
            text = "흐림",
            fontSize = 10.sp
        )
        Text(
            text = "최고:${maxTemperature}°C 최저: ${minTemperature}°C",
            fontSize = 10.sp
        )
    }
}