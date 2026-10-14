package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.mockAnimeList

@Composable
fun BookmarkScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Favorit Anda", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
            items(mockAnimeList.take(2)) { anime ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(text = anime.title, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
