package com.example.computer_bucket

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ReturnOrderApiService {
    @POST("return_order.php")
    fun returnOrder(@Body request: ReturnOrderRequest): Call<ApiResponse>
}