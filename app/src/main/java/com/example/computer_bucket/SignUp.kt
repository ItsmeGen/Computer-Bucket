package com.example.computer_bucket

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
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
    private lateinit var loadingDialog: Dialog
    private lateinit var btnSignUp: Button
    private lateinit var etPassword: EditText
    private var isPasswordVisible = false
    private var hasShownToast = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        // Initialize Dialog
        loadingDialog = Dialog(this)
        loadingDialog.setContentView(R.layout.progress_loading)
        loadingDialog.setCancelable(false)
        loadingDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Buttons and EditTexts
        btnSignUp = findViewById(R.id.btnSignIn)
        etPassword = findViewById(R.id.create_password)
        val btnLogin: TextView = findViewById(R.id.login)
        val etUsername: EditText = findViewById(R.id.username)
        val etEmail: EditText = findViewById(R.id.create_email)

        etPassword.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = etPassword.compoundDrawables[2] // Right drawable
                if (drawableEnd != null && event.rawX >= (etPassword.right - drawableEnd.bounds.width())) {
                    togglePasswordVisibility()
                    return@setOnTouchListener true
                }
            }
            false
        }

        // Register Button
        btnSignUp.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
                hasShownToast = true
                return@setOnClickListener
            }

            registerUser(username, email, password)
        }

        btnLogin.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }
    }
    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.visibility_off, 0)
        } else {
            etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.visibility_on, 0)
        }
        isPasswordVisible = !isPasswordVisible
        etPassword.setSelection(etPassword.length()) // Keep cursor position at the end
    }


    // Call API
    private fun registerUser(username: String, email: String, password: String) {
        val user = RegisterRequest(username, email, password)
        loadingDialog.show()
        btnSignUp.isEnabled = false

        UserApiClient.apiService.registerUser(user).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                loadingDialog.dismiss()
                btnSignUp.isEnabled = true

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
                loadingDialog.dismiss()
                btnSignUp.isEnabled = true
                Toast.makeText(this@SignUp, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
