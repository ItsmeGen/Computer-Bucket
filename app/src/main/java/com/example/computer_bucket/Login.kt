package com.example.computer_bucket

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Login : AppCompatActivity() {
    private lateinit var progressBarLayout: LinearLayout
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var btnLogin: Button
    private lateinit var signUp: TextView
    private lateinit var loadingDialog: Dialog
    private lateinit var sharedPreferences: SharedPreferences
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        if (sharedPreferences.getBoolean("is_logged_in", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        loadingDialog = Dialog(this)
        loadingDialog.setContentView(R.layout.progress_loading)
        loadingDialog.setCancelable(false)
        loadingDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        btnLogin = findViewById(R.id.btnLogin)
        signUp = findViewById(R.id.sign_in_btn)
        progressBarLayout = findViewById(R.id.loadingLayout)

        passwordEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = passwordEditText.compoundDrawables[2]
                if (drawableEnd != null && event.rawX >= (passwordEditText.right - drawableEnd.bounds.width())) {
                    togglePasswordVisibility()
                    passwordEditText.performClick()
                    return@setOnTouchListener true
                }
            }
            false
        }

        signUp.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
            finish()
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

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.visibility_off, 0)
        } else {
            passwordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.visibility_on, 0)
        }
        isPasswordVisible = !isPasswordVisible
        passwordEditText.setSelection(passwordEditText.length())
    }

    private fun loginUser(email: String, password: String) {
        loadingDialog.show()
        btnLogin.isEnabled = false

        val apiService = UserApiClient.apiService
        val call = apiService.loginUser(LoginRequest(email, password))

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                loadingDialog.dismiss()
                btnLogin.isEnabled = true

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!

                    if (loginResponse.success) {
                        val user = loginResponse.user

                        // Check if user is blocked
                        if (user.status == "blocked") {
                            Toast.makeText(this@Login, "Your account is blocked. Contact support.", Toast.LENGTH_LONG).show()
                            return
                        }

                        val editor = sharedPreferences.edit()
                        editor.putInt("user_id", user.id.toInt())
                        editor.putString("username", user.username)
                        editor.putBoolean("is_logged_in", true)
                        editor.apply()

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
                loadingDialog.dismiss()
                btnLogin.isEnabled = true
                Toast.makeText(this@Login, "Login failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
