package com.example.computer_bucket

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.Color
import android.widget.FrameLayout
import android.view.Gravity

class MainActivity : AppCompatActivity(), NotificationCountListener {

    private var notificationBadge: TextView? = null
    private val TAG = "MainActivity"
    private var bottomNavigation: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottom_navigation)
        setupNavigation()

        // Delay checking notifications to ensure views are properly initialized
        Handler().postDelayed({
            checkUnreadNotifications()
        }, 500)
    }

    private fun setupNavigation() {
        bottomNavigation?.let { navigation ->
            navigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> loadFragment(HomeFragment())
                    R.id.nav_orders -> loadFragment(OrdersFragment())
                    R.id.nav_notification -> loadFragment(NotificationFragment())
                    R.id.nav_profile -> loadFragment(ProfileFragment())
                }
                true
            }

            // Load initial fragment
            loadFragment(HomeFragment())
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }

    private fun checkUnreadNotifications() {
        // Get unread notification count from SharedPreferences
        val sharedPreferences = getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
        val readNotifications = sharedPreferences.getStringSet("read_notifications", emptySet()) ?: emptySet()

        Log.d(TAG, "Read notification IDs: $readNotifications")

        // Call API to get all notifications and count unread ones
        val userId = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getInt("user_id", -1)
        Log.d(TAG, "Checking notifications for user ID: $userId")

        if (userId != -1) {
            NotificationApiClient.instance.getNotifications(userId).enqueue(object : Callback<List<NotificationItem>> {
                override fun onResponse(call: Call<List<NotificationItem>>, response: Response<List<NotificationItem>>) {
                    Log.d(TAG, "Notification API response received: ${response.isSuccessful}")

                    if (response.isSuccessful && response.body() != null) {
                        val notifications = response.body()!!
                        Log.d(TAG, "Found ${notifications.size} notifications")

                        val readIds = readNotifications.mapNotNull { it.toIntOrNull() }.toSet()
                        val unreadCount = notifications.count { !readIds.contains(it.id) }

                        Log.d(TAG, "Unread count: $unreadCount")

                        // Update UI on main thread
                        runOnUiThread {
                            updateNotificationBadgeNewMethod(unreadCount)
                        }
                    } else {
                        Log.e(TAG, "API error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<NotificationItem>>, t: Throwable) {
                    Log.e(TAG, "API call failed", t)
                }
            })
        } else {
            Log.e(TAG, "Invalid user ID: $userId")
        }
    }

    override fun onUnreadCountChanged(count: Int) {
        Log.d(TAG, "onUnreadCountChanged called with count: $count")
        updateNotificationBadgeNewMethod(count)
    }

    private fun updateNotificationBadgeNewMethod(count: Int) {
        Log.d(TAG, "Updating badge with NEW method, count: $count")

        runOnUiThread {
            try {
                // Get the notification item view from BottomNavigationView
                bottomNavigation?.let { navigation ->
                    // First remove any existing badge
                    removeBadge(R.id.nav_notification)

                    // Only add badge if count > 0
                    if (count > 0) {
                        addBadge(R.id.nav_notification, count)
                        Log.d(TAG, "NEW badge method applied with count: $count")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error applying badge: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun addBadge(itemId: Int, count: Int) {
        val itemView = getBottomNavigationItemView(itemId)

        if (itemView == null) {
            Log.e(TAG, "Could not find view for menu item $itemId")
            return
        }

        // Create a FrameLayout to hold both the original view and the badge
        val container = FrameLayout(this)
        val parent = itemView.parent as ViewGroup
        val itemIndex = parent.indexOfChild(itemView)

        // Remove the original view from its parent
        parent.removeView(itemView)

        // Add the original view to our container
        container.addView(itemView)

        // Create and add the badge
        val badge = TextView(this)
        badge.id = R.id.badge_counter
        badge.text = if (count > 99) "99+" else count.toString()
        badge.setTextColor(Color.WHITE)
        badge.textSize = 10f
        badge.gravity = Gravity.CENTER
        badge.background = createBadgeBackground()

        val params = FrameLayout.LayoutParams(dpToPx(16), dpToPx(16))
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.topMargin = dpToPx(2) // Adjust this value as needed
        params.leftMargin = dpToPx(10) // Set your desired left margin
        badge.layoutParams = params


        container.addView(badge)

        // Add the container back to the parent at the same position
        parent.addView(container, itemIndex)

        Log.d(TAG, "Badge added to bottom navigation item")
    }

    private fun removeBadge(itemId: Int) {
        val itemView = getBottomNavigationItemView(itemId)
        if (itemView?.parent is FrameLayout) {
            val container = itemView.parent as FrameLayout
            val badge = container.findViewById<TextView>(R.id.badge_counter)
            if (badge != null) {
                container.removeView(badge)
                Log.d(TAG, "Badge removed from item")
            }
        }
    }

    private fun getBottomNavigationItemView(itemId: Int): View? {
        try {
            bottomNavigation?.let { navigation ->
                // Get the item position in the menu
                var menuItemIndex = -1

                // Loop through menu items to find the index
                val menu = navigation.menu
                for (i in 0 until menu.size()) {
                    val item = menu.getItem(i)
                    if (item.itemId == itemId) {
                        menuItemIndex = i
                        break
                    }
                }

                if (menuItemIndex == -1) {
                    Log.e(TAG, "Menu item with ID $itemId not found")
                    return null
                }

                Log.d(TAG, "Found menu item at index $menuItemIndex")

                // Get the BottomNavigationMenuView which is the first child of BottomNavigationView
                val menuView = navigation.getChildAt(0) as? ViewGroup
                if (menuView != null && menuItemIndex < menuView.childCount) {
                    // Get the item view at the found index
                    return menuView.getChildAt(menuItemIndex)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error finding navigation item view: ${e.message}")
            e.printStackTrace()
        }
        return null
    }

    private fun createBadgeBackground(): ShapeDrawable {
        val shape = OvalShape()
        return ShapeDrawable(shape).apply {
            paint.color = Color.RED
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}