package com.example.computer_bucket

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ToPayFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var groupedOrdersAdapter: GroupedOrdersAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateTextView: TextView

    private val groupedOrders = mutableListOf<GroupedOrder>()
    private var userId = 0
    private val orderStatus = "Processing"
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_to_pay, container, false)

        // Initialize views
        recyclerView = view.findViewById(R.id.toPayRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        groupedOrdersAdapter = GroupedOrdersAdapter(groupedOrders)
        recyclerView.adapter = groupedOrdersAdapter

        // Get User ID
        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("user_id", 0)

        // Validate User ID
        if (userId == 0) {
            showErrorState("User ID not found")
            return view
        }

        // Fetch Orders
        fetchOrders()

        return view
    }

    private fun fetchOrders() {
        // Show loading state
        setLoadingState(true)

        val call = FilteredOrderApiClient.api.getOrders(userId, orderStatus)
        call.enqueue(object : Callback<List<OrderItems>> {
            override fun onResponse(
                call: Call<List<OrderItems>>,
                response: Response<List<OrderItems>>
            ) {
                // Hide loading state
                setLoadingState(false)

                if (response.isSuccessful) {
                    val orderItemsList = response.body() ?: emptyList()

                    if (orderItemsList.isEmpty()) {
                        // Handle empty order list
                        showEmptyState()
                    } else {
                        // Process and display orders
                        val processedOrders = processOrderItems(orderItemsList)
                        groupedOrders.clear()
                        groupedOrders.addAll(processedOrders)
                        groupedOrdersAdapter.notifyDataSetChanged()

                        // Hide empty state if orders exist
                        emptyStateTextView.isVisible = false
                        recyclerView.isVisible = true
                    }
                } else {
                    showErrorState("Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<OrderItems>>, t: Throwable) {
                // Hide loading state
                setLoadingState(false)

                // Show network error
                showErrorState("Network error: ${t.message}")
            }
        })
    }

    private fun setLoadingState(isLoading: Boolean) {
        progressBar.isVisible = isLoading
        recyclerView.isVisible = !isLoading
    }

    private fun showEmptyState() {
        emptyStateTextView.text = "No processing orders found"
        emptyStateTextView.isVisible = true
        recyclerView.isVisible = false
    }

    private fun showErrorState(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        Log.e("ToPayFragment", message)

        emptyStateTextView.text = message
        emptyStateTextView.isVisible = true
        recyclerView.isVisible = false
    }
}