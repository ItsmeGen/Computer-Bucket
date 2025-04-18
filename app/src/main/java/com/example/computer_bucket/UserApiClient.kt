package com.example.computer_bucket

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object UserApiClient {
    private const val BASE_URL = "http://192.168.195.240/projectApi/userApi/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }
}
