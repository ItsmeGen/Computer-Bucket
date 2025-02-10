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

class SignUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        val btnSignUp: Button = findViewById(R.id.btnSignIn)
        val btnLogin: TextView = findViewById(R.id.login)
        val etUsername: EditText = findViewById(R.id.username)
        val etEmail: EditText = findViewById(R.id.create_email)
        val etPassword: EditText = findViewById(R.id.create_password)

        // RegisterBtn
        btnSignUp.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(username, email, password)
        }

        // Redirect to Login Page
        btnLogin.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }

    // Call API
    private fun registerUser(username: String, email: String, password: String) {
        val user = RegisterRequest(username, email, password)

        UserApiClient.apiService.registerUser(user).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result?.success == true) {
                        Toast.makeText(this@SignUp, "Registration Successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@SignUp, Login::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@SignUp, "Failed: ${result?.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SignUp, "Server Error!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Toast.makeText(this@SignUp, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
