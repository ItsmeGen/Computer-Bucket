package com.example.computer_bucket

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GroupedOrdersAdapter(private val orders: MutableList<GroupedOrder>) :
    RecyclerView.Adapter<GroupedOrdersAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val customerName: TextView = view.findViewById(R.id.customerName)
        val orderStatus: TextView = view.findViewById(R.id.orderStatus)
        val totalPrice: TextView = view.findViewById(R.id.totalPrice)
        val productsRecyclerView: RecyclerView = view.findViewById(R.id.productsRecyclerView)
        val cancelButton: Button = view.findViewById(R.id.cancelButton)
        val returnButton: Button = view.findViewById(R.id.returnButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grouped_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.customerName.text = "Customer: ${order.customer_name}"
        holder.orderStatus.text = "Status: ${order.order_status}"
        holder.totalPrice.text = "Total: ₱${order.total_price}"

        // Set up nested RecyclerView for products
        holder.productsRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.productsRecyclerView.adapter = OrderProductsAdapter(order.items)

        // Show cancel button only if order is "Processing"
        if (order.order_status == "Processing") {
            holder.cancelButton.visibility = View.VISIBLE
            holder.cancelButton.setOnClickListener {
                Log.d("DEBUG", "Cancelling Order ID: ${order.id}")
                if (order.id == null) {
                    Toast.makeText(holder.itemView.context, "Order ID is missing", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val params = mapOf("id" to order.id)
                val apiService = CancelOrderApiClient.instance
                val call = apiService.updateOrderStatus(params)
                call.enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful) {
                            val apiResponse = response.body()
                            if (apiResponse?.success == true) {
                                orders[position].order_status = "Cancelled"
                                notifyItemChanged(position)
                                Toast.makeText(holder.itemView.context, "Order cancelled successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(holder.itemView.context, "Failed to cancel order: ${apiResponse?.error}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(holder.itemView.context, "API Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Toast.makeText(holder.itemView.context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        } else {
            holder.cancelButton.visibility = View.GONE
        }

        // Show return button only if order is "Delivered"
        if (order.order_status == "Delivered") {
            holder.returnButton.visibility = View.VISIBLE
            holder.returnButton.setOnClickListener {
                Log.d("DEBUG", "Returning Order ID: ${order.id}")
                if (order.id == null) {
                    Toast.makeText(holder.itemView.context, "Order ID is missing", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                try {
                    val orderIdInt = order.id.toInt() // Convert to Int
                    val request = ReturnOrderRequest(id = orderIdInt)
                    val apiService = ReturnOrderApiClient.instance
                    val call = apiService.returnOrder(request)

                    call.enqueue(object : Callback<ApiResponse> {
                        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                            if (response.isSuccessful) {
                                val apiResponse = response.body()
                                if (apiResponse?.success == true) {
                                    orders[position].order_status = "Returned"
                                    notifyItemChanged(position)
                                    Toast.makeText(holder.itemView.context, "Order returned successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(holder.itemView.context, "Failed to return order: ${apiResponse?.error}", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(holder.itemView.context, "API Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                            Toast.makeText(holder.itemView.context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                            Log.e("ReturnOrder", "Network error", t)
                        }
                    })
                } catch (e: NumberFormatException) {
                    Toast.makeText(holder.itemView.context, "Invalid order ID format", Toast.LENGTH_SHORT).show()
                    Log.e("ReturnOrder", "Invalid Order ID Format", e)
                }

            }
        } else {
            holder.returnButton.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = orders.size
}