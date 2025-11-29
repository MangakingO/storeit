package com.example.storeit.data

import com.example.storeit.data.model.ItemChange
import com.example.storeit.data.model.InventoryItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.tasks.await

/**
 * Provides CRUD access to a specified inventory collection stored in Firestore.
 */
class InventoryRepository(private val firestore: FirebaseFirestore) {

    private fun inventoryItemsCollection(inventoryId: String) =
        firestore.collection("inventories").document(inventoryId).collection("items")

    suspend fun getInventoryName(inventoryId: String): String {
        return try {
            firestore.collection("inventories").document(inventoryId).get().await().getString("name") ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun listenToItems(inventoryId: String, onItemsUpdated: (List<InventoryItem>) -> Unit): ListenerRegistration {
        return inventoryItemsCollection(inventoryId)
            .orderBy("name")
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    error.printStackTrace()
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.map { doc ->
                    doc.toObject(InventoryItem::class.java)!!.apply {
                        hasPendingWrites = doc.metadata.hasPendingWrites()
                    }
                } ?: emptyList()
                onItemsUpdated(items)
            }
    }

    suspend fun addItem(
        inventoryId: String,
        name: String,
        quantity: Int,
        description: String?,
        price: String,
        inStock: Boolean,
        sku: String?,
        imageRes: Int?,
        reorderPoint: Int?
    ) {
        val data = mutableMapOf<String, Any?>(
            "name" to name,
            "quantity" to quantity,
            "description" to description,
            "price" to price,
            "inStock" to inStock,
            "sku" to sku,
            "imageRes" to imageRes,
            "reorderPoint" to reorderPoint
        )
        inventoryItemsCollection(inventoryId).add(data).await()
    }

    suspend fun updateItem(
        inventoryId: String,
        itemId: String,
        name: String,
        quantity: Int,
        description: String?,
        price: String,
        inStock: Boolean,
        sku: String?,
        imageRes: Int?,
        reorderPoint: Int?
    ) {
        val data = mutableMapOf<String, Any?>(
            "name" to name,
            "quantity" to quantity,
            "description" to description,
            "price" to price,
            "inStock" to inStock,
            "sku" to sku,
            "imageRes" to imageRes,
            "reorderPoint" to reorderPoint
        )
        inventoryItemsCollection(inventoryId).document(itemId).set(data).await()
    }

    suspend fun deleteItem(inventoryId: String, itemId: String) {
        if (itemId.isBlank()) return
        inventoryItemsCollection(inventoryId).document(itemId).delete().await()
    }

    suspend fun logItemChange(inventoryId: String, itemChange: ItemChange) {
        firestore.collection("inventories").document(inventoryId).collection("item_changes").add(itemChange).await()
    }
}
