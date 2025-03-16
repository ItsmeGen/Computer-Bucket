package com.example.computer_bucket

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NotificationApiService {
    @GET("get_notifications.php")
    fun getNotifications(@Query("user_id") userId: Int): Call<List<NotificationItem>>
}
