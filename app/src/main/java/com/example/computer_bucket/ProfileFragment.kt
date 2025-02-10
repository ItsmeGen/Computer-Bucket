package com.example.computer_bucket

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val txtUserId: TextView = view.findViewById(R.id.txtUserId)
        val txtUsername: TextView = view.findViewById(R.id.txtUsername)
        val btnLogout: Button = view.findViewById(R.id.btnLogout)

        // Get user details from SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)
        val username = sharedPref.getString("username", "N/A")

        if (userId == -1) {
            Toast.makeText(requireContext(), "User not found. Please log in.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), Login::class.java))
            requireActivity().finish()
        } else {
            // Display user details
            txtUserId.text = "User ID: $userId"
            txtUsername.text = "Username: $username"
        }

        btnLogout.setOnClickListener {
            logoutUser()
        }

        return view
    }

    private fun logoutUser() {
        val sharedPref = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear() // Clears all saved user data
            apply()
        }

        val intent = Intent(activity, Login::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}
