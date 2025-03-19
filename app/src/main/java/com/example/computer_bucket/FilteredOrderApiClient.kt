package com.example.computer_bucket


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object FilteredOrderApiClient {

    private const val BASE_URL = "http://192.168.170.240/projectApi/filter_orders/"

    val api: FilteredOrderApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(FilteredOrderApiService::class.java)
    }
}