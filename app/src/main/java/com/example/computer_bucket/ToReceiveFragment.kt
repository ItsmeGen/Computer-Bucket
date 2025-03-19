package com.example.computer_bucket

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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

class ToReceiveFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var groupedOrdersAdapter: GroupedOrdersAdapter
    private val groupedOrders = mutableListOf<GroupedOrder>()
    private var userId = 0
    private val orderStatus = "Out for Delivery"
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_to_receive, container, false)

        recyclerView = view.findViewById(R.id.toReceiveRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        groupedOrdersAdapter = GroupedOrdersAdapter(groupedOrders)
        recyclerView.adapter = groupedOrdersAdapter

        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE) // Corrected key to UserPrefs
        userId = sharedPreferences.getInt("user_id", 0) // Corrected key to user_id

        if (userId == 0) {
            Toast.makeText(context, "User ID not found.", Toast.LENGTH_SHORT).show()
            return view;
        }

        fetchOrders()

        return view
    }

    private fun fetchOrders() {
        val call = FilteredOrderApiClient.api.getOrders(userId, orderStatus)

        call.enqueue(object : Callback<List<OrderItems>> {
            override fun onResponse(
                call: Call<List<OrderItems>>,
                response: Response<List<OrderItems>>
            ) {
                if (response.isSuccessful) {
                    val orderItemsList = response.body() ?: emptyList()
                    val processedOrders = processOrderItems(orderItemsList)
                    groupedOrders.clear()
                    groupedOrders.addAll(processedOrders)
                    groupedOrdersAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    Log.e("Retrofit", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<OrderItems>>, t: Throwable) {
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("Retrofit", "Network error: ${t.message}")
            }
        })
    }
}