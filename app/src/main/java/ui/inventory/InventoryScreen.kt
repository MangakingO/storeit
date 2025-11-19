package com.example.storeit.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import com.example.storeit.R
import com.example.storeit.data.model.InventoryItem
import com.example.storeit.ui.auth.AuthViewModel
import com.example.storeit.ui.components.ItemEditorDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    inventoryViewModel: InventoryViewModel,
    userId: String
) {
    var searchQuery by remember { mutableStateOf("") }
    var showTopMenu by remember { mutableStateOf(false) }

    var selectedItem by remember { mutableStateOf<InventoryItem?>(null) }
    val uiState by inventoryViewModel.uiState.collectAsState(initial = InventoryUiState())
    val filteredItems = uiState.items.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                (it.sku?.contains(searchQuery, ignoreCase = true) ?: false)
    }

    var showItemDetailsDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var showCheckmark by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Open editor dialog
    val editorState = uiState.editorState
    val isEditorOpen = uiState.isEditorOpen
    val editorError = uiState.editorError

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Inventory", fontWeight = FontWeight.Medium) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F2E9)),
                actions = {
                    IconButton(onClick = { showTopMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Inventory Menu")
                    }
                    DropdownMenu(
                        expanded = showTopMenu,
                        onDismissRequest = { showTopMenu = false },
                        modifier = Modifier.background(Color(0xFFE6E1D1))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Reports") },
                            onClick = {
                                showTopMenu = false
                                navController.navigate("reports")
                            },
                            leadingIcon = { Icon(painterResource(id = R.drawable.reports), null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Sync") },
                            onClick = {
                                showTopMenu = false
                                showCheckmark = true
                                scope.launch {
                                    snackbarHostState.showSnackbar("Sync successful")
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.Sync, "Sync") }
                        )
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                showTopMenu = false
                                authViewModel.logout()
                            },
                            leadingIcon = { Icon(Icons.Default.ExitToApp, null) }
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = Color(0xFFF3EEDF), tonalElevation = 4.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomBarButton(Icons.Default.Add, "Add Item") {
                        inventoryViewModel.openNewItem()
                    }
                    BottomBarButton(Icons.Default.Edit, "Edit Item") {
                        selectedItem?.let { inventoryViewModel.editItem(it) }
                    }
                    BottomBarButton(Icons.Default.Delete, "Delete Item") {
                        if (selectedItem != null) {
                            showDeleteConfirmationDialog = true
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF5F2E9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name or SKU") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("List item", fontWeight = FontWeight.SemiBold,modifier = Modifier.weight(1f))
                Text("Price", fontWeight = FontWeight.SemiBold,modifier = Modifier.weight(1f))
                Text("Qt.", fontWeight = FontWeight.SemiBold,modifier = Modifier.weight(0.5f))
                Text("Status", fontWeight = FontWeight.SemiBold,modifier = Modifier.weight(0.3f))
            }

            LazyColumn (
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ){
                items(filteredItems) { item ->
                    InventoryItemRow(
                        item = item,
                        selected = selectedItem == item,
                        onItemClick = { clickedItem ->
                            selectedItem = clickedItem
                            showItemDetailsDialog = true
                        },
                        showCheckmark = showCheckmark
                    )
                }
                item {
                    HorizontalDivider(color = Color.Gray, thickness = 1.dp)
                }
            }
        }
    }

    // Item Details Dialog
    if (showItemDetailsDialog && selectedItem != null) {
        ItemDetailsDialog(
            item = selectedItem!!,
            onDismiss = { showItemDetailsDialog = false }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmationDialog) {
        DeleteItemConfirmationDialog(
            onConfirm = {
                selectedItem?.let {
                    inventoryViewModel.deleteItem(userId, it)
                    selectedItem = null
                }
                showDeleteConfirmationDialog = false
            },
            onDismiss = { showDeleteConfirmationDialog = false }
        )
    }

    // Editor Dialog (Add/Edit)
    if (isEditorOpen) {
        ItemEditorDialog(
            editorState = editorState,
            isSaving = uiState.isSaving,
            errorMessage = editorError,
            onDismiss = { inventoryViewModel.dismissEditor() },
            onNameChanged = { inventoryViewModel.updateEditorName(it) },
            onQuantityChanged = { inventoryViewModel.updateEditorQuantity(it) },
            onDescriptionChanged = { inventoryViewModel.updateEditorDescription(it) },
            onPriceChanged = { inventoryViewModel.updateEditorPrice(it) },
            onSkuChanged = { inventoryViewModel.updateEditorSku(it) },
            onSave = { inventoryViewModel.saveEditor(userId) }
        )
    }
}

@Composable
fun DeleteItemConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Item") },
        text = { Text("Are you sure you want to delete this item?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun InventoryItemRow(
    item: InventoryItem,
    selected: Boolean,
    onItemClick: (InventoryItem) -> Unit,
    showCheckmark: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(item) }
            .background(if (selected) Color(0xFFDDEBF7) else Color.Transparent)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(id = R.drawable.itemdefault),
            contentDescription = "Item thumbnail",
            modifier = Modifier.size(40.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(item.name, modifier = Modifier.weight(1f))
        Text(item.price, modifier = Modifier.weight(1f))
        Text(item.quantity.toString(), modifier = Modifier.weight(0.5f))
        Box (modifier = Modifier.weight(0.3f),
             contentAlignment = Alignment.Center) {
            if (item.inStock) {
                val icon = if (showCheckmark) Icons.Default.Check else Icons.Default.Sync
                Icon(icon, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun RowScope.BottomBarButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.weight(1f).clickable(onClick = onClick),
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
                    text = "${item.sku}",
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
