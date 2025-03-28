package com.example.computer_bucket

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Callback
import java.text.NumberFormat
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CheckoutItemAdapter
    private lateinit var totalTextView: TextView
    private lateinit var subtotalTextView: TextView
    private lateinit var shippingFeeTextView: TextView
    private lateinit var placeOrderButton: Button
    private lateinit var selectedProducts: ArrayList<Product>
    private lateinit var codRadioButton: RadioButton
    private lateinit var otherPaymentRadioButton: RadioButton

    private val EDIT_REQUEST_CODE = 1
    private lateinit var customerFullName: TextView
    private lateinit var customerPhone: TextView
    private lateinit var customerAddress: TextView
    private lateinit var editCustomerDetails: TextView

    // Shared preferences name for customer details
    private val CUSTOMER_PREFS_NAME = "CustomerDetailsPrefs"

    // Keys for customer details
    private val KEY_FULLNAME = "fullName"
    private val KEY_PHONE = "phone"
    private val KEY_ADDRESS = "address"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        // Initialize views
        recyclerView = findViewById(R.id.checkoutRecyclerView)
        totalTextView = findViewById(R.id.totalPriceTextView)
        subtotalTextView = findViewById(R.id.subtotalTextView)
        shippingFeeTextView = findViewById(R.id.shippingFeeTextView)
        placeOrderButton = findViewById(R.id.placeOrderButton)

        customerFullName = findViewById(R.id.customerFullName)
        customerPhone = findViewById(R.id.customerPhone)
        customerAddress = findViewById(R.id.customerAddress)
        editCustomerDetails = findViewById(R.id.editCustomerDetails)

        codRadioButton = findViewById(R.id.codRadioButton)
        otherPaymentRadioButton = findViewById(R.id.otherPaymentRadioButton)

        // Load saved customer details
        loadCustomerDetails()

        editCustomerDetails.setOnClickListener {
            val intent = Intent(this, EditCustomerDetailsActivity::class.java)

            val name = customerFullName.text.toString()
            val phone = customerPhone.text.toString()
            val address = customerAddress.text.toString()

            intent.putExtra("currentFullName", name)
            intent.putExtra("currentPhone", phone)
            intent.putExtra("currentAddress", address)

            startActivityForResult(intent, EDIT_REQUEST_CODE)
        }

        // Get selected products from intent
        selectedProducts = intent.getParcelableArrayListExtra("SELECTED_PRODUCTS") ?: ArrayList()

        // Check if selectedProducts is empty
        if (selectedProducts.isEmpty()) {
            Toast.makeText(this, "No products in checkout!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Setup RecyclerView
        adapter = CheckoutItemAdapter(this, selectedProducts)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Calculate and display totals
        calculateAndDisplayTotals()

        // Set up button click listener
        placeOrderButton.setOnClickListener {
            val userPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val userId = userPrefs.getInt("user_id", -1)

            if (userId == -1) {
                Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate customer details BEFORE proceeding
            if (!validateCustomerDetails()) {
                return@setOnClickListener // Stop execution if validation fails
            }

            val paymentMethod = if (codRadioButton.isChecked) "Cash on Delivery (COD)" else "Other Payment Method"

            val orderItems = selectedProducts.map {
                OrderItem(
                    product_id = it.product_id,
                    product_name = it.product_name,
                    quantity = it.quantity,
                    price = it.product_price.toDouble()
                )
            }

            val orderRequest = OrderRequest(
                user_id = userId,
                total_price = totalTextView.text.toString().replace("₱", "").replace(",","").toDouble(),
                payment_method = paymentMethod,
                customer_name = customerFullName.text.toString().trim(),
                customer_phone = customerPhone.text.toString().trim(),
                customer_address = customerAddress.text.toString().trim(),
                order_items = orderItems
                // order_status is not explicitly set, so it will default to "Processing"
            )

            // Call API
            val call = OrderApiClient.apiService.placeOrder(orderRequest)
            call.enqueue(object : Callback<OrderResponse> {
                override fun onResponse(call: retrofit2.Call<OrderResponse>, response: retrofit2.Response<OrderResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@CheckoutActivity, "Order Placed Successfully!", Toast.LENGTH_SHORT).show()
                        finish() // Close activity after successful order
                    } else {
                        Toast.makeText(this@CheckoutActivity, "Order Failed!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<OrderResponse>, t: Throwable) {
                    Toast.makeText(this@CheckoutActivity, "Network Error!", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Back button handling
        findViewById<View>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDIT_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val updatedFullName = data.getStringExtra("updatedFullName")
            val updatedPhone = data.getStringExtra("updatedPhone")
            val updatedAddress = data.getStringExtra("updatedAddress")

            // Update UI
            updatedFullName?.let { customerFullName.text = it }
            updatedPhone?.let { customerPhone.text = it }
            updatedAddress?.let { customerAddress.text = it }

            // Save to user-specific preferences
            if (updatedFullName != null && updatedPhone != null && updatedAddress != null) {
                saveCustomerDetails(updatedFullName, updatedPhone, updatedAddress)
            }
        }
    }

    // Function to load saved customer details
    private fun loadCustomerDetails() {
        val userPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = userPrefs.getInt("user_id", -1)

        if (userId != -1) {
            // Get the customer details for this specific user
            val customerPrefs = getSharedPreferences(CUSTOMER_PREFS_NAME, Context.MODE_PRIVATE)

            customerFullName.text = customerPrefs.getString("${KEY_FULLNAME}_$userId", "Enter Full Name")
            customerPhone.text = customerPrefs.getString("${KEY_PHONE}_$userId", "Enter Phone Number")
            customerAddress.text = customerPrefs.getString("${KEY_ADDRESS}_$userId", "Enter Address")
        } else {
            // Default values if no user is logged in
            customerFullName.text = "Enter Full Name"
            customerPhone.text = "Enter Phone Number"
            customerAddress.text = "Enter Address"
        }
    }

    // Function to save customer details
    private fun saveCustomerDetails(fullName: String, phone: String, address: String) {
        val userPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = userPrefs.getInt("user_id", -1)

        if (userId != -1) {
            // Save the customer details for this specific user
            val customerPrefs = getSharedPreferences(CUSTOMER_PREFS_NAME, Context.MODE_PRIVATE)
            val editor = customerPrefs.edit()

            editor.putString("${KEY_FULLNAME}_$userId", fullName)
            editor.putString("${KEY_PHONE}_$userId", phone)
            editor.putString("${KEY_ADDRESS}_$userId", address)
            editor.apply()
        }
    }

    private fun calculateAndDisplayTotals() {
        val subtotal = selectedProducts.sumOf { it.product_price.toDouble() * it.quantity }
        val shippingFee = if (subtotal > 0) 50.0 else 0.0 // Example shipping fee
        val total = subtotal + shippingFee

        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "PH"))

        subtotalTextView.text = currencyFormatter.format(subtotal).replace(",", "")
        shippingFeeTextView.text = currencyFormatter.format(shippingFee).replace(",", "")
        totalTextView.text = currencyFormatter.format(total).replace(",", "")
    }

    // Function to validate customer details
    private fun validateCustomerDetails(): Boolean {
        val fullName = customerFullName.text.toString().trim()
        val phone = customerPhone.text.toString().trim()
        val address = customerAddress.text.toString().trim()

        if (fullName.isEmpty() || phone.isEmpty() || address.isEmpty() ||
            fullName == "Enter Full Name" || phone == "Enter Phone Number" || address == "Enter Address") {
            Toast.makeText(this, "Please complete your customer details before placing the order!", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}