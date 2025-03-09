package com.example.computer_bucket

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNavigation()

    }

    private fun setupNavigation() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        loadFragment(HomeFragment())

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_notification -> loadFragment(NotificationFragment())
                R.id.nav_profile -> loadFragment(ProfileFragment())
            }
            true
        }
    }


    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }


}