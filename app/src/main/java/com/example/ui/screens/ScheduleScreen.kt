package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScheduleScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Jadwal Rilis", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
            items(7) { day ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(text = "Hari ke-${day + 1}: Anime rilis...", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
