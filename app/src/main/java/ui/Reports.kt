package com.example.storeit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.storeit.data.model.ItemChange
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Reports(navController: NavController, inventoryId: String, reportsViewModel: ReportsViewModel = viewModel()) {
    val uiState by reportsViewModel.uiState.collectAsState()

    LaunchedEffect(inventoryId) {
        reportsViewModel.fetchItemChanges(inventoryId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(it)) {
                items(uiState.itemChanges) { itemChange ->
                    ItemChangeRow(itemChange)
                }
            }
        }
    }
}

@Composable
fun ItemChangeRow(itemChange: ItemChange) {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = itemChange.itemName, fontWeight = FontWeight.Bold)
            Text(text = "Change Type: ${itemChange.changeType}", style = MaterialTheme.typography.bodySmall)
            itemChange.timestamp?.let {
                Text(text = formatter.format(it), style = MaterialTheme.typography.bodySmall)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = "Old: ${itemChange.oldValue ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
            Text(text = "New: ${itemChange.newValue ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
