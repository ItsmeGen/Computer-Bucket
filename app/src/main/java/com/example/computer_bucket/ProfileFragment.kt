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
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val txtUserId: TextView = view.findViewById(R.id.txtUserId)
        val txtUsername: TextView = view.findViewById(R.id.txtUsername)
        val btnLogin: Button = view.findViewById(R.id.btnLogin)
        val btnLogout: Button = view.findViewById(R.id.btnLogout)

        val sharedPref = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)
        val username = sharedPref.getString("username", "N/A")

        if (userId == -1) {
            txtUserId.text = "User ID: N/A"
            txtUsername.text = "Username: N/A"
            btnLogout.visibility = View.GONE
            btnLogin.visibility = View.VISIBLE
        } else {
            txtUserId.text = "User ID: $userId"
            txtUsername.text = "Username: $username"
            btnLogin.visibility = View.GONE
            btnLogout.visibility = View.VISIBLE
        }

        btnLogin.setOnClickListener {
            startActivity(Intent(requireContext(), Login::class.java))
            requireActivity().finish()
        }

        btnLogout.setOnClickListener {
            logoutUser()
        }

        return view
    }

    private fun logoutUser() {
        val sharedPref = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
        Toast.makeText(requireContext(), "Successfully Logged Out", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireContext(), Login::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}
