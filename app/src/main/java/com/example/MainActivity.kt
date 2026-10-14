package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.AdblockDetector
import com.example.ui.components.SerenimeBottomNavBar
import com.example.ui.theme.SerenimeTheme
import com.example.ui.screens.*

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      SerenimeTheme {
        val navController = rememberNavController()
        var currentRoute by remember { mutableStateOf("home") }

        Scaffold(
          bottomBar = { SerenimeBottomNavBar(currentRoute) { route -> 
            currentRoute = route
            navController.navigate(route) 
          } }
        ) { innerPadding ->
          Box(modifier = Modifier.padding(innerPadding)) {
            AdblockDetector {}
            NavHost(navController = navController, startDestination = "home") {
              composable("home") { HomeScreen() }
              composable("list") { ListScreen() }
              composable("schedule") { ScheduleScreen() }
              composable("bookmark") { BookmarkScreen() }
              composable("profile") { ProfileScreen() }
            }
          }
        }
      }
    }
  }
}
