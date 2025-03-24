package com.example.computer_bucket

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AddToCartApiClient {
    private const val BASE_URL = "http://192.168.195.240/projectApi/addToCart/" // Replace with your server URL

    val apiService: AddToCartApiService by lazy {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY) // Or BASIC/HEADERS

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(AddToCartApiService::class.java)
    }
}