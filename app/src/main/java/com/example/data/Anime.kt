package com.example.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

// Backward compatible model for simple views
data class Anime(
    val id: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val episode: String = "",
    val rating: Double = 0.0,
    val synopsis: String = "",
    val genres: List<String> = emptyList(),
    val type: String = "TV",
    val season: String = "Fall 2026",
    val studio: String = "Ufotable"
)

@IgnoreExtraProperties
data class AnimeItem(
    @DocumentId val id: String = "",
    val title: String = "",
    val japaneseTitle: String = "",
    val synopsis: String = "",
    val posterUrl: String = "",
    val rating: Double = 0.0,
    val genres: List<String> = emptyList(),
    val status: String = "Ongoing",
    val type: String = "TV",
    val season: String = "Fall 2026",
    val studio: String = "",
    val totalEpisodes: Int = 12,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toAnime(): Anime {
        return Anime(
            id = id,
            title = title,
            imageUrl = posterUrl,
            episode = "Ep $totalEpisodes",
            rating = rating,
            synopsis = synopsis,
            genres = genres,
            type = type,
            season = season,
            studio = studio
        )
    }
}

data class StreamSource(
    val source: String = "HD",
    val url: String = ""
)

data class DownloadLink(
    val quality: String = "720p",
    val host: String = "VIKING",
    val url: String = ""
)

@IgnoreExtraProperties
data class EpisodeItem(
    @DocumentId val id: String = "",
    val episodeNumber: Int = 1,
    val title: String = "",
    val streamSources: List<StreamSource> = emptyList(),
    val downloadLinks: List<DownloadLink> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val role: String = "user",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FavoriteItem(
    @DocumentId val id: String = "",
    val animeId: String = "",
    val title: String = "",
    val posterUrl: String = "",
    val rating: Double = 0.0,
    val episodeCount: Int = 12,
    @ServerTimestamp val addedAt: Timestamp? = null
)

// Initial curated anime dataset with complete categories
val initialSampleAnime = listOf(
    AnimeItem(
        id = "seitokai-ni-mo-ana-wa-aru",
        title = "Seitokai ni mo Ana wa Aru!",
        japaneseTitle = "生徒会にも穴はある！",
        synopsis = "Kisah komedi kehidupan sehari-hari pengurus OSIS dengan dinamika humor yang segar, unik, dan penuh kejutan di lingkungan sekolah.",
        posterUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop&q=80",
        rating = 8.6,
        genres = listOf("Comedy", "School", "Slice of Life"),
        status = "Ongoing",
        type = "TV",
        season = "Summer 2026",
        studio = "Doga Kobo",
        totalEpisodes = 12
    ),
    AnimeItem(
        id = "demon-slayer-kimetsu",
        title = "Demon Slayer: Kimetsu no Yaiba",
        japaneseTitle = "鬼滅の刃",
        synopsis = "Tanjiro Kamado bertekad menjadi pembasmi iblis untuk membalaskan dendam keluarganya dan mengembalikan adiknya, Nezuko, menjadi manusia.",
        posterUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=600&auto=format&fit=crop&q=80",
        rating = 9.2,
        genres = listOf("Action", "Fantasy", "Shounen"),
        status = "Completed",
        type = "TV",
        season = "Fall 2026",
        studio = "Ufotable",
        totalEpisodes = 26
    ),
    AnimeItem(
        id = "jujutsu-kaisen-s2",
        title = "Jujutsu Kaisen Season 2",
        japaneseTitle = "呪術廻戦",
        synopsis = "Kisah masa lalu Satoru Gojo dan Suguru Geto serta insiden Shibuya yang mengubah tatanan dunia sihir kutukan selamanya.",
        posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80",
        rating = 9.0,
        genres = listOf("Action", "Supernatural", "Dark Fantasy"),
        status = "Completed",
        type = "TV",
        season = "Fall 2026",
        studio = "MAPPA",
        totalEpisodes = 23
    ),
    AnimeItem(
        id = "frieren-beyond-journeys-end",
        title = "Frieren: Beyond Journey's End",
        japaneseTitle = "葬送のフリーレン",
        synopsis = "Penyihir elf Frieren memulai perjalanan baru untuk memahami makna ikatan dan waktu manusia setelah kemenangan atas Raja Iblis.",
        posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
        rating = 9.4,
        genres = listOf("Adventure", "Drama", "Fantasy"),
        status = "Completed",
        type = "TV",
        season = "Winter 2026",
        studio = "Madhouse",
        totalEpisodes = 28
    ),
    AnimeItem(
        id = "one-piece-egghead",
        title = "One Piece: Egghead Arc",
        japaneseTitle = "ワンピース",
        synopsis = "Kelompok Topi Jerami tiba di Pulau Masa Depan Egghead dan berhadapan langsung dengan ilmuwan terhebat dunia, Dr. Vegapunk.",
        posterUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=600&auto=format&fit=crop&q=80",
        rating = 9.5,
        genres = listOf("Action", "Adventure", "Fantasy"),
        status = "Ongoing",
        type = "TV",
        season = "Ongoing",
        studio = "Toei Animation",
        totalEpisodes = 1100
    )
)

val mockAnimeList = initialSampleAnime.map { it.toAnime() }
