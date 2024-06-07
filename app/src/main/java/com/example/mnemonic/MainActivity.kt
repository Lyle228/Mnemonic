package com.example.mnemonic

import DateUtil.getCurrentDateFormatted
import DateUtil.getCurrentTimeFormatted
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.mnemonic.util.GpsUtil.getCurrentUserGpsData
import com.example.mnemonic.weather.RetrofitInstance
import com.example.mnemonic.weather.WeatherRepository
import com.example.mnemonic.weather.WeatherViewModel
import com.example.mnemonic.weather.WeatherViewModelFactory
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mnemonic.ui.theme.MnemonicTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

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
        val baseDate = getCurrentDateFormatted("yyyyMMdd").toIntOrNull()
        val baseTime = timeChangeForWeatherApi(getCurrentTimeFormatted("HH00"))

        /* 날씨 API 호출을 위한 GPS 정보 요청 */
        getCurrentUserGpsData(this) { location ->
            if (location == null) {
                Toast.makeText(this, "GPS 데이터를 불러오는데 실패했습니다", Toast.LENGTH_SHORT).show()
            } else{
                /* 날씨 API 호출 */
                if (baseDate != null && baseTime != null) {
                    Log.d(TAG, "ㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋ $baseDate, $baseTime, $location  ")
                    viewModel.getWeather(
                        "JSON",
                        14,
                        1,
                        baseDate,
                        baseTime,
                        location.longitude.toString(),
                        location.latitude.toString()
                    )
                    viewModel.weatherResponse.observe(this) {
                        for (i in it.body()?.response!!.body.items.item) {
                            Log.d(TAG, "$i")
                        }
                    }
                } else {
                    Toast.makeText(this, "기상 정보를 불러오는데 실패했습니다", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "ㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋ $location")
                }
            }
        }
    }
}


fun timeChangeForWeatherApi(time: String):Int?{
    var convertedTime = "";
    when (time) {
        "0200", "0300", "0400" -> convertedTime = "0200"
        "0500", "0600", "0700" -> convertedTime = "0500"
        "0800", "0900", "1000" -> convertedTime = "0800"
        "1100", "1200", "1300" -> convertedTime = "1100"
        "1400", "1500", "1600" -> convertedTime = "1400"
        "1700", "1800", "1900" -> convertedTime = "1700"
        "2000", "2100", "2200" -> convertedTime = "2000"
        "2300", "0000", "0100" -> convertedTime = "2300"
    }
    return convertedTime.toIntOrNull()
}

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
        Test()
    }
}

@Composable
fun Test(modifier: Modifier = Modifier){
    val seoul = LatLng(37.566535, 126.97796919)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(seoul, 10f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = MarkerState(position = seoul),
            title = "Seoul",
            snippet = "Marker in Seoul"
        )
    }
}