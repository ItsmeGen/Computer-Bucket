package com.example.computer_bucket

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ReturnOrderApiClient {
    private const val BASE_URL = "http://192.168.195.240/projectApi/return_order/"

    val instance: ReturnOrderApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ReturnOrderApiService::class.java)
    }
}