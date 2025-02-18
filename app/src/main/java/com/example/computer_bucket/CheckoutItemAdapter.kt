package com.example.computer_bucket

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Locale

class CheckoutItemAdapter(
    private val context: Context,
    private val selectedProducts: List<Product>
) : RecyclerView.Adapter<CheckoutItemAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.checkoutProductImage)
        val productName: TextView = itemView.findViewById(R.id.checkoutProductName)
        val productPrice: TextView = itemView.findViewById(R.id.checkoutProductPrice)
        val productQuantity: TextView = itemView.findViewById(R.id.checkoutProductQuantity)
        val productTotalPrice: TextView = itemView.findViewById(R.id.checkoutProductTotalPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_checkout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = selectedProducts[position]
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "PH"))

        // Load product image
        Glide.with(context)
            .load(product.product_imgUrl)
            .placeholder(R.drawable.loading_image)
            .error(R.drawable.arrow_back)
            .into(holder.productImage)

        // Set product details
        holder.productName.text = product.product_name
        holder.productPrice.text = currencyFormatter.format(product.product_price.toDouble())
        holder.productQuantity.text = "× ${product.quantity}"

        // Calculate and set total price for this product
        val totalPrice = product.product_price.toDouble() * product.quantity
        holder.productTotalPrice.text = currencyFormatter.format(totalPrice)
    }

    override fun getItemCount(): Int = selectedProducts.size
}