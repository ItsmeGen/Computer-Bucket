package com.example.computer_bucket

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

interface NotificationCountListener {
    fun onUnreadCountChanged(count: Int)
}

class NotificationFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private val notifications = mutableListOf<NotificationItem>()
    private var notificationCountListener: NotificationCountListener? = null
    private val TAG = "NotificationFragment"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Try to get the listener from the activity
        if (context is NotificationCountListener) {
            notificationCountListener = context
            Log.d(TAG, "NotificationCountListener attached successfully")
        } else {
            Log.e(TAG, "Activity does not implement NotificationCountListener")
        }
    }

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

        // Fetch notifications after a short delay to ensure activity is fully initialized
        Handler(Looper.getMainLooper()).postDelayed({
            fetchNotifications()
        }, 300)

        return view
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called, refreshing notifications")
        // Refresh notifications when the fragment becomes visible
        fetchNotifications()
    }

    private fun fetchNotifications() {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId == -1) {
            Log.e(TAG, "Invalid user ID: $userId")
            Toast.makeText(requireContext(), "User not found!", Toast.LENGTH_SHORT).show()
            return
        }

        NotificationApiClient.instance.getNotifications(userId).enqueue(object : Callback<List<NotificationItem>> {
            override fun onResponse(call: Call<List<NotificationItem>>, response: Response<List<NotificationItem>>) {
                if (response.isSuccessful) {
                    val notificationList = response.body()
                    Log.d(TAG, "Successfully fetched ${notificationList?.size ?: 0} notifications")

                    if (notificationList != null) {
                        // Get read notification IDs from SharedPreferences
                        val readNotifications = getReadNotificationIds()
                        Log.d(TAG, "Read notification IDs: $readNotifications")

                        // Mark notifications as read if they're in the read list
                        val processedList = notificationList.map { notification ->
                            notification.copy(isRead = readNotifications.contains(notification.id))
                        }

                        // Update on main thread to avoid race conditions
                        Handler(Looper.getMainLooper()).post {
                            adapter.updateNotifications(processedList)
                            notifications.clear()
                            notifications.addAll(processedList)

                            // Count unread notifications and update badge
                            updateUnreadCount()
                        }
                    } else {
                        Log.e(TAG, "Response body is null")
                    }
                } else {
                    Log.e(TAG, "Response failed, code: ${response.code()}")
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "No notifications found!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<List<NotificationItem>>, t: Throwable) {
                Log.e(TAG, "API call failed: ${t.message}")
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to fetch notifications", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun markNotificationAsRead(notificationId: Int) {
        Log.d(TAG, "Marking notification $notificationId as read")
        // Update UI
        adapter.markAsRead(notificationId)

        // Save to SharedPreferences
        val readNotifications = getReadNotificationIds().toMutableSet()
        readNotifications.add(notificationId)
        saveReadNotificationIds(readNotifications)

        // Update unread count
        updateUnreadCount()
    }

    private fun markAllNotificationsAsRead() {
        Log.d(TAG, "Marking all notifications as read")
        // Update UI
        adapter.markAllAsRead()

        // Save all current notification IDs as read
        val allIds = notifications.map { it.id }.toSet()
        saveReadNotificationIds(allIds)

        // Update unread count
        updateUnreadCount()

        Toast.makeText(requireContext(), "All notifications marked as read", Toast.LENGTH_SHORT).show()
    }

    private fun updateUnreadCount() {
        val unreadCount = notifications.count { !it.isRead }
        Log.d(TAG, "Updating unread count: $unreadCount")

        // Use Handler to ensure we're on the main thread
        Handler(Looper.getMainLooper()).post {
            try {
                notificationCountListener?.onUnreadCountChanged(unreadCount)
                Log.d(TAG, "Notified listener about unread count change: $unreadCount")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating unread count: ${e.message}")
            }
        }
    }

    private fun getReadNotificationIds(): Set<Int> {
        val sharedPreferences = requireContext().getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
        val stringSet = sharedPreferences.getStringSet("read_notifications", emptySet()) ?: emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    private fun saveReadNotificationIds(ids: Set<Int>) {
        val sharedPreferences = requireContext().getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("read_notifications", ids.map { it.toString() }.toSet())
        val success = editor.commit() // Using commit() instead of apply() for immediate write
        Log.d(TAG, "Saved read notification IDs: $ids, success: $success")
    }
}