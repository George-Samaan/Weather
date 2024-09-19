package com.example.iti.network

import com.example.iti.model.Weather
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiServices {
    @GET("weather")
    suspend fun getWeather(  //bt serve 3alia wa't m7tagha
        @Query("lat") lat: Double,
        @Query("lon") long: Double,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): retrofit2.Response<Weather>
}