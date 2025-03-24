package com.example.computer_bucket

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private var userId: Int = -1 // Default user ID
    private lateinit var totalPriceText: TextView
    private lateinit var checkoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        val arrowBack: ImageView = findViewById(R.id.arrowBack)
        arrowBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        totalPriceText = findViewById(R.id.totalPriceTextView)

        // Get user ID from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("user_id", -1)

        recyclerView = findViewById(R.id.cartRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        checkoutButton = findViewById(R.id.checkoutButton)
        checkoutButton.isEnabled = false // Disable checkout by default

        checkoutButton.setOnClickListener {
            proceedToCheckout()
        }

        loadCartItems()
    }

    private fun loadCartItems() {
        val apiService = AddToCartApiClient.apiService
        val call = apiService.getCartItems(userId, "getCartItems") // Added action parameter

        call.enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful) {
                    val cartItems = response.body() ?: emptyList()
                    cartAdapter = CartAdapter(
                        this@CartActivity,
                        cartItems.toMutableList(),
                        userId,
                        onQuantityChanged = { itemId, newQuantity ->
                            updateQuantityInDatabase(itemId, newQuantity)
                        },
                        onItemRemoved = {
                            loadCartItems()
                        },
                        onSelectionChanged = { total ->
                            updateTotalPrice(total)
                            checkoutButton.isEnabled = total > 0
                        }
                    )
                    recyclerView.adapter = cartAdapter
                } else {
                    Toast.makeText(this@CartActivity, "Failed to load cart items", Toast.LENGTH_SHORT).show()
                    Log.e("CartActivity", "Error loading cart: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                Toast.makeText(this@CartActivity, "Network error loading cart", Toast.LENGTH_SHORT).show()
                Log.e("CartActivity", "Network error: ${t.message}")
            }
        })
    }

    private fun updateQuantityInDatabase(itemId: Int, newQuantity: Int) {
        if (newQuantity >= 10) {
            Toast.makeText(this, "Maximum quantity is 10", Toast.LENGTH_SHORT).show()
            return
        }

        val apiService = AddToCartApiClient.apiService
        val call = apiService.updateCartItemQuantity(itemId, userId, newQuantity, "updateCartItemQuantity") // Added action parameter

        call.enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                if (response.isSuccessful) {
                    val isUpdated = response.body() ?: false
                    if (isUpdated) {
                        loadCartItems()
                    } else {
                        Toast.makeText(this@CartActivity, "Failed to update quantity", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@CartActivity, "Failed to update quantity", Toast.LENGTH_SHORT).show()
                    Log.e("CartActivity", "Error updating quantity: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                Toast.makeText(this@CartActivity, "Network error updating quantity", Toast.LENGTH_SHORT).show()
                Log.e("CartActivity", "Network error: ${t.message}")
            }
        })
    }

    private fun updateTotalPrice(total: Int) {
        totalPriceText.text = "Total: ₱$total"
    }

    private fun proceedToCheckout() {
        val selectedProducts = cartAdapter.getSelectedProducts()

        if (selectedProducts.isEmpty()) {
            Toast.makeText(this, "Please select at least one item", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, CheckoutActivity::class.java)
        intent.putParcelableArrayListExtra("SELECTED_PRODUCTS", ArrayList(selectedProducts))
        startActivity(intent)
    }
}