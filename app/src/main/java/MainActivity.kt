package com.example.storeit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.storeit.ui.auth.AuthViewModel
import com.example.storeit.ui.auth.AuthScreen
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.remember

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()

        setContent {
            val navController = rememberNavController()

            val startRoute = remember {
                if (auth.currentUser != null) {
                    "inventory" // User is already logged in
                } else {
                    "login"     // User needs to log in
                }
            }

            NavHost(navController = navController, startDestination = startRoute) {
                composable("login") {
                    val authViewModel: AuthViewModel = viewModel(
                        factory = AuthViewModel.Factory(auth)
                    )

                    val state = authViewModel.uiState // Get the current state for observation


                    // 🎯 1. LAUNCHED EFFECT for Navigation 🎯
                    // This block runs whenever 'state.currentUser' changes.
                    // The NavController is passed as 'key' to avoid capturing a stale reference.
                    LaunchedEffect(state.currentUser, navController) {
                        // Only navigate if the currentUser is NOT null (authentication succeeded)
                        if (state.currentUser != null) {
                            navController.navigate("inventory") {
                                // Clear the back stack so pressing back exits the app
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
                        onSubmit = {
                            authViewModel.submitCredentials()
                        }
                    )
                }
                composable("inventory") {
                    // 1. Get the AuthViewModel (scoped to the activity/navController)
                    val auth = FirebaseAuth.getInstance()
                    val authViewModel: AuthViewModel = viewModel(
                        factory = AuthViewModel.Factory(auth)
                    )
                    val state = authViewModel.uiState

                    // 2. LAUNCHED EFFECT for Logout Navigation
                    LaunchedEffect(state.currentUser, navController) {
                        // Only navigate if the currentUser IS null (logout succeeded)
                        if (state.currentUser == null) {
                            navController.navigate("login") {
                                // Clear the back stack so pressing back exits the app from login
                                popUpTo("inventory") { inclusive = true }
                            }
                        }
                    }

                    // 3. Call the InventoryScreen, passing the NavController and ViewModel
                    InventoryScreen(
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }

                composable("reports") {
                    Reports(navController = navController)
                }
            }
        }
    }
}