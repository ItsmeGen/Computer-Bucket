package com.example.computer_bucket

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CartActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var databaseHelper: DataBaseHelper
    private var userId: Int = -1  // Default user ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        databaseHelper = DataBaseHelper(this)

        // Get user ID from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("user_id", -1)

        recyclerView = findViewById(R.id.cartRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadCartItems()
    }

    private fun loadCartItems() {
        val cartItems = databaseHelper.getCartItems(userId)
        cartAdapter = CartAdapter(this, cartItems.toMutableList(), databaseHelper, userId) {
            loadCartItems() // Refresh the cart after removal
        }
        recyclerView.adapter = cartAdapter
    }
}
