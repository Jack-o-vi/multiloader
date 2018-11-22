package com.zeienko.servicesapp.data.net.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

interface UnsplashApi {

    //    @Multipart
    @GET("wikipedia/commons/f/ff/Pizigani_1367_Chart_10MB.jpg")
    fun getImage(): Call<ResponseBody>
}