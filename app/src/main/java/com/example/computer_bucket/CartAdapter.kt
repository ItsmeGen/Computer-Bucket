package com.example.computer_bucket

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CartAdapter(
    private val context: Context,
    private var cartItems: MutableList<Product>,
    private val databaseHelper: DataBaseHelper,
    private val userId: Int,  // Current logged-in user ID
    private val onItemRemoved: () -> Unit  // Callback to refresh UI after item removal
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.cartProductImage)
        val productName: TextView = itemView.findViewById(R.id.cartProductName)
        val productPrice: TextView = itemView.findViewById(R.id.cartProductPrice)
        val removeButton: Button = itemView.findViewById(R.id.removeFromCartButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val product = cartItems[position]

        holder.productName.text = product.product_name
        holder.productPrice.text = "₱${product.product_price}"

        // Load product image using Glide
        Glide.with(context)
            .load(product.product_imgUrl)
            .placeholder(R.drawable.loading_image)
            .error(R.drawable.arrow_back)
            .into(holder.productImage)

        // Remove item from cart when button is clicked
        holder.removeButton.setOnClickListener {
            databaseHelper.removeFromCart(userId, product.product_id)
            cartItems.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, cartItems.size)
            onItemRemoved()  // Callback to refresh UI if needed
        }
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    fun updateCart(newCartItems: List<Product>) {
        cartItems.clear()
        cartItems.addAll(newCartItems)
        notifyDataSetChanged()
    }
}
