package com.example.computer_bucket

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.computer_bucket.databinding.FragmentHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var productAdapter: ProductAdapter
    private val apiService = ApiClient.create()
    private var allProducts: List<Product> = listOf()        // Complete list of products
    private var filteredProducts: MutableList<Product> = mutableListOf() // Filtered products
    private val TAG = "HomeFragment" // Tag for logging

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchBar()
        fetchProducts()

        val cartBtn: ImageView = view.findViewById(R.id.cart_btn)
        cartBtn.setOnClickListener {
            val intent = Intent(activity, CartActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSearchBar() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                Log.d(TAG, "Search query changed: '$query'")
                if (query.isEmpty()) {
                    Log.d(TAG, "Search bar cleared, resetting product list")
                    resetProductList()
                } else {
                    filterProducts(query)
                }
            }
        })
    }

    private fun filterProducts(query: String) {
        Log.d(TAG, "Filtering products with query: '$query'")
        filteredProducts.clear()

        for (product in allProducts) {
            if (product.product_name.lowercase(Locale.getDefault())
                    .contains(query.lowercase(Locale.getDefault()))) {
                filteredProducts.add(product)
            }
        }

        Log.d(TAG, "Filter results: ${filteredProducts.size} products matched the query")

        if (filteredProducts.isEmpty()) {
            binding.noResultsTextView.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.noResultsTextView.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }

        updateRecyclerView(filteredProducts)
    }

    private fun resetProductList() {
        Log.d(TAG, "Resetting product list to show all ${allProducts.size} products")
        filteredProducts.clear()
        filteredProducts.addAll(allProducts)

        binding.recyclerView.visibility = View.VISIBLE
        binding.noResultsTextView.visibility = View.GONE

        // ✅ Force RecyclerView to refresh
        updateRecyclerView(filteredProducts)
    }

    private fun updateRecyclerView(products: List<Product>) {
        val newList = ArrayList(products)
        productAdapter.submitList(newList) {
            binding.recyclerView.adapter = productAdapter // 🔥 Force RecyclerView refresh
        }
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView")
        productAdapter = ProductAdapter { product ->
            val intent = Intent(requireContext(), ProductDetailActivity::class.java).apply {
                putExtra("product_id", product.product_id)
                putExtra("product_name", product.product_name)
                putExtra("product_description", product.product_description)
                putExtra("product_price", product.product_price)
                putExtra("product_imgUrl", product.product_imgUrl)
            }
            startActivity(intent)
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
        }
    }

    private fun fetchProducts() {
        Log.d(TAG, "Fetching products from API")
        apiService.productlist().enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (!isAdded) return

                if (response.isSuccessful) {
                    response.body()?.let {
                        allProducts = it
                        filteredProducts.clear()
                        filteredProducts.addAll(it)

                        if (it.isEmpty()) {
                            binding.noResultsTextView.visibility = View.VISIBLE
                            binding.recyclerView.visibility = View.GONE
                        } else {
                            binding.noResultsTextView.visibility = View.GONE
                            binding.recyclerView.visibility = View.VISIBLE
                        }

                        updateRecyclerView(filteredProducts)
                    }
                } else {
                    Log.e(TAG, "API error: ${response.code()} - ${response.message()}")
                    Toast.makeText(requireContext(), "Failed to load products", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                if (isAdded) {
                    Log.e(TAG, "API call failed", t)
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
