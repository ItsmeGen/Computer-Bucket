package com.example.computer_bucket

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartAdapter(
    private val context: Context,
    private val cartItems: MutableList<Product>,
    private val userId: Int,
    private val onQuantityChanged: (Int, Int) -> Unit,
    private val onItemRemoved: () -> Unit,
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val selectedItems = mutableSetOf<Int>()

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.cartProductImage)
        val productName: TextView = itemView.findViewById(R.id.cartProductName)
        val productPrice: TextView = itemView.findViewById(R.id.cartProductPrice)
        val productQuantity: TextView = itemView.findViewById(R.id.cartProductQuantity)
        val buttonPlus: Button = itemView.findViewById(R.id.btnIncreaseQuantity)
        val buttonMinus: Button = itemView.findViewById(R.id.btnDecreaseQuantity)
        val removeButton: Button = itemView.findViewById(R.id.removeFromCartButton)
        val checkBox: CheckBox = itemView.findViewById(R.id.cartCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]

        Glide.with(context)
            .load(cartItem.product_imgUrl)
            .placeholder(R.drawable.loading_image)
            .error(R.drawable.arrow_back)
            .into(holder.productImage)

        holder.productName.text = cartItem.product_name
        holder.productPrice.text = "₱${cartItem.product_price}"
        holder.productQuantity.text = cartItem.quantity.toString()

        holder.buttonPlus.setOnClickListener {
            val currentQuantity = cartItem.quantity
            if (currentQuantity < 10) {
                cartItem.quantity++
                holder.productQuantity.text = cartItem.quantity.toString()
                onQuantityChanged(cartItem.product_id, cartItem.quantity)
            } else {
                Toast.makeText(context, "Maximum quantity is 10", Toast.LENGTH_SHORT).show()
            }
        }

        holder.buttonMinus.setOnClickListener {
            val currentQuantity = cartItem.quantity
            if (currentQuantity > 1) {
                cartItem.quantity--
                holder.productQuantity.text = cartItem.quantity.toString()
                onQuantityChanged(cartItem.product_id, cartItem.quantity)
            }
        }

        holder.removeButton.setOnClickListener {
            showRemoveConfirmationDialog(cartItem.product_id, position)
        }

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedItems.add(cartItem.product_id)
            } else {
                selectedItems.remove(cartItem.product_id)
            }
            onSelectionChanged(calculateTotal())
        }
    }

    private fun showRemoveConfirmationDialog(productId: Int, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Remove Item")
            .setMessage("Are you sure you want to remove this item from your cart?")
            .setPositiveButton("Yes") { _, _ ->
                removeItemFromCart(productId, position)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun removeItemFromCart(productId: Int, position: Int) {
        val apiService = AddToCartApiClient.apiService
        val call = apiService.removeFromCart(userId, productId, "removeFromCart")

        call.enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                if (response.isSuccessful) {
                    val isRemoved = response.body() ?: false
                    if (isRemoved) {
                        cartItems.removeAt(position)
                        notifyItemRemoved(position)
                        onItemRemoved()
                        onSelectionChanged(calculateTotal())
                        Toast.makeText(context, "Item removed from cart.", Toast.LENGTH_SHORT).show() // Toast
                    } else {
                        Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show()
                    Log.e("CartAdapter", "Error removing item: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                Toast.makeText(context, "Network error removing item", Toast.LENGTH_SHORT).show()
                Log.e("CartAdapter", "Network error: ${t.message}")
            }
        })
    }

    private fun calculateTotal(): Int {
        var total = 0
        for (item in cartItems) {
            if (selectedItems.contains(item.product_id)) {
                total += (item.product_price * item.quantity).toInt()
            }
        }
        return total
    }

    fun getSelectedProducts(): List<Product> {
        return cartItems.filter { selectedItems.contains(it.product_id) }
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }
}