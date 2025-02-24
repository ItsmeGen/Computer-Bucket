package com.example.computer_bucket

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditCustomerDetailsActivity : AppCompatActivity() {
    private lateinit var editFullName: EditText
    private lateinit var editPhone: EditText
    private lateinit var editAddress: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_customer_details)

        // Initialize Views
        editFullName = findViewById(R.id.editFullName)
        editPhone = findViewById(R.id.editPhone)
        editAddress = findViewById(R.id.editAddress)
        btnSave = findViewById(R.id.btnSave)

        // Get UserPrefs SharedPreferences and userId
        val sharedPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPrefs.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load saved details from UserPrefs for this specific user
        val savedFullName = sharedPrefs.getString("fullName_$userId", "")
        val savedPhone = sharedPrefs.getString("phone_$userId", "")
        val savedAddress = sharedPrefs.getString("address_$userId", "")

        // Set existing values
        editFullName.setText(savedFullName)
        editPhone.setText(savedPhone)
        editAddress.setText(savedAddress)

        // Save button action
        btnSave.setOnClickListener {
            val updatedFullName = editFullName.text.toString().trim()
            val updatedPhone = editPhone.text.toString().trim()
            val updatedAddress = editAddress.text.toString().trim()

            // Validate input
            if (updatedFullName.isEmpty() || updatedPhone.isEmpty() || updatedAddress.isEmpty()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save to UserPrefs with user-specific keys
            sharedPrefs.edit().apply {
                putString("fullName_$userId", updatedFullName)
                putString("phone_$userId", updatedPhone)
                putString("address_$userId", updatedAddress)
                apply()
            }

            // Send updated details back to CheckoutActivity
            val resultIntent = Intent().apply {
                putExtra("updatedFullName", updatedFullName)
                putExtra("updatedPhone", updatedPhone)
                putExtra("updatedAddress", updatedAddress)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}