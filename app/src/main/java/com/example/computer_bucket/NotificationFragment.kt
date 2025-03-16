package com.example.computer_bucket

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private val notifications = mutableListOf<NotificationItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Set up "Mark All Read" button click listener
        val markAllReadBtn = view.findViewById<TextView>(R.id.btnMarkAllRead)
        markAllReadBtn.setOnClickListener {
            markAllNotificationsAsRead()
        }

        // Initialize adapter with click listener
        adapter = NotificationAdapter(notifications) { notification ->
            // Handle notification click - mark as read and perform action
            markNotificationAsRead(notification.id)
        }

        recyclerView.adapter = adapter

        fetchNotifications()

        return view
    }

    private fun fetchNotifications() {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(requireContext(), "User not found!", Toast.LENGTH_SHORT).show()
            return
        }

        NotificationApiClient.instance.getNotifications(userId).enqueue(object : Callback<List<NotificationItem>> {
            override fun onResponse(call: Call<List<NotificationItem>>, response: Response<List<NotificationItem>>) {
                if (response.isSuccessful) {
                    val notificationList = response.body()

                    if (notificationList != null) {
                        // Get read notification IDs from SharedPreferences
                        val readNotifications = getReadNotificationIds()

                        // Mark notifications as read if they're in the read list
                        val processedList = notificationList.map { notification ->
                            notification.copy(isRead = readNotifications.contains(notification.id))
                        }

                        adapter.updateNotifications(processedList)
                    } else {
                        println("DEBUG: Response body is null")
                    }
                } else {
                    println("DEBUG: Response failed, code: ${response.code()}")
                    Toast.makeText(requireContext(), "No notifications found!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<NotificationItem>>, t: Throwable) {
                println("DEBUG: API call failed: ${t.message}")
                Toast.makeText(requireContext(), "Failed to fetch notifications", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun markNotificationAsRead(notificationId: Int) {
        // Update UI
        adapter.markAsRead(notificationId)

        // Save to SharedPreferences
        val readNotifications = getReadNotificationIds().toMutableSet()
        readNotifications.add(notificationId)
        saveReadNotificationIds(readNotifications)
    }

    private fun markAllNotificationsAsRead() {
        // Update UI
        adapter.markAllAsRead()

        // Save all current notification IDs as read
        val allIds = notifications.map { it.id }.toSet()
        saveReadNotificationIds(allIds)

        Toast.makeText(requireContext(), "All notifications marked as read", Toast.LENGTH_SHORT).show()
    }

    private fun getReadNotificationIds(): Set<Int> {
        val sharedPreferences = requireContext().getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getStringSet("read_notifications", emptySet())?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    }

    private fun saveReadNotificationIds(ids: Set<Int>) {
        val sharedPreferences = requireContext().getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putStringSet("read_notifications", ids.map { it.toString() }.toSet()).apply()
    }
}