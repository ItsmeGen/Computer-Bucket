package com.example.computer_bucket

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface CancelOrderApiService {
    @POST("cancel_order.php")
    fun updateOrderStatus(@Body requestBody: Map<String, Int>): Call<ApiResponse>
}
data class ApiResponse(val success: Boolean?, val error: String?)