package com.example.computer_bucket

import android.os.Parcel
import android.os.Parcelable

data class Product(
    val product_id: Int,
    val product_name: String,
    val product_description: String,
    val product_price: Double,
    val product_sold: Int,
    val product_imgUrl: String,
    var quantity: Int,
    var isChecked: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt() == 1
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(product_id)
        parcel.writeString(product_name)
        parcel.writeString(product_description)
        parcel.writeDouble(product_price)
        parcel.writeInt(product_sold)
        parcel.writeString(product_imgUrl)
        parcel.writeInt(quantity)
        parcel.writeInt(if (isChecked) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Product> {
        override fun createFromParcel(parcel: Parcel): Product {
            return Product(parcel)
        }

        override fun newArray(size: Int): Array<Product?> {
            return arrayOfNulls(size)
        }
    }
}