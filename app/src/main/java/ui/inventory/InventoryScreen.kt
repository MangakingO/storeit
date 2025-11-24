package com.example.storeit.ui.inventory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.storeit.R
import com.example.storeit.data.model.InventoryItem
import com.example.storeit.ui.auth.AuthViewModel
import com.example.storeit.ui.components.ItemDetailsDialog
import com.example.storeit.ui.components.ItemEditorDialog
import com.example.storeit.utils.ConnectionState
import com.example.storeit.utils.currentConnectionState
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

    var selectedItems by remember { mutableStateOf<List<InventoryItem>>(emptyList()) }
    val uiState by inventoryViewModel.uiState.collectAsState(initial = InventoryUiState())
    val filteredItems = uiState.items.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                (it.sku?.contains(searchQuery, ignoreCase = true) ?: false)
    }

    var itemForDetails by remember { mutableStateOf<InventoryItem?>(null) }
    var showItemDetailsDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val connection by currentConnectionState()
    val isOffline = connection === ConnectionState.Unavailable

    val itemsToReorder = uiState.items.filter { it.needsReorder }

    // Open editor dialog
    val editorState = uiState.editorState
    val isEditorOpen = uiState.isEditorOpen
    val editorError = uiState.editorError

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Inventory", fontWeight = FontWeight.Medium) },
                actions = {
                    IconButton(onClick = { showTopMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Inventory Menu")
                    }
                    DropdownMenu(
                        expanded = showTopMenu,
                        onDismissRequest = { showTopMenu = false },
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
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null) }
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar() {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomBarButton(Icons.Default.Add, "Add Item") {
                        inventoryViewModel.openNewItem()
                    }
                    BottomBarButton(
                        icon = Icons.Default.Edit,
                        label = "Edit Item",
                        enabled = selectedItems.size == 1
                    ) {
                        selectedItems.firstOrNull()?.let { inventoryViewModel.editItem(it) }
                    }
                    BottomBarButton(
                        icon = Icons.Default.Delete,
                        label = "Delete Item",
                        enabled = selectedItems.isNotEmpty()
                    ) {
                        showDeleteConfirmationDialog = true
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            if (itemsToReorder.isNotEmpty()) {
                ReorderAlert(items = itemsToReorder)
            }

            AnimatedVisibility(
                visible = isOffline,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                OfflineWarning()
            }

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
                Text("Price", fontWeight = FontWeight.SemiBold,modifier = Modifier.weight(0.7f))
                Text("Qt.", fontWeight = FontWeight.SemiBold,modifier = Modifier.weight(0.32f))
                Text("Status", fontWeight = FontWeight.SemiBold, maxLines = 1)
            }

            LazyColumn (
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ){
                items(filteredItems) { item ->
                    InventoryItemRow(
                        item = item,
                        selected = item in selectedItems,
                        onItemClick = { clickedItem ->
                            selectedItems = if (clickedItem in selectedItems) {
                                selectedItems - clickedItem
                            } else {
                                selectedItems + clickedItem
                            }
                        },
                        onImageClick = {
                            itemForDetails = it
                            showItemDetailsDialog = true
                        }
                    )
                }
                item {
                    HorizontalDivider()
                }
            }
        }
    }

    // Item Details Dialog
    if (showItemDetailsDialog && itemForDetails != null) {
        ItemDetailsDialog(
            item = itemForDetails!!,
            onDismiss = { showItemDetailsDialog = false }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmationDialog) {
        DeleteItemsConfirmationDialog(
            itemsToDelete = selectedItems,
            onConfirm = {
                selectedItems.forEach { item ->
                    inventoryViewModel.deleteItem(userId, item)
                }
                selectedItems = emptyList()
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
            onReorderPointChanged = { inventoryViewModel.updateEditorReorderPoint(it) },
            onSave = { inventoryViewModel.saveEditor(userId) }
        )
    }
}

@Composable
fun OfflineWarning() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "You're offline. Edits will be saved locally.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun ReorderAlert(items: List<InventoryItem>) {
    val message = if (items.size == 1) {
        "${items.first().name} is low on stock."
    } else {
        "Multiple items are low on stock."
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(16.dp)
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DeleteItemsConfirmationDialog(
    itemsToDelete: List<InventoryItem>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val title = if (itemsToDelete.size == 1) "Delete Item" else "Delete Items"
    val text = if (itemsToDelete.size == 1) {
        "Are you sure you want to delete this item?"
    } else {
        "Are you sure you want to delete ${itemsToDelete.size} items?"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
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
    onImageClick: (InventoryItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(item) }
            .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp)
            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMedium)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (item.imageRes != null) {
            Image(
                painter = painterResource(id = item.imageRes!!),
                contentDescription = "Item thumbnail",
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onImageClick(item) }
            )
        } else {
            Icon(
                painterResource(id = R.drawable.itemdefault),
                contentDescription = "Item thumbnail",
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onImageClick(item) },
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(item.name, modifier = Modifier.weight(1f), overflow = TextOverflow.Ellipsis, maxLines = 1)
        Text(item.price, modifier = Modifier.weight(0.7f), maxLines = 1)
        Text(item.quantity.toString(), modifier = Modifier.weight(0.32f), maxLines = 1)
        Box(modifier = Modifier.padding(start = 8.dp), contentAlignment = Alignment.Center) {
            if (item.hasPendingWrites) {
                Icon(
                    Icons.Default.Sync, "Syncing",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    Icons.Default.Check, "Synced",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun RowScope.BottomBarButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(enabled = enabled, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, tint = color)
        Text(label, fontSize = 11.sp, color = color)
    }
}
