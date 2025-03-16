package com.example.computer_bucket

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class NotificationAdapter(
    private var notifications: MutableList<NotificationItem>,
    private val onNotificationClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val notificationTitle: TextView = itemView.findViewById(R.id.tvNotificationTitle)
        val notificationMessage: TextView = itemView.findViewById(R.id.tvNotificationMessage)
        val notificationTime: TextView = itemView.findViewById(R.id.tvNotificationTime)
        val notificationIndicator: View = itemView.findViewById(R.id.viewUnreadIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.notificationTitle.text = notification.title
        holder.notificationMessage.text = notification.message
        holder.notificationTime.text = notification.timestamp

        // Set read/unread styling
        if (notification.isRead) {
            // Read notification styling
            holder.notificationTitle.typeface = Typeface.DEFAULT
            holder.notificationIndicator.visibility = View.INVISIBLE
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.transparent))
        } else {
            // Unread notification styling
            holder.notificationTitle.typeface = Typeface.DEFAULT_BOLD
            holder.notificationIndicator.visibility = View.VISIBLE
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.unread_background))
        }

        // Set click listener to mark as read when clicked
        holder.itemView.setOnClickListener {
            onNotificationClick(notification)
        }
    }

    override fun getItemCount(): Int = notifications.size

    fun updateNotifications(newNotifications: List<NotificationItem>) {
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged()
    }

    // Method to mark a notification as read
    fun markAsRead(notificationId: Int) {
        val position = notifications.indexOfFirst { it.id == notificationId }
        if (position >= 0) {
            val updatedNotification = notifications[position].copy(isRead = true)
            notifications[position] = updatedNotification
            notifyItemChanged(position)
        }
    }

    // Method to mark all notifications as read
    fun markAllAsRead() {
        for (i in notifications.indices) {
            notifications[i] = notifications[i].copy(isRead = true)
        }
        notifyDataSetChanged()
    }
}