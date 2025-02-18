package com.example.computer_bucket

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        // Initialize views
        recyclerView = findViewById(R.id.checkoutRecyclerView)
        totalTextView = findViewById(R.id.totalPriceTextView)
        subtotalTextView = findViewById(R.id.subtotalTextView)
        shippingFeeTextView = findViewById(R.id.shippingFeeTextView)
        placeOrderButton = findViewById(R.id.placeOrderButton)

        // Get selected products from intent
        selectedProducts = intent.getParcelableArrayListExtra("SELECTED_PRODUCTS") ?: ArrayList()

        // Setup RecyclerView
        adapter = CheckoutItemAdapter(this, selectedProducts)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Calculate and display totals
        calculateAndDisplayTotals()

        // Set up button click listener
        placeOrderButton.setOnClickListener {
            // Implement your order placement logic here
            // e.g., save order to database, show confirmation, etc.
        }

        // Back button handling
        findViewById<View>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun calculateAndDisplayTotals() {
        val subtotal = selectedProducts.sumOf { it.product_price.toDouble() * it.quantity }
        val shippingFee = if (subtotal > 0) 150.0 else 0.0  // Example shipping fee
        val total = subtotal + shippingFee

        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "PH"))

        subtotalTextView.text = currencyFormatter.format(subtotal)
        shippingFeeTextView.text = currencyFormatter.format(shippingFee)
        totalTextView.text = currencyFormatter.format(total)
    }
}