package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.auth.AuthManager
import com.example.data.*
import com.example.data.repository.FirestoreRepository
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchScreen(
    animeId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repo = remember { FirestoreRepository(context) }
    val currentUser by AuthManager.currentUser.collectAsState()

    val animeList by repo.observeAnimeList().collectAsState(initial = emptyList())
    val currentAnime = animeList.find { it.id == animeId }
        ?: initialSampleAnime.find { it.id == animeId }
        ?: initialSampleAnime.first()

    val firestoreEpisodes by repo.observeEpisodes(currentAnime.id).collectAsState(initial = emptyList())
    val episodes = remember(firestoreEpisodes, currentAnime) {
        if (firestoreEpisodes.isNotEmpty()) firestoreEpisodes
        else listOf(
            EpisodeItem(
                id = "${currentAnime.id}-ep-1",
                episodeNumber = 1,
                title = "Episode 1: Permulaan",
                streamSources = listOf(StreamSource("HD", "https://upbolt.to/e/u2jey90afkvg")),
                downloadLinks = listOf(
                    DownloadLink("1080p", "VIKING", "https://vikingfile.com/f/KyiS4sIbps"),
                    DownloadLink("720p", "FILEQ", "https://fileq.io/d/example720"),
                    DownloadLink("480p", "GDRIVE", "https://drive.google.com/uc?id=example480")
                )
            ),
            EpisodeItem(
                id = "${currentAnime.id}-ep-2",
                episodeNumber = 2,
                title = "Episode 2: Babak Selanjutnya",
                streamSources = listOf(StreamSource("HD", "https://upbolt.to/e/u2jey90afkvg")),
                downloadLinks = listOf(
                    DownloadLink("1080p", "VIKING", "https://vikingfile.com/f/KyiS4sIbps"),
                    DownloadLink("720p", "FILEQ", "https://fileq.io/d/example720")
                )
            )
        )
    }

    var selectedEpisodeNumber by remember { mutableIntStateOf(1) }
    val currentEpisode = episodes.find { it.episodeNumber == selectedEpisodeNumber } ?: episodes.firstOrNull()

    var isFavorite by remember { mutableStateOf(false) }
    var isDownloadExpanded by remember { mutableStateOf(true) }
    var isPlayerLoading by remember { mutableStateOf(true) }

    // Listen to user favorite status
    val userFavorites by repo.observeFavorites(currentUser?.uid ?: "").collectAsState(initial = emptyList())
    LaunchedEffect(userFavorites, currentAnime) {
        isFavorite = userFavorites.any { it.animeId == currentAnime.id }
    }

    val activeStreamUrl = currentEpisode?.streamSources?.firstOrNull()?.url
        ?: "https://upbolt.to/e/u2jey90afkvg"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentAnime.title,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (currentUser == null) {
                            Toast.makeText(context, "Silakan login di tab Profil untuk simpan favorit", Toast.LENGTH_SHORT).show()
                        } else {
                            coroutineScope.launch {
                                val result = repo.toggleFavorite(currentUser!!.uid, currentAnime)
                                result.onSuccess { added ->
                                    isFavorite = added
                                    val msg = if (added) "Ditambahkan ke Favorit" else "Dihapus dari Favorit"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }.onFailure {
                                    Toast.makeText(context, "Gagal mengupdate favorit: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Favorit",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Video Player Container (Aspect Ratio 16:9)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            webChromeClient = WebChromeClient()
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    isPlayerLoading = false
                                }
                            }
                            loadUrl(activeStreamUrl)
                        }
                    },
                    update = { webView ->
                        if (webView.url != activeStreamUrl) {
                            isPlayerLoading = true
                            webView.loadUrl(activeStreamUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (isPlayerLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // 2. Info Episode & Navigasi Antar Episode
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${currentAnime.title} - Episode $selectedEpisodeNumber",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (currentEpisode?.title?.isNotBlank() == true) {
                    Text(
                        text = currentEpisode.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Episode Quick Navigation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val prevEpisode = episodes.find { it.episodeNumber == selectedEpisodeNumber - 1 }
                    val nextEpisode = episodes.find { it.episodeNumber == selectedEpisodeNumber + 1 }

                    OutlinedButton(
                        onClick = { if (prevEpisode != null) selectedEpisodeNumber = prevEpisode.episodeNumber },
                        enabled = prevEpisode != null,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Prev", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { /* Scroll to list */ },
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eps $selectedEpisodeNumber/${episodes.size.coerceAtLeast(currentAnime.totalEpisodes)}", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { if (nextEpisode != null) selectedEpisodeNumber = nextEpisode.episodeNumber },
                        enabled = nextEpisode != null,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Next", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Grid Episode Buttons (Interactive 1, 2, 3...)
                Text(
                    text = "Daftar Episode",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                val totalCount = episodes.size.coerceAtLeast(currentAnime.totalEpisodes.coerceAtLeast(12))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(totalCount) { index ->
                        val epNum = index + 1
                        val isSelected = epNum == selectedEpisodeNumber
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .width(52.dp)
                                .height(44.dp)
                                .clickable { selectedEpisodeNumber = epNum }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$epNum",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Download Accordion Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isDownloadExpanded = !isDownloadExpanded }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Download Episode $selectedEpisodeNumber",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (isDownloadExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = Color.LightGray
                            )
                        }

                        AnimatedVisibility(visible = isDownloadExpanded) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                val downloads = currentEpisode?.downloadLinks?.ifEmpty { null } ?: listOf(
                                    DownloadLink("1080p", "VIKING", "https://vikingfile.com/f/KyiS4sIbps"),
                                    DownloadLink("720p", "FILEQ", "https://fileq.io/d/example720"),
                                    DownloadLink("480p", "GDRIVE", "https://drive.google.com/uc?id=example480")
                                )

                                val grouped = downloads.groupBy { it.quality }
                                grouped.forEach { (quality, links) ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                            contentColor = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            Text(quality, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            links.forEach { link ->
                                                FilledTonalButton(
                                                    onClick = {
                                                        try {
                                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                                                            context.startActivity(intent)
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "URL tidak valid: ${link.url}", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(32.dp),
                                                    colors = ButtonDefaults.filledTonalButtonColors(
                                                        containerColor = Color(0xFF631010)
                                                    )
                                                ) {
                                                    Text(link.host, fontSize = 11.sp, color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 5. Sinopsis & Genre
                Text(
                    text = "Sinopsis",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = currentAnime.synopsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Genre Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    currentAnime.genres.forEach { genre ->
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = genre,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
