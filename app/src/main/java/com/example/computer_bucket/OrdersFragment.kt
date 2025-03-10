package com.example.computer_bucket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrdersFragment : Fragment() {

    private lateinit var ordersRecyclerView: RecyclerView
    private lateinit var ordersAdapter: GroupedOrdersAdapter
    private var groupedOrdersList = mutableListOf<GroupedOrder>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_orders, container, false)
        ordersRecyclerView = view.findViewById(R.id.ordersRecyclerView)
        ordersRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        ordersAdapter = GroupedOrdersAdapter(groupedOrdersList)
        ordersRecyclerView.adapter = ordersAdapter

        fetchOrders()

        return view
    }

    private fun fetchOrders() {
        val userId = getUserId()

        if (userId == -1) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Fetch orders from API
        OrderFetchApiClient.OrderFetchApiService.getOrders(userId)
            .enqueue(object : Callback<List<OrderItems>> {
                override fun onResponse(call: Call<List<OrderItems>>, response: Response<List<OrderItems>>) {
                    if (response.isSuccessful) {
                        response.body()?.let { orderItems ->
                            val groupedOrders = processOrderItems(orderItems)

                            groupedOrdersList.clear()
                            groupedOrdersList.addAll(groupedOrders)
                            ordersAdapter.notifyDataSetChanged()

                            if (groupedOrders.isEmpty()) {
                                Toast.makeText(requireContext(), "No orders found", Toast.LENGTH_SHORT).show()
                            }
                        } ?: run {
                            Toast.makeText(requireContext(), "Empty response from server", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to load orders: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<OrderItems>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun getUserId(): Int {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1) // Default is -1 if not found
    }
}
