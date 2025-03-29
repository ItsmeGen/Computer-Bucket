package com.example.computer_bucket

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private val apiService = ApiClient.create()
    private val TAG = "ProfileFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val txtUsername: TextView = view.findViewById(R.id.txtUsername)
        val btnLogin: Button = view.findViewById(R.id.btnLogin)
        val btnLogout: Button = view.findViewById(R.id.btnLogout)
        recyclerView = view.findViewById(R.id.recyclerView)

        val sharedPref = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)
        val username = sharedPref.getString("username", "N/A")

        if (userId == -1) {
            txtUsername.text = "Username: N/A"
            btnLogout.visibility = View.GONE
            btnLogin.visibility = View.VISIBLE
        } else {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchProducts()
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView")
        productAdapter = ProductAdapter { product ->
            val intent = Intent(requireContext(), ProductDetailActivity::class.java).apply {
                putExtra("product_id", product.product_id)
                putExtra("product_name", product.product_name)
                putExtra("product_description", product.product_description)
                putExtra("product_price", product.product_price)
                putExtra("product_imgUrl", product.product_imgUrl)
            }
            startActivity(intent)
        }

        recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
        }
    }

    private fun fetchProducts() {
        Log.d(TAG, "Fetching products from API")
        apiService.productlist().enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful && response.body() != null) {
                    productAdapter.submitList(response.body())
                } else {
                    Log.e(TAG, "API error: ${response.code()} - ${response.message()}")
                    Toast.makeText(requireContext(), "Failed to load products", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                Log.e(TAG, "API call failed", t)
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
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
