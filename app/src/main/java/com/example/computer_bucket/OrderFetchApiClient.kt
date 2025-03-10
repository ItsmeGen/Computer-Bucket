package com.example.computer_bucket

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OrderFetchApiClient{
    private const val BASE_URL = "http://192.168.180.240/projectApi/orderFetch/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val OrderFetchApiService: OrdersFetchApiService by lazy {
        retrofit.create(OrdersFetchApiService::class.java)
    }
}
