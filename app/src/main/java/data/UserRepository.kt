package com.example.storeit.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val role: String?,
    val inventoryId: String?,
    val inventories: Map<String, String> = emptyMap()
)

class UserRepository(private val firestore: FirebaseFirestore) {

    suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val role = userDoc.getString("role")
            val inventoryId = userDoc.getString("inventoryId")
            val inventories = userDoc.get("inventories") as? Map<String, String> ?: emptyMap()
            UserProfile(role, inventoryId, inventories)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createUser(userId: String) {
        val user = mapOf("role" to "employee")
        firestore.collection("users").document(userId).set(user).await()
    }
}
