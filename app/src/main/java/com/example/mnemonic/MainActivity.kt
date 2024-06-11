package com.example.mnemonic

import DateUtil.getCurrentDateFormatted
import DateUtil.getCurrentTimeFormatted
import android.content.ContentValues.TAG
import android.location.Location
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.lifecycle.ViewModelProvider
import com.example.mnemonic.util.GpsUtil.getCurrentUserGpsData
import com.example.mnemonic.weather.api.RetrofitInstance
import com.example.mnemonic.weather.repository.WeatherRepository
import com.example.mnemonic.weather.viewmodel.WeatherViewModel
import com.example.mnemonic.weather.viewmodel.WeatherViewModelFactory
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mnemonic.ui.theme.MnemonicTheme
import com.example.mnemonic.weather.model.PrecipitationType
import com.example.mnemonic.weather.model.WeatherApiRequest
import com.example.mnemonic.weather.model.WeatherFormattedDataPerDay
import com.example.mnemonic.weather.model.WeatherFormattedDataPerHour
import com.example.mnemonic.weather.model.WeatherType
import com.example.mnemonic.weather.util.WeatherCoordinateConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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

                /* 지역 이름 정보 요청 */
                viewModel.getLocationName(this, location.latitude, location.longitude)
            }
        }

        setContent {
            MnemonicTheme {
                Column {
                    TodayWeatherInfomation(modifier = Modifier, viewModel)
                    Row(Modifier.fillMaxWidth().height(60.dp)) {
                        ForecastWeatherInformation(viewModel = viewModel, dayLater = 1, modifier = Modifier.weight(1f))
                        ForecastWeatherInformation(viewModel = viewModel, dayLater = 2, modifier = Modifier.weight(1f))
                    }
                }
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
    val converter = WeatherCoordinateConverter()
    val convertedLocation = converter.convertToXy(location.latitude, location.longitude)
    return WeatherApiRequest(baseDate = convertedDate, nx = convertedLocation.nx.toString(), ny = convertedLocation.ny.toString())
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

@Preview(showBackground = true)
@Composable
fun TodayWeatherInfomationPreview() {
    MnemonicTheme {
        val repository = WeatherRepository(RetrofitInstance.api)
        val viewModel = WeatherViewModel(repository)
        TodayWeatherInfomation(viewModel = viewModel)
    }
}

@Composable
fun TodayWeatherInfomation(modifier: Modifier = Modifier, viewModel: WeatherViewModel){
    val weatherFormattedDataPerDayList by viewModel.weatherFormattedDataPerDayList.observeAsState()
    val weatherFormattedDataPerDayToday = weatherFormattedDataPerDayList?.getOrNull(0)
    val dateFormatter = DateTimeFormatter.ofPattern("HH00", Locale.US)
    val nowHour: Int? = LocalDateTime.now().format(dateFormatter).toIntOrNull()

    /* 주소 정보 */
    val locationName by viewModel.locationName.observeAsState("unknown")
    val locationNameDetail by viewModel.locationNameDetail.observeAsState("unknown")

    /* 최고, 최저 기온 */
    val maxTemperature = weatherFormattedDataPerDayToday?.maximumTemperature ?: 0.0
    val minTemperature = weatherFormattedDataPerDayToday?.minimumTemperature ?: 0.0

    /* 날씨 정보 */
    val precipitationType =
        weatherFormattedDataPerDayToday?.weatherFormattedDataPerHourMap?.getValue(nowHour.toString())?.precipitationType
    val weatherType =
        weatherFormattedDataPerDayToday?.weatherFormattedDataPerHourMap?.getValue(nowHour.toString())?.weatherType
    var weatherNowText = ""
    var weatherIconId = R.drawable.icon_weather_sunny
    when (precipitationType) {
        PrecipitationType.None     -> {
            when(weatherType) {
                WeatherType.Sunny -> {
                    weatherNowText = "맑음"
                    weatherIconId = R.drawable.icon_weather_sunny
                }
                WeatherType.cloudy -> {
                    weatherNowText = "구름 많음"
                    weatherIconId = R.drawable.icon_weather_cloudy
                }
                WeatherType.cloudyWeather -> {
                    weatherNowText = "흐림"
                    weatherIconId = R.drawable.icon_weather_cloud_weather
                }
                else-> weatherNowText = "오류"
            }
        }
        PrecipitationType.Rain     -> {
            weatherNowText = "비"
            weatherIconId = R.drawable.icon_weather_rain
        }
        PrecipitationType.SnowRain -> {
            weatherNowText = "눈비"
            weatherIconId = R.drawable.icon_weather_snowrain
        }
        PrecipitationType.Snow     -> {
            weatherNowText = "눈"
            weatherIconId = R.drawable.icon_weather_snow
        }
        PrecipitationType.Shower   -> {
            weatherNowText = "소나기"
            weatherIconId = R.drawable.icon_weather_shower
        }
        else -> weatherNowText = "오류"
    }

    /* 현재 기온 */
    val temperatureNowText = weatherFormattedDataPerDayToday?.weatherFormattedDataPerHourMap?.getValue(nowHour.toString())?.temperature.toString()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = locationName,
            fontSize = 17.sp,
            color = Color.Black
        )
        Text(
            text = locationNameDetail,
            fontSize = 13.sp,
            color = Color.Gray
        )
        Image(
            modifier = Modifier
                .width(120.dp)
                .height(170.dp)
                .padding(vertical = 30.dp),
            painter = painterResource(id = weatherIconId),
            contentDescription = "sunny"
        )
        Text(
            text = "$temperatureNowText°C",
            fontSize = 30.sp
        )
        Text(
            text = weatherNowText,
            fontSize = 10.sp
        )
        Text(
            text = "최고:${maxTemperature}°C 최저: ${minTemperature}°C",
            fontSize = 10.sp
        )
    }
}
@Preview(showBackground = true)
@Composable
fun ForecastWeatherInformationPreview() {
    MnemonicTheme {
        val repository = WeatherRepository(RetrofitInstance.api)
        val viewModel = WeatherViewModel(repository)
        Row(
            Modifier
                .fillMaxWidth()
                .height(40.dp)) {
            ForecastWeatherInformation(viewModel = viewModel, dayLater = 0, modifier = Modifier.weight(1f))
            ForecastWeatherInformation(viewModel = viewModel, dayLater = 0, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ForecastWeatherInformation(modifier: Modifier = Modifier, viewModel: WeatherViewModel, dayLater: Int) {
    val weatherFormattedDataPerDayList by viewModel.weatherFormattedDataPerDayList.observeAsState()
    val weatherFormattedDataPerDayToday = weatherFormattedDataPerDayList?.getOrNull(dayLater)
    val dateFormatter = DateTimeFormatter.ofPattern("MM/dd", Locale.US)
    val nowDate: String = LocalDate.now().format(dateFormatter).toString()
    /* 최고, 최저 기온 */
    val maxTemperature = weatherFormattedDataPerDayToday?.maximumTemperature ?: 0.0
    val minTemperature = weatherFormattedDataPerDayToday?.minimumTemperature ?: 0.0

    /* 기상 정보*/
    var precipitationType = PrecipitationType.None
    var weatherType = WeatherType.Sunny
    val weatherFormattedDataPerHourMap =
        weatherFormattedDataPerDayToday?.weatherFormattedDataPerHourMap

    /* TODO : 합리적인 우선 순위로 변경하기 */
    if (!weatherFormattedDataPerHourMap.isNullOrEmpty()) {
        for (dataPerHour in weatherFormattedDataPerHourMap) {
            val weatherFormattedDataPerHour = dataPerHour.value
            val tempPrecipitationType = weatherFormattedDataPerHour.precipitationType
            val tempWeatherType = weatherFormattedDataPerHour.weatherType
            if (precipitationType == PrecipitationType.None && tempPrecipitationType != null && tempPrecipitationType != PrecipitationType.None) {
                precipitationType = tempPrecipitationType
            }
            if (weatherType == WeatherType.Sunny && tempWeatherType != null && tempWeatherType != WeatherType.Sunny) {
                weatherType = tempWeatherType
            }
            if (weatherType != WeatherType.Sunny && precipitationType != PrecipitationType.None) {
                break
            }
        }
    }

    var weatherIconId = R.drawable.icon_weather_sunny
    when (precipitationType) {
        PrecipitationType.None -> {
            when (weatherType) {
                WeatherType.Sunny -> {
                    weatherIconId = R.drawable.icon_weather_sunny
                }

                WeatherType.cloudy -> {
                    weatherIconId = R.drawable.icon_weather_cloudy
                }

                WeatherType.cloudyWeather -> {
                    weatherIconId = R.drawable.icon_weather_cloud_weather
                }

                else -> weatherIconId = R.drawable.icon_weather_sunny
            }
        }

        PrecipitationType.Rain -> {
            weatherIconId = R.drawable.icon_weather_rain
        }

        PrecipitationType.SnowRain -> {
            weatherIconId = R.drawable.icon_weather_snowrain
        }

        PrecipitationType.Snow -> {
            weatherIconId = R.drawable.icon_weather_snow
        }

        PrecipitationType.Shower -> {
            weatherIconId = R.drawable.icon_weather_shower
        }

        else -> weatherIconId = R.drawable.icon_weather_sunny
    }

    Row(
        modifier = Modifier.padding(10.dp),
    ) {
        Column (
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center){
            Text(
                modifier = Modifier,
                text = nowDate,
                fontSize = 10.sp,
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column (
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center) {
            Image(
                modifier = Modifier
                    .width(30.dp)
                    .height(20.dp)
                    .padding(end = 10.dp),
                painter = painterResource(id = weatherIconId),
                contentDescription = "sunny"
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column (
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center){
        Text(
            modifier = Modifier
                .padding(end = 10.dp),
            text = "${maxTemperature}°C / ${minTemperature}°C",
            fontSize = 14.sp,
        )
        }
    }
}