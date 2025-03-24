package com.example.computer_bucket

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AddToCartApiService {
    @FormUrlEncoded
    @POST("cart.php")
    fun addToCart(
        @Field("user_id") userId: Int,
        @Field("product_id") productId: Int,
        @Field("product_name") productName: String,
        @Field("product_description") productDescription: String,
        @Field("product_price") productPrice: Double,
        @Field("product_sold") productSold: Int,
        @Field("product_imgUrl") productImgUrl: String,
        @Field("quantity") quantity: Int,
        @Field("action") action: String
    ): Call<Boolean>

    @FormUrlEncoded
    @POST("cart.php")
    fun getCartItems(
        @Field("user_id") userId: Int,
        @Field("action") action: String
    ): Call<List<Product>>

    @FormUrlEncoded
    @POST("cart.php")
    fun removeFromCart(
        @Field("user_id") userId: Int,
        @Field("product_id") productId: Int,
        @Field("action") action: String
    ): Call<Boolean>

    @FormUrlEncoded
    @POST("cart.php")
    fun clearCart(
        @Field("user_id") userId: Int,
        @Field("action") action: String
    ): Call<Boolean>

    @FormUrlEncoded
    @POST("cart.php")
    fun updateCartItemQuantity(
        @Field("product_id") productId: Int,
        @Field("user_id") userId: Int,
        @Field("quantity") quantity: Int,
        @Field("action") action: String
    ): Call<Boolean>
}