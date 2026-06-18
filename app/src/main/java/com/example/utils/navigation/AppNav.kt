package com.example.utils.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screen.DashboardScreen
import com.example.ui.viewmodel.AppViewModel

@Composable
fun AppNav(viewModel: AppViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            DashboardScreen(viewModel = viewModel, onNavigateToBooking = {
                navController.navigate("booking")
            })
        }
        // Add more routes here
    }
}
