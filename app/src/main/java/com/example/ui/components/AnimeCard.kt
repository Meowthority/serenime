package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.Anime

@Composable
fun AnimeCard(anime: Anime?, isLoading: Boolean = false) {
    if (isLoading) {
        Column(modifier = Modifier.width(140.dp).padding(8.dp)) {
            SkeletonLoader(modifier = Modifier.fillMaxWidth().height(200.dp))
            Spacer(modifier = Modifier.height(8.dp))
            SkeletonLoader(modifier = Modifier.fillMaxWidth().height(16.dp))
        }
    } else if (anime != null) {
        Card(modifier = Modifier.width(140.dp).padding(8.dp)) {
            Column {
                // Placeholder image
                Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(MaterialTheme.colorScheme.surfaceVariant))
                Text(text = anime.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(8.dp), maxLines = 1)
            }
        }
    }
}
