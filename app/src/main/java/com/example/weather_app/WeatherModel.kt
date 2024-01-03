package com.example.weather_app

import java.util.Random

data class WeatherModel(
    var id: Int = getAutoId(),
    var city: String = "",
    var country: String = "",
    var currTemp: String = ""
){


    companion object{

        fun getAutoId(): Int{
            val random = Random()
            return random.nextInt(100)
        }
    }
}
