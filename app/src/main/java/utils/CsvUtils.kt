package com.example.storeit.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import com.example.storeit.data.model.InventoryItem

fun generateCsvContent(items: List<InventoryItem>): String {
    val header = "Name,Quantity,Price,SKU,Description,Reorder Point\n"
    return header + items.joinToString(separator = "\n") {
        // Escape commas in fields to prevent CSV corruption
        val name = it.name.replace(",", "")
        val description = it.description?.replace(",", "") ?: ""
        "$name,${it.quantity},${it.price},${it.sku ?: ""},$description,${it.reorderPoint ?: ""}"
    }
}

fun launchCsvExport(context: Context, csvLauncher: ActivityResultLauncher<Intent>, inventoryName: String) {
    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "text/csv"
        putExtra(Intent.EXTRA_TITLE, "$inventoryName.csv")
    }
    csvLauncher.launch(intent)
}

fun writeCsvToFile(context: Context, uri: Uri, content: String) {
    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(content.toByteArray())
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
