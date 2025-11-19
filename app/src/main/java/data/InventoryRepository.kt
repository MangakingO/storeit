package com.example.storeit.data

import com.example.storeit.data.model.InventoryItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.tasks.await

/**
 * Provides CRUD access to user scoped inventory collections stored in Firestore.
 */
class InventoryRepository(private val firestore: FirebaseFirestore) {

    private fun userItemsCollection(userId: String) =
        firestore.collection("users").document(userId).collection("items")

    fun listenToItems(userId: String, onItemsUpdated: (List<InventoryItem>) -> Unit): ListenerRegistration {
        return userItemsCollection(userId)
            .orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    error.printStackTrace()
                    return@addSnapshotListener
                }

                val items = snapshot?.toObjects<InventoryItem>() ?: emptyList()
                onItemsUpdated(items)
            }
    }

    suspend fun addItem(
        userId: String,
        name: String,
        quantity: Int,
        description: String?,
        price: String,
        inStock: Boolean,
        sku: String?,
        imageRes: Int?
    ) {
        val data = mutableMapOf<String, Any?>(
            "name" to name,
            "quantity" to quantity,
            "description" to description,
            "price" to price,
            "inStock" to inStock,
            "sku" to sku,
            "imageRes" to imageRes
        )
        userItemsCollection(userId).add(data).await()
    }

    suspend fun updateItem(
        userId: String,
        itemId: String,
        name: String,
        quantity: Int,
        description: String?,
        price: String,
        inStock: Boolean,
        sku: String?,
        imageRes: Int?
    ) {
        val data = mutableMapOf<String, Any?>(
            "name" to name,
            "quantity" to quantity,
            "description" to description,
            "price" to price,
            "inStock" to inStock,
            "sku" to sku,
            "imageRes" to imageRes
        )
        userItemsCollection(userId).document(itemId).set(data).await()
    }

    suspend fun deleteItem(userId: String, itemId: String) {
        if (itemId.isBlank()) return
        userItemsCollection(userId).document(itemId).delete().await()
    }
}
