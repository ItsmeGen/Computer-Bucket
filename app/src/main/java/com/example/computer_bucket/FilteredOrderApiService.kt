package com.example.computer_bucket

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface FilteredOrderApiService {
    @GET("fetch_filtered_orders.php") // Replace with your PHP endpoint path
    fun getOrders(
        @Query("user_id") userId: Int,
        @Query("order_status") orderStatus: String
    ): Call<List<OrderItems>>
}