package com.example.computer_bucket

data class NotificationItem(
    val id: Int,
    val user_id: Int,
    val order_id: Int,
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false // Default to unread when notifications are first fetched
)
