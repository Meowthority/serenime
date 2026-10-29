package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreRepository(context: Context) {
    private val TAG = "FirestoreRepo"
    private val db: FirebaseFirestore = try {
        val databaseId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(databaseId)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get named database instance, falling back to default", e)
        FirebaseFirestore.getInstance()
    }

    // Observe all anime in real-time
    fun observeAnimeList(): Flow<List<AnimeItem>> = callbackFlow {
        val subscription = db.collection("anime")
            .orderBy("title", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for anime collection: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(AnimeItem::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            Log.w(TAG, "Error mapping AnimeItem: ${e.message}")
                            null
                        }
                    }
                    trySend(items)
                }
            }
        awaitClose { subscription.remove() }
    }.flowOn(Dispatchers.IO)

    // Observe episodes for a specific anime
    fun observeEpisodes(animeId: String): Flow<List<EpisodeItem>> = callbackFlow {
        val subscription = db.collection("anime").document(animeId)
            .collection("episodes")
            .orderBy("episodeNumber", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for episodes: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(EpisodeItem::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(items)
                }
            }
        awaitClose { subscription.remove() }
    }.flowOn(Dispatchers.IO)

    // Admin: Add new anime
    suspend fun addAnime(item: AnimeItem): Result<String> = withContext(Dispatchers.IO) {
        try {
            val collection = db.collection("anime")
            val docRef = if (item.id.isNotBlank()) collection.document(item.id) else collection.document()
            val payload = hashMapOf<String, Any>(
                "title" to item.title,
                "synopsis" to item.synopsis,
                "posterUrl" to item.posterUrl,
                "rating" to item.rating,
                "genres" to item.genres,
                "status" to item.status,
                "totalEpisodes" to item.totalEpisodes,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "addAnime failed", e)
            Result.failure(e)
        }
    }

    // Admin: Update anime
    suspend fun updateAnime(item: AnimeItem): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(item.id.isNotBlank()) { "Anime ID must not be blank for update" }
            val docRef = db.collection("anime").document(item.id)
            val payload = hashMapOf<String, Any>(
                "title" to item.title,
                "synopsis" to item.synopsis,
                "posterUrl" to item.posterUrl,
                "rating" to item.rating,
                "genres" to item.genres,
                "status" to item.status,
                "totalEpisodes" to item.totalEpisodes,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "updateAnime failed", e)
            Result.failure(e)
        }
    }

    // Admin: Delete anime
    suspend fun deleteAnime(animeId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            db.collection("anime").document(animeId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "deleteAnime failed", e)
            Result.failure(e)
        }
    }

    // Admin: Add or update episode
    suspend fun addEpisode(animeId: String, episode: EpisodeItem): Result<String> = withContext(Dispatchers.IO) {
        try {
            val episodesRef = db.collection("anime").document(animeId).collection("episodes")
            val docRef = if (episode.id.isNotBlank()) episodesRef.document(episode.id) else episodesRef.document()

            val streamSourcesList = episode.streamSources.map {
                mapOf("source" to it.source, "url" to it.url)
            }
            val downloadLinksList = episode.downloadLinks.map {
                mapOf("quality" to it.quality, "host" to it.host, "url" to it.url)
            }

            val payload = hashMapOf<String, Any>(
                "episodeNumber" to episode.episodeNumber,
                "title" to episode.title,
                "streamSources" to streamSourcesList,
                "downloadLinks" to downloadLinksList,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "addEpisode failed", e)
            Result.failure(e)
        }
    }

    // Admin Helper: Seed sample anime catalog into Firestore
    suspend fun seedInitialData(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var count = 0
            for (anime in initialSampleAnime) {
                addAnime(anime)
                count++

                // Seed at least Episode 1 and 2 with mock streaming and download links
                val ep1 = EpisodeItem(
                    id = "${anime.id}-ep-1",
                    episodeNumber = 1,
                    title = "Episode 1: Awal Mula Petualangan",
                    streamSources = listOf(
                        StreamSource("HD (1080p)", "https://upbolt.to/e/u2jey90afkvg"),
                        StreamSource("SD (720p)", "https://upbolt.to/e/u2jey90afkvg")
                    ),
                    downloadLinks = listOf(
                        DownloadLink("1080p", "VIKING", "https://vikingfile.com/f/KyiS4sIbps"),
                        DownloadLink("720p", "FILEQ", "https://fileq.io/d/example720"),
                        DownloadLink("480p", "GDRIVE", "https://drive.google.com/uc?id=example480")
                    )
                )
                val ep2 = EpisodeItem(
                    id = "${anime.id}-ep-2",
                    episodeNumber = 2,
                    title = "Episode 2: Tantangan Baru",
                    streamSources = listOf(
                        StreamSource("HD (1080p)", "https://upbolt.to/e/u2jey90afkvg")
                    ),
                    downloadLinks = listOf(
                        DownloadLink("1080p", "VIKING", "https://vikingfile.com/f/KyiS4sIbps"),
                        DownloadLink("720p", "FILEQ", "https://fileq.io/d/example720")
                    )
                )
                addEpisode(anime.id, ep1)
                addEpisode(anime.id, ep2)
            }
            Result.success(count)
        } catch (e: Exception) {
            Log.e(TAG, "seedInitialData failed", e)
            Result.failure(e)
        }
    }

    // User: Favorites
    fun observeFavorites(userId: String): Flow<List<FavoriteItem>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val subscription = db.collection("users").document(userId)
            .collection("favorites")
            .orderBy("addedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Failed observing favorites: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull {
                    it.toObject(FavoriteItem::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { subscription.remove() }
    }.flowOn(Dispatchers.IO)

    // Toggle favorite state
    suspend fun toggleFavorite(userId: String, anime: AnimeItem): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            require(userId.isNotBlank()) { "User must be logged in to manage favorites" }
            val docRef = db.collection("users").document(userId).collection("favorites").document(anime.id)
            val doc = docRef.get().await()
            if (doc.exists()) {
                docRef.delete().await()
                Result.success(false) // Removed
            } else {
                val data = hashMapOf(
                    "animeId" to anime.id,
                    "title" to anime.title,
                    "posterUrl" to anime.posterUrl,
                    "rating" to anime.rating,
                    "episodeCount" to anime.totalEpisodes,
                    "addedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(data).await()
                Result.success(true) // Added
            }
        } catch (e: Exception) {
            Log.e(TAG, "toggleFavorite failed", e)
            Result.failure(e)
        }
    }
}
