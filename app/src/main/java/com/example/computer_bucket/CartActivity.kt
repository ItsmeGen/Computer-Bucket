package com.example.computer_bucket

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CartActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var databaseHelper: DataBaseHelper
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
        databaseHelper = DataBaseHelper(this)

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
        // Ensure we get a non-null list
        val cartItems = databaseHelper.getCartItems(userId) ?: emptyList()

        cartAdapter = CartAdapter(this, cartItems.toMutableList(), databaseHelper, userId,
            onQuantityChanged = { itemId, newQuantity ->
                updateQuantityInDatabase(itemId, newQuantity)
            },
            onItemRemoved = {
                loadCartItems() // Reload cart after removal
            },
            onSelectionChanged = { total ->
                updateTotalPrice(total)
                checkoutButton.isEnabled = total > 0 // Enable checkout button when total is greater than 0
            }
        )
        recyclerView.adapter = cartAdapter
    }

    private fun updateQuantityInDatabase(itemId: Int, newQuantity: Int) {
        if (newQuantity >= 10) {
            Toast.makeText(this, "Maximum quantity is 10", Toast.LENGTH_SHORT).show()
            return
        }
        val isUpdated = databaseHelper.updateCartItemQuantity(itemId, userId, newQuantity)
        if (isUpdated) {
            loadCartItems() // Reload items after quantity update
        } else {
            Toast.makeText(this, "Failed to update quantity", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTotalPrice(total: Int) {
        totalPriceText.text = "Total: ₱$total"
    }

    private fun proceedToCheckout() {
        // Get the list of selected products from the adapter
        val selectedProducts = cartAdapter.getSelectedProducts()

        if (selectedProducts.isEmpty()) {
            Toast.makeText(this, "Please select at least one item", Toast.LENGTH_SHORT).show()
            return
        }

        // Proceed to the checkout activity with selected products
        val intent = Intent(this, CheckoutActivity::class.java)
        intent.putParcelableArrayListExtra("SELECTED_PRODUCTS", ArrayList(selectedProducts))
        startActivity(intent)
    }
}
