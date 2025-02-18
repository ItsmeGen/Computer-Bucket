package com.example.computer_bucket

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CartAdapter(
    private val context: Context,
    private var cartItems: MutableList<Product>,
    private val databaseHelper: DataBaseHelper,
    private val userId: Int,  // Current logged-in user ID
    private val onQuantityChanged: (Int, Int) -> Unit, // Callback when quantity changes
    private val onItemRemoved: () -> Unit,
    private val onSelectionChanged: (Int) -> Unit // Callback to refresh UI after item removal
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {
    private val selectedItems = mutableSetOf<Int>()

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.cartProductImage)
        val productName: TextView = itemView.findViewById(R.id.cartProductName)
        val productPrice: TextView = itemView.findViewById(R.id.cartProductPrice)
        val quantityTextView: TextView = itemView.findViewById(R.id.cartProductQuantity)
        val increaseButton: Button = itemView.findViewById(R.id.btnIncreaseQuantity)
        val decreaseButton: Button = itemView.findViewById(R.id.btnDecreaseQuantity)
        val removeButton: Button = itemView.findViewById(R.id.removeFromCartButton)
        val checkBox: CheckBox = itemView.findViewById(R.id.cartCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }
    private var hasShownToast = false
    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val product = cartItems[position]

        holder.productName.text = product.product_name
        holder.productPrice.text = "₱${product.product_price}"
        holder.quantityTextView.text = "Qty: ${product.quantity}"

        // Load product image using Glide
        Glide.with(context)
            .load(product.product_imgUrl)
            .placeholder(R.drawable.loading_image)
            .error(R.drawable.arrow_back)
            .into(holder.productImage)

        holder.checkBox.isChecked = selectedItems.contains(product.product_id)

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedItems.add(product.product_id)
                // Update isChecked property of product
                product.isChecked = true
            } else {
                selectedItems.remove(product.product_id)
                // Update isChecked property of product
                product.isChecked = false
            }
            calculateTotal()
        }

        holder.productImage.setOnClickListener {
            navigateToProductDetail(product)
        }

        holder.productName.setOnClickListener {
            navigateToProductDetail(product)
        }

        holder.increaseButton.setOnClickListener {
            if (product.quantity < 10) {
                val newQuantity = product.quantity + 1
                updateQuantity(product, newQuantity)
                holder.increaseButton.isEnabled = false
            } else {
                Toast.makeText(context, "Maximum quantity is 10", Toast.LENGTH_SHORT).show()
                hasShownToast = true
            }

        }

        holder.decreaseButton.setOnClickListener {
            if (product.quantity > 1) {
                val newQuantity = product.quantity - 1
                updateQuantity(product, newQuantity)
            }
        }

        holder.removeButton.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Remove from Cart")
            builder.setMessage("Are you sure you want to remove this item from your cart?")

            builder.setPositiveButton("Yes") { dialog, _ ->
                databaseHelper.removeFromCart(userId, product.product_id)
                cartItems.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, cartItems.size)
                onItemRemoved()
                dialog.dismiss()
            }

            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

            val alertDialog = builder.create()
            alertDialog.show()
        }

    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    private fun updateQuantity(product: Product, newQuantity: Int) {
        databaseHelper.updateCartItemQuantity(product.product_id, userId, newQuantity)
        product.quantity = newQuantity
        notifyDataSetChanged()
        onQuantityChanged(product.product_id, newQuantity)
    }

    fun updateCart(newCartItems: List<Product>) {
        cartItems.clear()
        cartItems.addAll(newCartItems)
        notifyDataSetChanged()
    }

    private fun calculateTotal() {
        var total = 0.0
        for (product in cartItems) {
            if (selectedItems.contains(product.product_id)) {
                total += product.product_price.toDouble() * product.quantity
            }
        }
        onSelectionChanged(total.toInt())
    }

    private fun navigateToProductDetail(product: Product) {
        val intent = Intent(context, ProductDetailActivity::class.java).apply {
            putExtra("product_id", product.product_id)
            putExtra("product_name", product.product_name)
            putExtra("product_price", product.product_price)
            putExtra("product_description", product.product_description)
            putExtra("product_imgUrl", product.product_imgUrl)
        }
        context.startActivity(intent)
    }

    // New function to get the selected products for checkout
    fun getSelectedProducts(): List<Product> {
        return cartItems.filter { selectedItems.contains(it.product_id) }
    }
}