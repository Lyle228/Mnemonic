package com.example.mnemonic

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mnemonic.chatgpt.viewmodel.ChatGPTViewModel
import com.example.mnemonic.ui.theme.MnemonicTheme
import com.example.mnemonic.weather.api.RetrofitInstance
import com.example.mnemonic.weather.model.PrecipitationType
import com.example.mnemonic.weather.model.WeatherType
import com.example.mnemonic.weather.repository.WeatherRepository
import com.example.mnemonic.weather.viewmodel.WeatherViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    val nowHour: String = LocalDateTime.now().format(dateFormatter).toString()

    /* 주소 정보 */
    val locationName by viewModel.locationName.observeAsState("unknown")
    val locationNameDetail by viewModel.locationNameDetail.observeAsState("unknown")

    /* 최고, 최저 기온 */
    val maxTemperature = weatherFormattedDataPerDayToday?.maximumTemperature ?: 0.0
    val minTemperature = weatherFormattedDataPerDayToday?.minimumTemperature ?: 0.0

    /* 날씨 정보 */
    val precipitationType =
        weatherFormattedDataPerDayToday?.weatherFormattedDataPerHourMap?.getOrDefault(nowHour, null)?.precipitationType
    val weatherType =
        weatherFormattedDataPerDayToday?.weatherFormattedDataPerHourMap?.getOrDefault(nowHour, null)?.weatherType
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
    val nowDate: String = LocalDate.now().plusDays(dayLater.toLong()).format(dateFormatter)
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
@Preview
@Composable
fun CautionMessagePreview() {
    MnemonicTheme {
        val context = LocalContext.current
        //autionMessage(messageID = R.string.heatwave_msg, context = context, params = listOf("20") )
    }
}
@Composable
fun CautionMessage(modifier: Modifier = Modifier, viewModel: ChatGPTViewModel) {
    val cautionMessageText = viewModel.adviceMessage.observeAsState().value ?: "invalid"
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 30.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(10.dp)
                )
        ) {
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                modifier = modifier
                    .align(Alignment.CenterHorizontally),
                text = cautionMessageText,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}