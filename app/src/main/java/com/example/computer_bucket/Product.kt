package com.example.computer_bucket


data class Product(
    val product_id: Int,
    val product_name: String,
    val product_description: String,
    val product_price: Double,
    val product_sold : Int,
    val product_imgUrl: String,
    var quantity: Int,
    var isChecked: Boolean = false  // Default: unchecked

)
