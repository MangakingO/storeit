package com.example.storeit.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ItemChange(
    val itemId: String = "",
    val itemName: String = "",
    val changeType: String = "",
    val oldValue: String? = null,
    val newValue: String? = null,
    @ServerTimestamp
    val timestamp: Date? = null
)
