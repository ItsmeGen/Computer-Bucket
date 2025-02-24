package com.example.computer_bucket

data class OrderRequest(
    val user_id: Int,
    val total_price: Double,
    val payment_method: String,
    val customer_name: String,
    val customer_phone: String,
    val customer_address: String,
    val order_items: List<OrderItem>
)

data class OrderItem(
    val product_id: Int,
    val product_name: String,
    val quantity: Int,
    val price: Double
)

data class OrderResponse(
    val success: Boolean,
    val message: String,
    val order_id: Int?
)

