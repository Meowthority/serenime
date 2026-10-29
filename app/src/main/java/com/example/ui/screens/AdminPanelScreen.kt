package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.*
import com.example.data.repository.FirestoreRepository
import com.example.data.scraper.AnimeScraper
import com.example.data.scraper.ScrapedAnimeData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repo = remember { FirestoreRepository(context) }
    val animeList by repo.observeAnimeList().collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Dashboard", "Kelola Anime", "Episode", "Web Scraper")

    // State for Anime Form with Extended Categories
    var animeIdInput by remember { mutableStateOf("") }
    var titleInput by remember { mutableStateOf("") }
    var japaneseTitleInput by remember { mutableStateOf("") }
    var synopsisInput by remember { mutableStateOf("") }
    var posterUrlInput by remember { mutableStateOf("") }
    var ratingInput by remember { mutableStateOf("8.5") }
    var typeInput by remember { mutableStateOf("TV") }
    var seasonInput by remember { mutableStateOf("Fall 2026") }
    var studioInput by remember { mutableStateOf("Ufotable") }
    var statusInput by remember { mutableStateOf("Ongoing") }
    var genresInput by remember { mutableStateOf("Action, Fantasy") }
    var episodesCountInput by remember { mutableStateOf("12") }
    var isSavingAnime by remember { mutableStateOf(false) }

    // State for Episode Form
    var selectedAnimeForEpisode by remember { mutableStateOf<AnimeItem?>(null) }
    var episodeNumInput by remember { mutableStateOf("1") }
    var episodeTitleInput by remember { mutableStateOf("") }
    var streamUrlUpbolt by remember { mutableStateOf("https://upbolt.to/e/u2jey90afkvg") }
    var streamUrlStreamwish by remember { mutableStateOf("https://streamwish.to/e/example") }
    var download1080Input by remember { mutableStateOf("https://vikingfile.com/f/example1080") }
    var download720Input by remember { mutableStateOf("https://fileq.io/d/example720") }
    var download480Input by remember { mutableStateOf("https://drive.google.com/uc?id=example480") }
    var isSavingEpisode by remember { mutableStateOf(false) }

    // State for Scraper
    var scraperUrlInput by remember { mutableStateOf("https://otakudesu.cloud/anime/example") }
    var isScraping by remember { mutableStateOf(false) }
    var scrapedResult by remember { mutableStateOf<ScrapedAnimeData?>(null) }
    var batchCountInput by remember { mutableStateOf("12") }
    var batchBaseUrlInput by remember { mutableStateOf("https://upbolt.to/e/serenime-ep{ep}") }
    var isBatchGenerating by remember { mutableStateOf(false) }

    // State for Seeding
    var isSeeding by remember { mutableStateOf(false) }

    val allGenreOptions = listOf("Action", "Adventure", "Comedy", "Drama", "Ecchi", "Fantasy", "Isekai", "Mystery", "Romance", "Sci-Fi", "School", "Slice of Life", "Supernatural")
    val allTypeOptions = listOf("TV", "Movie", "OVA", "ONA", "Special")
    val allStatusOptions = listOf("Ongoing", "Completed")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Admin Panel",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text("PRO", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Header
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 12.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                }
            }

            when (selectedTab) {
                // 1. DASHBOARD & STATISTIK
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Statistik & Kontrol Serenime ID",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Icon(Icons.Default.Movie, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Total Anime", style = MaterialTheme.typography.labelMedium, color = Color.LightGray)
                                    Text(
                                        text = "${animeList.size}",
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF10B981))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Status Cloud", style = MaterialTheme.typography.labelMedium, color = Color.LightGray)
                                    Text(
                                        text = "Online",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Quick Seed Button
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF380808)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Inisialisasi Data Default",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Isi katalog awal 5 anime lengkap beserta episode dan link pemutar video langsung ke Firestore.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        isSeeding = true
                                        coroutineScope.launch {
                                            val result = repo.seedInitialData()
                                            isSeeding = false
                                            result.onSuccess { count ->
                                                Toast.makeText(context, "Berhasil mengunggah $count anime ke Firestore!", Toast.LENGTH_LONG).show()
                                            }.onFailure {
                                                Toast.makeText(context, "Gagal seed data: ${it.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    enabled = !isSeeding,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    if (isSeeding) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                                    } else {
                                        Text("Unggah Data Contoh ke Firestore")
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. KELOLA ANIME (KATEGORI LENGKAP)
                1 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        item {
                            Text(
                                text = if (animeIdInput.isBlank()) "Tambah Anime Baru" else "Edit Anime (${animeIdInput})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = titleInput,
                                onValueChange = { titleInput = it },
                                label = { Text("Judul Anime") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = japaneseTitleInput,
                                onValueChange = { japaneseTitleInput = it },
                                label = { Text("Judul Romaji / Jepang (Opsional)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = synopsisInput,
                                onValueChange = { synopsisInput = it },
                                label = { Text("Sinopsis Lengkap") },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = posterUrlInput,
                                onValueChange = { posterUrlInput = it },
                                label = { Text("URL Gambar Poster") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Format Type & Status
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Tipe Format:", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        allTypeOptions.take(3).forEach { t ->
                                            FilterChip(
                                                selected = typeInput == t,
                                                onClick = { typeInput = t },
                                                label = { Text(t, fontSize = 10.sp) }
                                            )
                                        }
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Status:", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        allStatusOptions.forEach { s ->
                                            FilterChip(
                                                selected = statusInput == s,
                                                onClick = { statusInput = s },
                                                label = { Text(s, fontSize = 10.sp) }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = studioInput,
                                    onValueChange = { studioInput = it },
                                    label = { Text("Studio Animasi") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = seasonInput,
                                    onValueChange = { seasonInput = it },
                                    label = { Text("Musim & Tahun") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = ratingInput,
                                    onValueChange = { ratingInput = it },
                                    label = { Text("Rating (0-10)") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = episodesCountInput,
                                    onValueChange = { episodesCountInput = it },
                                    label = { Text("Total Episode") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Pilih Genre Cepat:", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(allGenreOptions) { g ->
                                    val currentGenres = genresInput.split(",").map { it.trim() }
                                    val isSelected = g in currentGenres
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            val updated = if (isSelected) currentGenres - g else currentGenres + g
                                            genresInput = updated.filter { it.isNotBlank() }.joinToString(", ")
                                        },
                                        label = { Text(g, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        if (titleInput.isBlank()) {
                                            Toast.makeText(context, "Judul anime tidak boleh kosong", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        isSavingAnime = true
                                        val genresList = genresInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                        val newAnime = AnimeItem(
                                            id = if (animeIdInput.isNotBlank()) animeIdInput else titleInput.lowercase().replace(" ", "-").replace(Regex("[^a-z0-9-]"), ""),
                                            title = titleInput,
                                            japaneseTitle = japaneseTitleInput,
                                            synopsis = synopsisInput,
                                            posterUrl = if (posterUrlInput.isNotBlank()) posterUrlInput else "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600",
                                            rating = ratingInput.toDoubleOrNull() ?: 8.0,
                                            genres = genresList,
                                            status = statusInput,
                                            type = typeInput,
                                            season = seasonInput,
                                            studio = studioInput,
                                            totalEpisodes = episodesCountInput.toIntOrNull() ?: 12
                                        )

                                        coroutineScope.launch {
                                            val result = repo.addAnime(newAnime)
                                            isSavingAnime = false
                                            result.onSuccess {
                                                Toast.makeText(context, "Anime berhasil disimpan!", Toast.LENGTH_SHORT).show()
                                                titleInput = ""
                                                japaneseTitleInput = ""
                                                synopsisInput = ""
                                                posterUrlInput = ""
                                                animeIdInput = ""
                                            }.onFailure {
                                                Toast.makeText(context, "Gagal menyimpan: ${it.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    enabled = !isSavingAnime,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isSavingAnime) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                                    } else {
                                        Text(if (animeIdInput.isBlank()) "Simpan Anime" else "Update Anime")
                                    }
                                }

                                if (animeIdInput.isNotBlank()) {
                                    OutlinedButton(
                                        onClick = {
                                            animeIdInput = ""
                                            titleInput = ""
                                            synopsisInput = ""
                                            posterUrlInput = ""
                                        }
                                    ) {
                                        Text("Batal")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Daftar Anime Terdaftar (${animeList.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(animeList) { anime ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(anime.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text("${anime.type} • ${anime.season} • ${anime.studio} • ★ ${anime.rating}", fontSize = 12.sp, color = Color.LightGray)
                                    }
                                    IconButton(onClick = {
                                        animeIdInput = anime.id
                                        titleInput = anime.title
                                        japaneseTitleInput = anime.japaneseTitle
                                        synopsisInput = anime.synopsis
                                        posterUrlInput = anime.posterUrl
                                        ratingInput = anime.rating.toString()
                                        typeInput = anime.type
                                        seasonInput = anime.season
                                        studioInput = anime.studio
                                        statusInput = anime.status
                                        episodesCountInput = anime.totalEpisodes.toString()
                                        genresInput = anime.genres.joinToString(", ")
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.LightGray)
                                    }
                                    IconButton(onClick = {
                                        coroutineScope.launch {
                                            repo.deleteAnime(anime.id).onSuccess {
                                                Toast.makeText(context, "Dihapus dari Firestore", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. KELOLA EPISODE & LINK STREAMING MULTI-SERVER
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Kelola Episode & Link Video Streaming",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Pilih Judul Anime:", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                        Spacer(modifier = Modifier.height(4.dp))

                        // Selection chips
                        val activeSelection = selectedAnimeForEpisode ?: animeList.firstOrNull() ?: initialSampleAnime.first()
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(animeList) { anime ->
                                val isSelected = activeSelection.id == anime.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedAnimeForEpisode = anime },
                                    label = { Text(anime.title.take(16) + "...", fontSize = 11.sp) }
                                )
                            }
                        }

                        val targetAnime = selectedAnimeForEpisode ?: animeList.firstOrNull() ?: initialSampleAnime.first()
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Target: ${targetAnime.title}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = episodeNumInput,
                                onValueChange = { episodeNumInput = it },
                                label = { Text("No. Eps") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = episodeTitleInput,
                                onValueChange = { episodeTitleInput = it },
                                label = { Text("Judul Episode") },
                                modifier = Modifier.weight(2.5f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = streamUrlUpbolt,
                            onValueChange = { streamUrlUpbolt = it },
                            label = { Text("Server 1: Upbolt Player (Embed/Iframe)") },
                            placeholder = { Text("https://upbolt.to/e/...") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = streamUrlStreamwish,
                            onValueChange = { streamUrlStreamwish = it },
                            label = { Text("Server 2: Streamwish / Backup Player") },
                            placeholder = { Text("https://streamwish.to/e/...") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Tautan Unduhan (Download Multi-Resolusi):", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = download1080Input,
                            onValueChange = { download1080Input = it },
                            label = { Text("1080p (Viking)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = download720Input,
                            onValueChange = { download720Input = it },
                            label = { Text("720p (FileQ)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = download480Input,
                            onValueChange = { download480Input = it },
                            label = { Text("480p (Google Drive)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                val epNum = episodeNumInput.toIntOrNull() ?: 1
                                isSavingEpisode = true
                                val episode = EpisodeItem(
                                    id = "${targetAnime.id}-ep-$epNum",
                                    episodeNumber = epNum,
                                    title = if (episodeTitleInput.isNotBlank()) episodeTitleInput else "Episode $epNum",
                                    streamSources = listOf(
                                        StreamSource("Upbolt HD", streamUrlUpbolt),
                                        StreamSource("Streamwish", streamUrlStreamwish)
                                    ),
                                    downloadLinks = listOf(
                                        DownloadLink("1080p", "VIKING", download1080Input),
                                        DownloadLink("720p", "FILEQ", download720Input),
                                        DownloadLink("480p", "GDRIVE", download480Input)
                                    )
                                )

                                coroutineScope.launch {
                                    val result = repo.addEpisode(targetAnime.id, episode)
                                    isSavingEpisode = false
                                    result.onSuccess {
                                        Toast.makeText(context, "Episode $epNum berhasil disimpan!", Toast.LENGTH_SHORT).show()
                                        episodeNumInput = (epNum + 1).toString()
                                        episodeTitleInput = ""
                                    }.onFailure {
                                        Toast.makeText(context, "Gagal menyimpan episode: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = !isSavingEpisode,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isSavingEpisode) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                            } else {
                                Text("Simpan Episode ke Firestore")
                            }
                        }
                    }
                }

                // 4. ANIME SCRAPER FITUR (EKSTRAK WEB OTOMATIS)
                3 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Scraper Otomatis Situs Anime",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Masukkan URL situs nonton anime atau tautan episode untuk mengekstrak judul, sinopsis, poster, dan tautan embed video secara otomatis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = scraperUrlInput,
                            onValueChange = { scraperUrlInput = it },
                            label = { Text("URL Halaman Anime / Episode") },
                            placeholder = { Text("https://example-anime-site.com/anime/...") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (scraperUrlInput.isBlank()) {
                                    Toast.makeText(context, "Masukkan URL terlebih dahulu", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isScraping = true
                                coroutineScope.launch {
                                    val result = AnimeScraper.scrapeUrl(scraperUrlInput)
                                    isScraping = false
                                    result.onSuccess { data ->
                                        scrapedResult = data
                                        Toast.makeText(context, "Berhasil mengekstrak: ${data.title}", Toast.LENGTH_SHORT).show()
                                    }.onFailure {
                                        // Provide fallback extraction
                                        val fallbackData = AnimeScraper.parseHtml("", scraperUrlInput)
                                        scrapedResult = fallbackData
                                        Toast.makeText(context, "Ekstraksi pola URL berhasil: ${fallbackData.title}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = !isScraping,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isScraping) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sedang Scraping Web...")
                            } else {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mulai Scrape URL Anime")
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Scraped Results Card
                        if (scrapedResult != null) {
                            val data = scrapedResult!!
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Hasil Ekstraksi Scraper:",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context).data(data.posterUrl).crossfade(true).build(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(70.dp, 95.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(data.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(data.synopsis.take(90) + "...", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Genre: ${data.genres.joinToString(", ")}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Video Embed Terdeteksi: ${data.streamSources.firstOrNull()?.url ?: "https://upbolt.to/e/u2jey90afkvg"}",
                                        fontSize = 11.sp,
                                        color = Color.LightGray
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Apply to form
                                        Button(
                                            onClick = {
                                                titleInput = data.title
                                                synopsisInput = data.synopsis
                                                posterUrlInput = data.posterUrl
                                                genresInput = data.genres.joinToString(", ")
                                                ratingInput = data.rating.toString()
                                                selectedTab = 1 // Switch to Anime Tab
                                                Toast.makeText(context, "Data ditransfer ke form!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text("Terapkan ke Form", fontSize = 12.sp)
                                        }

                                        // Save directly to Firestore
                                        OutlinedButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    val animeItem = AnimeItem(
                                                        id = data.title.lowercase().replace(" ", "-").replace(Regex("[^a-z0-9-]"), ""),
                                                        title = data.title,
                                                        synopsis = data.synopsis,
                                                        posterUrl = data.posterUrl,
                                                        genres = data.genres,
                                                        rating = data.rating
                                                    )
                                                    repo.addAnime(animeItem)

                                                    // Also add Episode 1 with scraped stream
                                                    val ep1 = EpisodeItem(
                                                        id = "${animeItem.id}-ep-1",
                                                        episodeNumber = 1,
                                                        title = "Episode 1 (Scraped)",
                                                        streamSources = data.streamSources.ifEmpty {
                                                            listOf(StreamSource("HD", "https://upbolt.to/e/u2jey90afkvg"))
                                                        },
                                                        downloadLinks = data.downloadLinks
                                                    )
                                                    repo.addEpisode(animeItem.id, ep1)

                                                    Toast.makeText(context, "Berhasil disimpan ke Firestore!", Toast.LENGTH_LONG).show()
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Simpan ke Cloud", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        // Batch Episode Generator
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Generator Episode Massal",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Otomatis buat episode 1 sampai N sekaligus dengan template tautan server.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                val targetBatchAnime = animeList.firstOrNull() ?: initialSampleAnime.first()
                                Text("Target Anime: ${targetBatchAnime.title}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = batchCountInput,
                                        onValueChange = { batchCountInput = it },
                                        label = { Text("Jumlah Eps") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = batchBaseUrlInput,
                                        onValueChange = { batchBaseUrlInput = it },
                                        label = { Text("Template URL ({ep} nomor)") },
                                        modifier = Modifier.weight(2.5f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        val count = batchCountInput.toIntOrNull() ?: 12
                                        isBatchGenerating = true
                                        coroutineScope.launch {
                                            val generated = AnimeScraper.generateBatchEpisodes(
                                                animeId = targetBatchAnime.id,
                                                totalEpisodes = count,
                                                baseStreamPattern = batchBaseUrlInput
                                            )
                                            for (ep in generated) {
                                                repo.addEpisode(targetBatchAnime.id, ep)
                                            }
                                            isBatchGenerating = false
                                            Toast.makeText(context, "Sukses membuat $count episode untuk ${targetBatchAnime.title}!", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    enabled = !isBatchGenerating,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (isBatchGenerating) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                                    } else {
                                        Text("Generate $batchCountInput Episode Massal")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
