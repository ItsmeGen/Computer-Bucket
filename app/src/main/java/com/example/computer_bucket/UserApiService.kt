package com.example.computer_bucket

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserApiService {

    @POST("register.php")
    fun registerUser(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("login.php")
    fun loginUser(@Body loginRequest: LoginRequest): Call<LoginResponse>
}
