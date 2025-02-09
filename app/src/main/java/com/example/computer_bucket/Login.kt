package com.example.computer_bucket

import android.content.Context
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
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val emailEditText: EditText = findViewById(R.id.email)
        val passwordEditText: EditText = findViewById(R.id.password)
        val btnLogin: Button = findViewById(R.id.btnLogin)
        val signUp : TextView = findViewById(R.id.sign_in_btn)

        signUp.setOnClickListener{
            val intent = Intent(this,SignUp::class.java)
            startActivity(intent)

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

                        // Save login state in SharedPreferences
                        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putBoolean("isLoggedIn", true)
                            putString("user_id", user.id)  // Save User ID
                            putString("username", user.username) // Save Username
                            putString("user_email", user.email) // Save Email
                            apply()
                        }

                        // Navigate to MainActivity
                        val intent = Intent(this@Login, MainActivity::class.java)
                        startActivity(intent)
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
