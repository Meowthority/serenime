package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.initialSampleAnime
import com.example.data.mockAnimeList
import com.example.data.repository.FirestoreRepository

@Composable
fun ScheduleScreen(
    onNavigateToWatch: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val repo = remember { FirestoreRepository(context) }
    val firestoreList by repo.observeAnimeList().collectAsState(initial = emptyList())
    val activeAnimeList = remember(firestoreList) {
        if (firestoreList.isNotEmpty()) firestoreList.map { it.toAnime() }
        else mockAnimeList
    }

    val days = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
    var selectedDayIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Jadwal Rilis Anime",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Update episode baru tepat waktu setiap minggunya.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Days Filter Bar
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(days.size) { index ->
                val day = days[index]
                val isSelected = selectedDayIndex == index
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedDayIndex = index },
                    label = { Text(day, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Rilis Hari ${days[selectedDayIndex]}:",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Schedule list
        val dayAnime = activeAnimeList.filterIndexed { idx, _ -> idx % 7 == selectedDayIndex || activeAnimeList.size < 4 }
        val displayList = if (dayAnime.isNotEmpty()) dayAnime else activeAnimeList.take(2)

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(displayList) { anime ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onNavigateToWatch(anime.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(anime.imageUrl).crossfade(true).build(),
                            contentDescription = anime.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp, 84.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ) {
                                Text("Pukul 19:30 WIB", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(anime.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("${anime.rating}", fontSize = 11.sp, color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(anime.episode, fontSize = 11.sp, color = Color.LightGray)
                            }
                        }
                    }
                }
            }
        }
    }
}
