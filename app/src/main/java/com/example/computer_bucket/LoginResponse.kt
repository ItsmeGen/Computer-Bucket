package com.example.computer_bucket

data class LoginResponse(
    val success : Boolean,
    val message : String,
    val user : User
)
data class User(
    val id: String,
    val username: String,
    val email: String,
    val status: String
)
