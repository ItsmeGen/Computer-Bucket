package com.example.computer_bucket

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DataBaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ComputerBucketDB"
        private const val DATABASE_VERSION = 1

        // Table Name
        private const val TABLE_CART = "cart"

        // Column Names
        private const val COLUMN_ID = "id"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_PRODUCT_ID = "product_id"
        private const val COLUMN_PRODUCT_NAME = "product_name"
        private const val COLUMN_PRODUCT_DESCRIPTION = "product_description"
        private const val COLUMN_PRODUCT_PRICE = "product_price"
        private const val COLUMN_PRODUCT_SOLD = "product_sold"
        private const val COLUMN_PRODUCT_IMG_URL = "product_imgUrl"
        private const val COLUMN_QUANTITY = "quantity"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """CREATE TABLE $TABLE_CART (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_USER_ID INTEGER,
            $COLUMN_PRODUCT_ID INTEGER,
            $COLUMN_PRODUCT_NAME TEXT,
            $COLUMN_PRODUCT_DESCRIPTION TEXT,
            $COLUMN_PRODUCT_PRICE REAL,
            $COLUMN_PRODUCT_SOLD INTEGER,
            $COLUMN_PRODUCT_IMG_URL TEXT,
            $COLUMN_QUANTITY INTEGER,
            UNIQUE($COLUMN_USER_ID, $COLUMN_PRODUCT_ID) ON CONFLICT REPLACE
        );""".trimIndent()

        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CART")
        onCreate(db)
    }

    // Function to add item to cart
    fun addToCart(userId: Int, product: Product, quantity: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues()

        values.put(COLUMN_USER_ID, userId)
        values.put(COLUMN_PRODUCT_ID, product.product_id)
        values.put(COLUMN_PRODUCT_NAME, product.product_name)
        values.put(COLUMN_PRODUCT_DESCRIPTION, product.product_description)
        values.put(COLUMN_PRODUCT_PRICE, product.product_price)
        values.put(COLUMN_PRODUCT_SOLD, product.product_sold)
        values.put(COLUMN_PRODUCT_IMG_URL, product.product_imgUrl)
        values.put(COLUMN_QUANTITY, quantity)

        val result = db.insertWithOnConflict(TABLE_CART, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
        return result != -1L
    }

    // Function to get all cart items for a specific user
    fun getCartItems(userId: Int): List<Product> {
        val cartItems = mutableListOf<Product>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_CART WHERE $COLUMN_USER_ID = ?"

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val product = Product(
                    product_id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ID)),
                    product_name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME)),
                    product_description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_DESCRIPTION)),
                    product_price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_PRICE)),
                    product_sold = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_SOLD)),
                    product_imgUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_IMG_URL))
                )
                cartItems.add(product)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return cartItems
    }

    // Function to remove an item from the cart
    fun removeFromCart(userId: Int, productId: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_CART, "$COLUMN_USER_ID = ? AND $COLUMN_PRODUCT_ID = ?", arrayOf(userId.toString(), productId.toString()))
        db.close()
        return result > 0
    }

    // Function to clear cart for a user
    fun clearCart(userId: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_CART, "$COLUMN_USER_ID = ?", arrayOf(userId.toString()))
        db.close()
        return result > 0
    }
}
