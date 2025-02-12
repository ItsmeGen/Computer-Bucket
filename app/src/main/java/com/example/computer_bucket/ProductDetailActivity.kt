package com.example.computer_bucket

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.computer_bucket.databinding.ActivityProductDetailBinding

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var dbHelper: DataBaseHelper
    private var hasShownToast = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DataBaseHelper(this)

        intent.extras?.let { bundle ->
            binding.apply {
                val productId = bundle.getInt("product_id")
                val productName = bundle.getString("product_name") ?: ""
                val productDescription = bundle.getString("product_description") ?: ""
                val productPrice = bundle.getDouble("product_price")
                val productSold = bundle.getInt("product_sold")
                val productImgUrl = bundle.getString("product_imgUrl") ?: ""


                val productNameText: TextView = findViewById(R.id.productNameText)
                val productPriceText: TextView = findViewById(R.id.productPriceText)
                val productDescriptionText: TextView = findViewById(R.id.productDescriptionText)

                productNameText.text = productName
                productDescriptionText.text = productDescription
                productPriceText.text = "Price: $${productPrice}"

                Glide.with(this@ProductDetailActivity)
                    .load(productImgUrl)
                    .placeholder(R.drawable.loading_image)
                    .error(R.drawable.arrow_back)
                    .into(productImage)


                addToCartButton.setOnClickListener {
                    showQuantityDialog(productId, productName, productDescription, productPrice, productSold, productImgUrl)
                }
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun showQuantityDialog(productId: Int, name: String, description: String, price: Double, sold: Int, imgUrl: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_quantity, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val buttonPlus = dialogView.findViewById<Button>(R.id.buttonPlus)
        val buttonMinus = dialogView.findViewById<Button>(R.id.buttonMinus)
        val textQuantity = dialogView.findViewById<TextView>(R.id.textQuantity)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)

        var quantity = 1
        textQuantity.text = quantity.toString()

        buttonPlus.setOnClickListener {
            if(quantity < 10) {
                quantity++
                textQuantity.text = quantity.toString()
            }else{
                Toast.makeText(this, "Maximum quantity is 10", Toast.LENGTH_SHORT).show()
                hasShownToast = true
            }
        }

        buttonMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                textQuantity.text = quantity.toString()
            }
        }

        confirmButton.setOnClickListener {
            addToCart(productId, name, description, price, sold, imgUrl, quantity)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addToCart(productId: Int, name: String, description: String, price: Double, sold: Int, imgUrl: String, quantity: Int) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId == -1) {
            val intent = Intent(this,Login::class.java)
            startActivity(intent)
            Toast.makeText(this, "Can't add to cart Please log in first!", Toast.LENGTH_SHORT).show()
            return
        }

        val product = Product(productId, name, description, price, sold, imgUrl,quantity)

        val success = dbHelper.addToCart(userId, product, quantity)
        if (success) {
            Toast.makeText(this, "Added $quantity to Cart!", Toast.LENGTH_SHORT).show()
            hasShownToast = true
        } else {
            Toast.makeText(this, "Failed to add to cart!", Toast.LENGTH_SHORT).show()
        }
    }
}
