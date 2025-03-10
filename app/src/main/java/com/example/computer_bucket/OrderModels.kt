package com.example.computer_bucket

data class OrderItems(
    val id: Int,
    val user_id: Int,
    val customer_name: String,
    val customer_phone: String,
    val customer_address: String,
    val order_status: String,
    val created_at: String,
    val product_name: String,
    val quantity: Int,
    val price: Double,
    val tracking_number: String,
    val payment_method: String,
    val total_price: Double, // This is the total price of the entire order
    val product_imgUrl: String
)

// Grouped order to display in the UI
data class GroupedOrder(
    val id: Int,
    val customer_name: String,
    val customer_phone: String,
    val customer_address: String,
    val order_status: String,
    val created_at: String,
    val tracking_number: String?,
    val payment_method: String,
    val total_price: Double,
    val items: List<OrderProduct>
)

// Product item within an order, using your specified structure
data class OrderProduct(
    val product_name: String,
    val quantity: Int,
    val price: Double,
    val product_imgUrl: String
)

// Function to process the raw API response and group it by orders
fun processOrderItems(orderItems: List<OrderItems>): List<GroupedOrder> {
    // Group items by order ID
    val groupedItems = orderItems.groupBy { it.id }

    // Convert to GroupedOrder objects
    return groupedItems.map { (orderId, items) ->
        // Take common order details from the first item
        val firstItem = items.first()
        GroupedOrder(
            id = orderId,
            customer_name = firstItem.customer_name,
            customer_phone = firstItem.customer_phone,
            customer_address = firstItem.customer_address,
            order_status = firstItem.order_status,
            created_at = firstItem.created_at,
            tracking_number = firstItem.tracking_number,
            payment_method = firstItem.payment_method,
            total_price = firstItem.total_price,
            items = items.map { item ->
                OrderProduct(
                    product_name = item.product_name,
                    quantity = item.quantity,
                    price = item.price,
                    product_imgUrl = item.product_imgUrl
                )
            }
        )
    }
}
