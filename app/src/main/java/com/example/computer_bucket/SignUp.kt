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
import android.widget.ImageView
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
    private lateinit var etConfirmPassword: EditText
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

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
        etConfirmPassword = findViewById(R.id.confirm_password)
        val arrowBack: ImageView = findViewById(R.id.arrowBack)
        val etUsername: EditText = findViewById(R.id.username)
        val etEmail: EditText = findViewById(R.id.create_email)

        etPassword.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = etPassword.compoundDrawables[2]
                if (drawableEnd != null && event.rawX >= (etPassword.right - drawableEnd.bounds.width())) {
                    togglePasswordVisibility(etPassword, true)
                    return@setOnTouchListener true
                }
            }
            false
        }

        etConfirmPassword.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = etConfirmPassword.compoundDrawables[2]
                if (drawableEnd != null && event.rawX >= (etConfirmPassword.right - drawableEnd.bounds.width())) {
                    togglePasswordVisibility(etConfirmPassword, false)
                    return@setOnTouchListener true
                }
            }
            false
        }

        // Register Button Click Listener
        btnSignUp.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Split the email to check the local part separately
            val emailParts = email.split("@")
            if (emailParts.size != 2) {
                Toast.makeText(this, "Invalid email format!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val localPart = emailParts[0]

            // Check if local part has at least 8 alphabetic characters
            val alphabetCount = localPart.count { it.isLetter() }
            if (alphabetCount < 8) {
                Toast.makeText(this, "Email username must have at least 8 alphabetic characters!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if local part has no more than 2 numeric characters
            val digitCount = localPart.count { it.isDigit() }
            if (digitCount > 2) {
                Toast.makeText(this, "Email username cannot have more than 2 digits!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check general email format
            val generalEmailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$".toRegex()
            if (!email.matches(generalEmailRegex)) {
                Toast.makeText(this, "Invalid email format!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Only letters & numbers 15-20 characters, no spaces or special characters
            val passwordRegex = "^[a-zA-Z0-9]{15,20}$".toRegex()

            if (!password.matches(passwordRegex)) {
                Toast.makeText(this, "Password must be 15-20 characters long and contain only letters and numbers!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(username, email, password)
        }

        arrowBack.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }

    private fun togglePasswordVisibility(editText: EditText, isMainPassword: Boolean) {
        if (isMainPassword) {
            if (isPasswordVisible) {
                editText.transformationMethod = PasswordTransformationMethod.getInstance()
                editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.visibility_off, 0)
            } else {
                editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.visibility_on, 0)
            }
            isPasswordVisible = !isPasswordVisible
        } else {
            if (isConfirmPasswordVisible) {
                editText.transformationMethod = PasswordTransformationMethod.getInstance()
                editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.visibility_off, 0)
            } else {
                editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.visibility_on, 0)
            }
            isConfirmPasswordVisible = !isConfirmPasswordVisible
        }
        editText.setSelection(editText.length()) // Keep cursor at the end
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