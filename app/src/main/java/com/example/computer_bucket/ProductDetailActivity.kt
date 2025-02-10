package com.example.computer_bucket

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.computer_bucket.databinding.ActivityProductDetailBinding

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var dbHelper: DataBaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DataBaseHelper(this) // Initialize database helper

        intent.extras?.let { bundle ->
            binding.apply {
                val productId = bundle.getInt("product_id")
                val productName = bundle.getString("product_name") ?: ""
                val productDescription = bundle.getString("product_description") ?: ""
                val productPrice = bundle.getDouble("product_price")
                val productSold = bundle.getInt("product_sold")
                val productImgUrl = bundle.getString("product_imgUrl") ?: ""

                productNameText.text = productName
                productDescriptionText.text = productDescription
                productPriceText.text = "Price: $${productPrice}"

                // Load image using Glide
                Glide.with(this@ProductDetailActivity)
                    .load(productImgUrl)
                    .placeholder(R.drawable.loading_image)
                    .error(R.drawable.arrow_back)
                    .into(productImage)

                // Add to Cart button
                addToCartButton.setOnClickListener {
                    addToCart(productId, productName, productDescription, productPrice, productSold, productImgUrl)
                }
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun addToCart(productId: Int, name: String, description: String, price: Double, sold: Int, imgUrl: String) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, "Please log in first!", Toast.LENGTH_SHORT).show()
            return
        }

        val product = Product(productId, name, description, price, sold, imgUrl)

        val success = dbHelper.addToCart(userId, product, 1)
        if (success) {
            Toast.makeText(this, "Added to Cart!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to add to cart!", Toast.LENGTH_SHORT).show()
        }
    }
}
