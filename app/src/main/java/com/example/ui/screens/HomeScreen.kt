package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.mockAnimeList
import com.example.ui.components.AnimeCard
import kotlinx.coroutines.delay

@Composable
fun HomeScreen() {
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000)
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Trending", style = MaterialTheme.typography.headlineSmall)
        LazyRow {
            items(5) {
                AnimeCard(if (isLoading) null else mockAnimeList.first(), isLoading = isLoading)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Terbaru", style = MaterialTheme.typography.headlineSmall)
        LazyVerticalGrid(
            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize()
        ) {
            items(6) {
                AnimeCard(if (isLoading) null else mockAnimeList.first(), isLoading = isLoading)
            }
        }
    }
}
