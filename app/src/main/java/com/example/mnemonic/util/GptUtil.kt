package com.example.mnemonic.util

import android.content.ContentValues
import android.util.Log
import com.example.mnemonic.weather.model.PrecipitationType
import com.example.mnemonic.weather.model.WeatherFormattedDataPerDay
import com.example.mnemonic.weather.model.WeatherType

class GptUtil {
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
        Log.d(ContentValues.TAG, "convertToChatGPTQuestions: $question")
        return question
    }


}