package com.example.computer_bucket

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OrdersFetchApiService {
    @GET("orderfetch.php")  // Adjust the endpoint if needed
    fun getOrders(@Query("user_id") userId: Int): Call<List<OrderItems>>
}
