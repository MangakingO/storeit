package com.example.storeit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import com.example.storeit.ui.auth.AuthViewModel

data class InventoryItem(
    val name: String,
    val price: String,
    val quantity: Int,
    val inStock: Boolean,
    val SKU: String? = null,
    val imageRes: Int? = null,
    val description: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(navController: NavController, authViewModel: AuthViewModel) {
    val searchQuery = remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    val itemList = remember {
        listOf(
            InventoryItem("Item1", "$13.50", 30, true, "SKU12345", imageRes = R.drawable.itemdefault),
            InventoryItem("Item2", "$10.00", 100, true, "SKU67890"),
            InventoryItem("Item3", "$10.00", 100, true, "SKU67890"),
            InventoryItem("Item4", "$10.00", 100, true, "SKU67890"),
            InventoryItem("Item5", "$10.00", 100, true, "SKU67820")
        )
    }

    // state variables
    var showItemDetailsDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<InventoryItem?>(null) } // Can be null initially
    var showTopMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory", fontWeight = FontWeight.Medium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5F2E9) // Match background
                ),
                actions = {
                    IconButton(onClick = { showTopMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Inventory Menu")
                    }

                    DropdownMenu(
                        expanded = showTopMenu,
                        onDismissRequest = { showTopMenu = false },
                        modifier = Modifier.background(Color(0xFFE6E1D1))
                    ) {

                        // All previously bottom menu items moved here
                        DropdownMenuItem(
                            text = { Text("Account") },
                            onClick = { showTopMenu = false },
                            leadingIcon = { Icon(painterResource(id = R.drawable.account), "Account") }
                        )
                        DropdownMenuItem(
                            text = { Text("Export") },
                            onClick = { showTopMenu = false },
                            leadingIcon = { Icon(painterResource(id = R.drawable.export), "Export") }
                        )
                        DropdownMenuItem(
                            text = { Text("Reports") },
                            onClick = {
                                showTopMenu = false
                                navController.navigate("reports")
                            },
                            leadingIcon = { Icon(painterResource(id = R.drawable.reports), "Reports") }
                        )
                        DropdownMenuItem(
                            text = { Text("Dark Mode") },
                            onClick = { showTopMenu = false },
                            leadingIcon = { Icon(painterResource(id = R.drawable.darkmode), "Dark Mode") }
                        )
                        DropdownMenuItem(
                            text = { Text("Sync") },
                            onClick = { showTopMenu = false },
                            leadingIcon = { Icon(painterResource(id = R.drawable.sync), "Sync") }
                        )

                        HorizontalDivider()

                        // Logout stays here at the bottom
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                showTopMenu = false
                                authViewModel.logout()
                            },
                            leadingIcon = { Icon(Icons.Default.ExitToApp, "Logout") }
                        )
                    }
                }

            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color(0xFFF3EEDF),
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomBarButton(Icons.Default.Edit, "Edit Item")
                    BottomBarButton(Icons.Default.Add, "Add Item")
                    BottomBarButton(Icons.Default.Delete, "Delete Item")
                }
            }

        },
        containerColor = Color(0xFFF5F2E9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F2E9))
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                placeholder = { Text("Item Name / SKU Id") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F6F1))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // This should be removed or changed
                verticalAlignment = Alignment.CenterVertically // Add for better alignment
            ) {
                // Spacer for Thumbnail position (approximating 40.dp size)
                Spacer(modifier = Modifier.width(48.dp)) // 40.dp Icon + 8.dp padding/margin

                Text(
                    text = "List item",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f).padding(start = 8.dp) // Match item padding
                )
                Text(
                    text = "Price",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Qt.",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(0.5f)
                )
                // Spacer for Status Icon position (approximating icon size)
                Spacer(modifier = Modifier.width(24.dp)) // Adjust this value to align with the Status Check Icon
            }

            HorizontalDivider(
                color = Color.LightGray,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Item list
            LazyColumn {
                items(itemList) { item ->
                    InventoryItemRow(
                        item = item,
                        onItemClick = { clickedItem ->
                            selectedItem = clickedItem
                            showItemDetailsDialog = true
                        }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        }
    }

    if (showItemDetailsDialog && selectedItem != null) {
        ItemDetailsDialog(
            item = selectedItem!!,
            onDismiss = {
                showItemDetailsDialog = false
                selectedItem = null // Clear the selection
            }
        )
    }
}

@Composable
fun InventoryItemRow(item: InventoryItem, onItemClick: (InventoryItem) -> Unit) {
    Row(
    // Apply the clickable modifier here
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(item) } // Pass the current item back on click
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Thumbnail
        Icon(
            painterResource(id = R.drawable.itemdefault),
            contentDescription = "Item thumbnail",
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFE8E4D9))
                .padding(8.dp),
            tint = Color.Gray
        )
        // Add a small spacer to match the total header spacer of 48.dp (40.dp icon + 8.dp spacer)
        Spacer(modifier = Modifier.width(8.dp))

        // 2. Item Name (Weight 1f)
        Text(
            item.name,
            modifier = Modifier.weight(1f)
        )

        // 3. Price (Weight 1f)
        Text(
            item.price,
            modifier = Modifier.weight(1f)
        )

        // 4. Quantity (Weight 0.5f)
        Text(
            item.quantity.toString(),
            modifier = Modifier.weight(0.5f)
        )

        // 5. Status Icon (Fixed width to match the header's final Spacer)
        Box(
            modifier = Modifier.width(24.dp), // Use the same width as the header's Status Spacer
            contentAlignment = Alignment.Center // Center the icon within the box
        ) {
            if (item.inStock) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "In stock",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp) // Set a size for the icon
                )
            }
        }
    }
}

@Composable
fun RowScope.BottomBarButton(icon: ImageVector, label: String, onClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .weight(1f)
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, tint = Color.Black)
        Text(label, fontSize = 11.sp)
    }
}
@Composable
fun RowScope.BottomBarButton(icon: Painter, label: String, onClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .weight(1f)
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, tint = Color.Black)
        Text(label, fontSize = 11.sp)
    }
}

@Composable
fun ItemDetailsDialog(
    item: InventoryItem,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Large image
                item.imageRes?.let { resId ->
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = item.name,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Brief overview text
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Quantity: ${item.quantity}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "${item.SKU}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Price: ${item.price}",
                    style = MaterialTheme.typography.bodyMedium
                )

                item.description?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    )
}