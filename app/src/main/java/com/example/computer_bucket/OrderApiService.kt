package com.example.computer_bucket


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor


interface OrderApiService {
    @POST("place_order.php")
    fun placeOrder(@Body orderRequest: OrderRequest): Call<OrderResponse>
}

object OrderApiClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.170.240/projectApi/orderApi/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: OrderApiService = retrofit.create(OrderApiService::class.java)
}
