package com.example.computer_bucket

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NotificationApiClient{
    private const val BASE_URL = "http://192.168.195.240/projectApi/get_notification/" // Change if using a real server

    val instance: NotificationApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NotificationApiService::class.java)
    }
}
