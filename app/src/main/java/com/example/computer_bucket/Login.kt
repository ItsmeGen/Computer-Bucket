package com.example.computer_bucket

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is already logged in
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId != -1) { // User is already logged in
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val emailEditText: EditText = findViewById(R.id.email)
        val passwordEditText: EditText = findViewById(R.id.password)
        val btnLogin: Button = findViewById(R.id.btnLogin)
        val signUp: TextView = findViewById(R.id.sign_in_btn)

        signUp.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }

        btnLogin.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please enter email and password!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        val apiService = UserApiClient.apiService
        val call = apiService.loginUser(LoginRequest(email, password))

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    if (loginResponse.success) {
                        val user = loginResponse.user

                        // Save user details in SharedPreferences
                        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putInt("user_id", user.id.toInt()) // Save user ID
                        editor.putString("username", user.username) // Save username
                        editor.apply()

                        // Navigate to MainActivity
                        startActivity(Intent(this@Login, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@Login, loginResponse.message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@Login, "Invalid response from server", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@Login, "Login failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
