package com.example.computer_bucket

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.computer_bucket.databinding.ActivityProductDetailBinding

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        intent.extras?.let { bundle ->
            binding.apply {
                productName.text = bundle.getString("product_name")
                productDescription.text = bundle.getString("product_description")
                productPrice.text = "Price: $${bundle.getDouble("product_price")}"

                // Load image using Glide
                Glide.with(this@ProductDetailActivity)
                    .load(bundle.getString("product_imgUrl"))
                    .placeholder(R.drawable.loading_image)
                    .error(R.drawable.arrow_back)
                    .into(productImage)
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }
}