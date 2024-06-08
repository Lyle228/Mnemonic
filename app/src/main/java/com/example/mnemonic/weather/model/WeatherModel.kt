package com.example.mnemonic.weather.model


data class Weather(
    val response: Response
){
    data class Response(
        val header: Header,
        val body: Body
    )

    data class Header(
        val resultCode:Int,
        val resultMsg: String
    )

    data class Body(
        val dataType: String,
        val items: Items
    )

    data class Items(
        val item: List<Item>
    )

    data class Item(
        val baseDate: String,
        val baseTime: String,
        val category: String,
        val fcstDate: String,
        val fcstTime: String,
        val fcstValue: String,
        val nx : Int,
        val ny : Int
    )
}