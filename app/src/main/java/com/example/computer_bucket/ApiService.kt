package com.example.computer_bucket

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("productlist.php")
    fun productlist(): Call<List<Product>>
}