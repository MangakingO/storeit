package com.example.storeit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.storeit.ui.auth.AuthViewModel
import com.example.storeit.ui.auth.AuthScreen
import com.example.storeit.ui.inventory.InventoryScreen
import com.example.storeit.ui.inventory.InventoryViewModel
import com.example.storeit.data.InventoryRepository
import com.example.storeit.ui.theme.StoreitTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.persistentCacheSettings

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

        setContent {
            StoreitTheme {
                val navController = rememberNavController()

                // Pick start destination based on authentication
                val startRoute = if (auth.currentUser != null) "inventory" else "login"

                NavHost(
                    navController = navController,
                    startDestination = startRoute
                ) {

                    // --------------------------
                    // LOGIN SCREEN
                    // --------------------------
                    composable("login") {

                        val authViewModel: AuthViewModel = viewModel(
                            factory = AuthViewModel.Factory(auth)
                        )

                        val state = authViewModel.uiState

                        // If logged in, go to inventory
                        LaunchedEffect(state.currentUser) {
                            if (state.currentUser != null) {
                                navController.navigate("inventory") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        }

                        AuthScreen(
                            state = state,
                            onEmailChanged = authViewModel::onEmailChanged,
                            onPasswordChanged = authViewModel::onPasswordChanged,
                            onConfirmPasswordChanged = authViewModel::onConfirmPasswordChanged,
                            onToggleMode = authViewModel::toggleMode,
                            onSubmit = { authViewModel.submitCredentials() }
                        )
                    }

                    // --------------------------
                    // INVENTORY SCREEN
                    // --------------------------
                    composable("inventory") {

                        val authViewModel: AuthViewModel = viewModel(
                            factory = AuthViewModel.Factory(auth)
                        )
                        val authState = authViewModel.uiState

                        LaunchedEffect(authState.currentUser) {
                            if (authState.currentUser == null) {
                                navController.navigate("login") {
                                    popUpTo("inventory") { inclusive = true }
                                }
                            }
                        }

                        authState.currentUser?.uid?.let { userId ->
                            val inventoryViewModel: InventoryViewModel = viewModel(
                                factory = InventoryViewModel.Factory(inventoryRepository)
                            )


                            LaunchedEffect(userId) {
                                inventoryViewModel.startListening(userId)
                            }

                            DisposableEffect(inventoryViewModel) {
                                onDispose {
                                    inventoryViewModel.stopListening()
                                }
                            }

                            InventoryScreen(
                                navController = navController,
                                authViewModel = authViewModel,
                                inventoryViewModel = inventoryViewModel,
                                userId = userId
                            )
                        }
                    }

                    // --------------------------
                    // REPORTS SCREEN
                    // --------------------------
                    composable("reports") {
                        Reports(navController = navController)
                    }
                }
            }
        }
    }
}
