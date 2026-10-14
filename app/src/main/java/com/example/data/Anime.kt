package com.example.data

data class Anime(
    val id: String,
    val title: String,
    val imageUrl: String,
    val episode: String,
    val rating: Double
)

val mockAnimeList = listOf(
    Anime("1", "Seitokai ni mo Ana wa Aru!", "https://example.com/a.jpg", "Ep 2", 8.5),
    Anime("2", "Demon Slayer", "https://example.com/b.jpg", "Ep 15", 9.2),
    Anime("3", "One Piece", "https://example.com/c.jpg", "Ep 1050", 9.5),
    Anime("4", "Jujutsu Kaisen", "https://example.com/d.jpg", "Ep 20", 9.0)
)
