package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(val title: String, val icon: ImageVector, val route: String)

@Composable
fun SerenimeBottomNavBar(currentRoute: String, onNavigate: (String) -> Unit) {
  val items = listOf(
    NavItem("Beranda", Icons.Default.Home, "home"),
    NavItem("Daftar", Icons.Default.GridView, "list"),
    NavItem("Jadwal", Icons.Default.CalendarMonth, "schedule"),
    NavItem("Favorit", Icons.Default.Bookmark, "bookmark"),
    NavItem("Profil", Icons.Default.Person, "profile")
  )

  NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
    items.forEach { item ->
      NavigationBarItem(
        selected = currentRoute == item.route,
        onClick = { onNavigate(item.route) },
        label = { Text(item.title) },
        icon = { Icon(item.icon, contentDescription = item.title) }
      )
    }
  }
}
