package com.example.storeit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.storeit.data.InventoryRepository
import com.example.storeit.data.UserRepository
import com.example.storeit.ui.Reports
import com.example.storeit.ui.ReportsViewModel
import com.example.storeit.ui.auth.AuthScreen
import com.example.storeit.ui.auth.AuthViewModel
import com.example.storeit.ui.inventory.CreateOrJoinScreen
import com.example.storeit.ui.inventory.CreateOrJoinViewModel
import com.example.storeit.ui.inventory.InventoryScreen
import com.example.storeit.ui.inventory.InventorySelectionScreen
import com.example.storeit.ui.inventory.InventorySelectionViewModel
import com.example.storeit.ui.inventory.InventoryViewModel
import com.example.storeit.ui.theme.StoreitTheme
import com.example.storeit.utils.ThemeManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.persistentCacheSettings
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()

        // --- ENABLE OFFLINE PERSISTENCE ---
        val firestore = FirebaseFirestore.getInstance().apply {
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(persistentCacheSettings { })
                .build()
        }

        val inventoryRepository = InventoryRepository(firestore)
        val userRepository = UserRepository(firestore)

        setContent {
            val context = LocalContext.current
            val themeManager = remember { ThemeManager(context) }
            val isDarkTheme by themeManager.isDarkTheme.collectAsState(initial = false)
            val scope = rememberCoroutineScope()

            StoreitTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(auth, userRepository))
                val authState by authViewModel.uiState.collectAsState()

                // Main navigation logic
                NavHost(
                    navController = navController,
                    startDestination = if (auth.currentUser == null) "login" else "inventory_selection"
                ) {
                    composable("login") {
                        LaunchedEffect(authState) {
                            if (authState.currentUser != null) {
                                navController.navigate("inventory_selection") { popUpTo("login") { inclusive = true } }
                            }
                        }

                        AuthScreen(
                            state = authState,
                            onEmailChanged = authViewModel::onEmailChanged,
                            onPasswordChanged = authViewModel::onPasswordChanged,
                            onConfirmPasswordChanged = authViewModel::onConfirmPasswordChanged,
                            onToggleMode = authViewModel::toggleMode,
                            onSubmit = { authViewModel.submitCredentials() }
                        )
                    }

                    composable("inventory_selection") {
                        LaunchedEffect(authState.inventoryId) {
                            if (authState.inventoryId != null) {
                                navController.navigate("inventory") { popUpTo("inventory_selection") { inclusive = true } }
                            }
                        }

                        val selectionViewModel: InventorySelectionViewModel = viewModel(factory = InventorySelectionViewModel.Factory(userRepository, inventoryRepository))
                        InventorySelectionScreen(
                            navController = navController,
                            viewModel = selectionViewModel,
                            userId = authState.currentUser!!.uid,
                            onInventorySelected = { inventoryInfo ->
                                authViewModel.selectInventory(inventoryInfo.id, inventoryInfo.name, inventoryInfo.role)
                            },
                            onCreateOrJoinClicked = {
                                navController.navigate("create_or_join")
                            },
                            onLogout = {
                                authViewModel.logout()
                            }
                        )
                    }

                    composable("create_or_join") {
                        val createOrJoinViewModel: CreateOrJoinViewModel = viewModel(factory = CreateOrJoinViewModel.Factory(firestore))
                        authState.currentUser?.uid?.let {
                            CreateOrJoinScreen(createOrJoinViewModel, it, onLogout = { authViewModel.logout() })
                        } ?: LaunchedEffect(Unit) {
                            navController.navigate("login") { popUpTo("create_or_join") { inclusive = true } }
                        }
                    }

                    composable("inventory") {
                        val inventoryId = authState.inventoryId
                        if (inventoryId == null) {
                            authViewModel.clearInventorySelection()
                            navController.navigate("inventory_selection") { popUpTo("inventory") { inclusive = true } }
                            return@composable
                        }

                        LaunchedEffect(authState.currentUser) {
                            if (authState.currentUser == null) {
                                navController.navigate("login") { popUpTo("inventory") { inclusive = true } }
                            }
                        }

                        val inventoryViewModel: InventoryViewModel = viewModel(
                            factory = InventoryViewModel.Factory(inventoryRepository)
                        )

                        LaunchedEffect(inventoryId) {
                            inventoryViewModel.startListening(inventoryId)
                        }

                        DisposableEffect(inventoryViewModel) {
                            onDispose { inventoryViewModel.stopListening() }
                        }

                        InventoryScreen(
                            navController = navController,
                            authViewModel = authViewModel,
                            inventoryViewModel = inventoryViewModel,
                            onToggleTheme = {
                                scope.launch {
                                    themeManager.setTheme(!isDarkTheme)
                                }
                            }
                        )
                    }

                    composable("reports") {
                        val inventoryId = authState.inventoryId
                        if (inventoryId == null) {
                            navController.popBackStack()
                            return@composable
                        }
                        val reportsViewModel: ReportsViewModel = viewModel(factory = ReportsViewModel.Factory(firestore))
                        Reports(navController = navController, inventoryId = inventoryId, reportsViewModel = reportsViewModel)
                    }
                }
            }
        }
    }
}
