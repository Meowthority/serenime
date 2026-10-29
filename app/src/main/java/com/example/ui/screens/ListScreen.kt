package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.initialSampleAnime
import com.example.data.mockAnimeList
import com.example.data.repository.FirestoreRepository
import com.example.ui.components.AnimeCard

@Composable
fun ListScreen(
    onNavigateToWatch: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val repo = remember { FirestoreRepository(context) }

    val firestoreList by repo.observeAnimeList().collectAsState(initial = emptyList())
    val activeAnimeList = remember(firestoreList) {
        if (firestoreList.isNotEmpty()) firestoreList.map { it.toAnime() }
        else mockAnimeList
    }

    var searchQuery by remember { mutableStateOf("") }
    val filteredList = remember(searchQuery, activeAnimeList) {
        if (searchQuery.isBlank()) activeAnimeList
        else activeAnimeList.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.genres.any { g -> g.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Daftar Anime",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Jelajahi seluruh katalog anime lengkap dengan subtitle Indonesia.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari judul anime atau genre...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Cari", tint = Color.LightGray)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Hapus", tint = Color.LightGray)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredList.size} Judul Ditemukan",
                style = MaterialTheme.typography.labelMedium,
                color = Color.LightGray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tidak ada anime yang cocok dengan \"$searchQuery\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList) { anime ->
                    AnimeCard(
                        anime = anime,
                        isLoading = false,
                        onClick = { onNavigateToWatch(anime.id) }
                    )
                }
            }
        }
    }
}
