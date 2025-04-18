package com.example.computer_bucket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrdersFragment : Fragment() {

    private lateinit var tvToPay: TextView
    private lateinit var tvToShip: TextView
    private lateinit var tvToReceive: TextView
    private lateinit var tvDelivered: TextView
    private lateinit var tvCompleted: TextView
    private lateinit var tvReturned: TextView
    private lateinit var tvCancelled: TextView
    private lateinit var orderContentContainer: FrameLayout

    private lateinit var ordersRecyclerView: RecyclerView
    private lateinit var ordersAdapter: GroupedOrdersAdapter
    private var groupedOrdersList = mutableListOf<GroupedOrder>()
    private val orderStatus = "Processing"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_orders, container, false)

        tvToPay = view.findViewById(R.id.tvToPay)
        tvToShip = view.findViewById(R.id.tvToShip)
        tvToReceive = view.findViewById(R.id.tvToReceive)
        tvDelivered = view.findViewById(R.id.tvDelivered)
        tvCompleted = view.findViewById(R.id.tvCompleted)
        tvReturned = view.findViewById(R.id.tvReturned)
        tvCancelled = view.findViewById(R.id.tvCancelled)
        orderContentContainer = view.findViewById(R.id.orderContentContainer)
        ordersRecyclerView = view.findViewById(R.id.toPayRecyclerView)

        ordersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        ordersAdapter = GroupedOrdersAdapter(groupedOrdersList)
        ordersRecyclerView.adapter = ordersAdapter

        replaceFragment(ToPayFragment())
        selectTab(tvToPay)

        tvToPay.setOnClickListener {
            replaceFragment(ToPayFragment())
            ordersRecyclerView.visibility = View.VISIBLE
            selectTab(tvToPay)
            fetchOrders() // Fetch only when "To Pay" is clicked
        }
        tvToShip.setOnClickListener {
            loadFragment(ToShipFragment())
            selectTab(tvToShip)
        }
        tvToReceive.setOnClickListener {
            loadFragment(ToReceiveFragment())
            selectTab(tvToReceive)
        }
        tvDelivered.setOnClickListener {
            loadFragment(DeliveredFragment())
            selectTab(tvDelivered)
        }
        tvCompleted.setOnClickListener {
            loadFragment(CompletedFragment())
            selectTab(tvCompleted)
        }
        tvReturned.setOnClickListener{
            loadFragment(ReturnedFragment())
            selectTab(tvReturned)
        }
        tvCancelled.setOnClickListener{
            loadFragment(CancelledFragment())
            selectTab(tvCancelled)
        }

        fetchOrders() // Fetch on initial load

        return view
    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.orderContentContainer, fragment)
            .commit()
    }

    private fun loadFragment(fragment: Fragment) {
        ordersRecyclerView.visibility = View.GONE

        childFragmentManager.beginTransaction()
            .replace(R.id.orderContentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        ordersRecyclerView.visibility = View.VISIBLE
    }

    private fun fetchOrders() {
        val userId = getUserId()

        if (userId == -1) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        FilteredOrderApiClient.api.getOrders(userId, orderStatus)
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
        return sharedPreferences.getInt("user_id", -1)
    }

    private fun selectTab(selectedTextView: TextView) {
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.selected_tab_color)
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.default_tab_color)

        tvToPay.setTextColor(if (tvToPay == selectedTextView) selectedColor else defaultColor)
        tvToShip.setTextColor(if (tvToShip == selectedTextView) selectedColor else defaultColor)
        tvToReceive.setTextColor(if (tvToReceive == selectedTextView) selectedColor else defaultColor)
        tvDelivered.setTextColor(if (tvDelivered == selectedTextView) selectedColor else defaultColor)
        tvCompleted.setTextColor(if (tvCompleted == selectedTextView) selectedColor else defaultColor)
    }
}