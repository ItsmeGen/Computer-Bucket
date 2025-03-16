package com.example.computer_bucket

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GroupedOrdersAdapter(private val orders: List<GroupedOrder>) :
    RecyclerView.Adapter<GroupedOrdersAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val customerName: TextView = view.findViewById(R.id.customerName)
        val orderStatus: TextView = view.findViewById(R.id.orderStatus)
        val totalPrice: TextView = view.findViewById(R.id.totalPrice)
        val productsRecyclerView: RecyclerView = view.findViewById(R.id.productsRecyclerView)
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
        holder.totalPrice.text = "Total: $${order.total_price}"

        // Set up nested RecyclerView for products
        holder.productsRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.productsRecyclerView.adapter = OrderProductsAdapter(order.items)
    }

    override fun getItemCount(): Int = orders.size
}