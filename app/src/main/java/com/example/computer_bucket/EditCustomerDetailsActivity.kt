package com.example.computer_bucket

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class EditCustomerDetailsActivity : AppCompatActivity() {

    private lateinit var fullNameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_customer_details)

        // Initialize views
        fullNameEditText = findViewById(R.id.editFullName)
        phoneEditText = findViewById(R.id.editPhone)
        addressEditText = findViewById(R.id.editAddress)
        saveButton = findViewById(R.id.btnSave)

        // Get current values from intent
        val currentFullName = intent.getStringExtra("currentFullName") ?: ""
        val currentPhone = intent.getStringExtra("currentPhone") ?: ""
        val currentAddress = intent.getStringExtra("currentAddress") ?: ""

        // Pre-fill the EditText fields with current values
        // Only pre-fill if they're not the placeholder text
        if (currentFullName != "Enter Full Name") {
            fullNameEditText.setText(currentFullName)
        }

        if (currentPhone != "Enter Phone Number") {
            phoneEditText.setText(currentPhone)
        }

        if (currentAddress != "Enter Address") {
            addressEditText.setText(currentAddress)
        }

        // Set up save button click listener
        saveButton.setOnClickListener {
            val updatedFullName = fullNameEditText.text.toString().trim()
            val updatedPhone = phoneEditText.text.toString().trim()
            val updatedAddress = addressEditText.text.toString().trim()

            if (updatedFullName.isEmpty() || updatedPhone.isEmpty() || updatedAddress.isEmpty()) {

                return@setOnClickListener
            }

            // Return the updated values to the calling activity
            val resultIntent = Intent()
            resultIntent.putExtra("updatedFullName", updatedFullName)
            resultIntent.putExtra("updatedPhone", updatedPhone)
            resultIntent.putExtra("updatedAddress", updatedAddress)
            setResult(Activity.RESULT_OK, resultIntent)

            finish()
        }

        // Back button
        findViewById<View>(R.id.backButton)?.setOnClickListener {
            finish()
        }
    }
}