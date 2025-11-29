package com.example.storeit.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.storeit.ui.theme.StoreitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventorySelectionScreen(
    navController: NavController,
    viewModel: InventorySelectionViewModel,
    userId: String,
    onInventorySelected: (InventoryInfo) -> Unit,
    onCreateOrJoinClicked: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadInventories(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Inventories") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    if (uiState.inventories.isEmpty()) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "You are not a member of any inventory yet.",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(uiState.inventories) { inventory ->
                                InventoryCard(
                                    inventory = inventory,
                                    onClick = { onInventorySelected(inventory) }
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    Button(
                        onClick = onCreateOrJoinClicked,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Create or Join Inventory")
                    }

                    uiState.error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryCard(inventory: InventoryInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = inventory.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(inventory.role, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InventorySelectionScreenPreview() {
    StoreitTheme {
        val navController = rememberNavController()
        val sampleInventories = listOf(
            InventoryInfo("1", "Home Inventory", "Owner"),
            InventoryInfo("2", "Work Stock", "Admin"),
            InventoryInfo("3", "Project Supplies", "Employee")
        )
//        InventorySelectionScreen(
//            navController = navController,
//           inventories = sampleInventories,
//            onInventorySelected = {},
//           onCreateOrJoinClicked = {}
//       )
    }
}

@Preview(showBackground = true)
@Composable
fun InventorySelectionScreenEmptyPreview() {
    StoreitTheme {
        val navController = rememberNavController()
//        InventorySelectionScreen(
//            navController = navController,
//            inventories = emptyList(),
//            onInventorySelected = {},
//            onCreateOrJoinClicked = {}
//        )
    }
}
