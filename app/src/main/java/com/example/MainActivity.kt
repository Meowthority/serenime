package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.AdblockDetector
import com.example.ui.components.SerenimeBottomNavBar
import com.example.ui.screens.*
import com.example.ui.theme.SerenimeTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      SerenimeTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination?.route ?: "home"

        val showBottomBar = !currentDestination.startsWith("watch") && currentDestination != "admin"

        Scaffold(
          bottomBar = {
            if (showBottomBar) {
              SerenimeBottomNavBar(currentDestination) { route ->
                if (currentDestination != route) {
                  navController.navigate(route) {
                    popUpTo("home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                  }
                }
              }
            }
          }
        ) { innerPadding ->
          Box(modifier = Modifier.padding(innerPadding)) {
            AdblockDetector {}
            NavHost(navController = navController, startDestination = "home") {
              composable("home") {
                HomeScreen(
                  onNavigateToWatch = { animeId ->
                    navController.navigate("watch/$animeId")
                  }
                )
              }
              composable("list") {
                ListScreen(
                  onNavigateToWatch = { animeId ->
                    navController.navigate("watch/$animeId")
                  }
                )
              }
              composable("schedule") {
                ScheduleScreen(
                  onNavigateToWatch = { animeId ->
                    navController.navigate("watch/$animeId")
                  }
                )
              }
              composable("bookmark") {
                BookmarkScreen(
                  onNavigateToWatch = { animeId ->
                    navController.navigate("watch/$animeId")
                  },
                  onNavigateToProfile = {
                    navController.navigate("profile")
                  }
                )
              }
              composable("profile") {
                ProfileScreen(
                  onOpenAdminPanel = {
                    navController.navigate("admin")
                  }
                )
              }
              composable("admin") {
                AdminPanelScreen(
                  onBack = {
                    navController.popBackStack()
                  }
                )
              }
              composable(
                route = "watch/{animeId}",
                arguments = listOf(navArgument("animeId") { type = NavType.StringType })
              ) { backStackEntry ->
                val animeId = backStackEntry.arguments?.getString("animeId") ?: ""
                WatchScreen(
                  animeId = animeId,
                  onBack = {
                    navController.popBackStack()
                  }
                )
              }
            }
          }
        }
      }
    }
  }
}
