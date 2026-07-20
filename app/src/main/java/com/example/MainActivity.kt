package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.RationViewModel
import com.example.ui.screens.AdminDashboard
import com.example.ui.screens.CitizenDashboard
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ShopkeeperDashboard
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Instantiate our shared ViewModel
        val viewModel = ViewModelProvider(this)[RationViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "login"
                    ) {
                        composable("login") {
                            LoginScreen(
                                viewModel = viewModel,
                                onLoginSuccess = {
                                    val user = viewModel.currentUser.value
                                    if (user != null) {
                                        when (user.role) {
                                            "Citizen" -> navController.navigate("citizen") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                            "Shopkeeper" -> navController.navigate("shopkeeper") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                            "Admin" -> navController.navigate("admin") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
                                    }
                                }
                            )
                        }

                        composable("citizen") {
                            CitizenDashboard(
                                viewModel = viewModel,
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo("citizen") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("shopkeeper") {
                            ShopkeeperDashboard(
                                viewModel = viewModel,
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo("shopkeeper") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("admin") {
                            AdminDashboard(
                                viewModel = viewModel,
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo("admin") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
