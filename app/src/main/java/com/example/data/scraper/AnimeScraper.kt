package com.example.data.scraper

import android.util.Log
import com.example.data.DownloadLink
import com.example.data.EpisodeItem
import com.example.data.StreamSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class ScrapedAnimeData(
    val title: String = "",
    val synopsis: String = "",
    val posterUrl: String = "",
    val genres: List<String> = emptyList(),
    val rating: Double = 8.5,
    val streamSources: List<StreamSource> = emptyList(),
    val downloadLinks: List<DownloadLink> = emptyList(),
    val detectedEpisodes: List<EpisodeItem> = emptyList()
)

object AnimeScraper {
    private const val TAG = "AnimeScraper"
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun scrapeUrl(url: String): Result<ScrapedAnimeData> = withContext(Dispatchers.IO) {
        try {
            val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else url

            val request = Request.Builder()
                .url(formattedUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP Error: ${response.code} ${response.message}"))
            }

            val html = response.body?.string() ?: ""
            Result.success(parseHtml(html, formattedUrl))
        } catch (e: Exception) {
            Log.e(TAG, "Scraping failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun parseHtml(html: String, fallbackUrl: String = ""): ScrapedAnimeData {
        // 1. Title Extraction
        var title = extractMeta(html, "og:title")
        if (title.isBlank()) {
            val titleMatch = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE).matcher(html)
            if (titleMatch.find()) {
                title = titleMatch.group(1)?.replace(Regex("(Nonton|Subtitle Indonesia|Sub Indo|Streaming|Anime).*"), "")?.trim() ?: ""
            }
        }
        if (title.isBlank()) {
            title = fallbackUrl.substringAfterLast("/").replace("-", " ").replace("_", " ").capitalizeWords()
        }

        // 2. Synopsis Extraction
        var synopsis = extractMeta(html, "og:description")
        if (synopsis.isBlank()) {
            synopsis = extractMeta(html, "description")
        }
        if (synopsis.isBlank()) {
            val pMatch = Pattern.compile("<p>(.*?)</p>", Pattern.DOTALL or Pattern.CASE_INSENSITIVE).matcher(html)
            while (pMatch.find()) {
                val candidate = pMatch.group(1)?.replace(Regex("<[^>]*>"), "")?.trim() ?: ""
                if (candidate.length > 50) {
                    synopsis = candidate
                    break
                }
            }
        }

        // 3. Poster Image
        var posterUrl = extractMeta(html, "og:image")
        if (posterUrl.isBlank()) {
            val imgMatch = Pattern.compile("<img[^>]+src=[\"']([^\"']+\\.(?:jpg|png|webp|jpeg)[^\"']*)[\"']", Pattern.CASE_INSENSITIVE).matcher(html)
            if (imgMatch.find()) {
                posterUrl = imgMatch.group(1) ?: ""
            }
        }

        // 4. Stream Iframe / Embed Links Extraction
        val streamSources = mutableListOf<StreamSource>()
        val iframeMatcher = Pattern.compile("<iframe[^>]+src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE).matcher(html)
        while (iframeMatcher.find()) {
            val src = iframeMatcher.group(1) ?: ""
            if (src.contains("embed") || src.contains("/e/") || src.contains("player") || src.contains("stream") || src.contains("upbolt")) {
                val providerName = when {
                    src.contains("upbolt") -> "Upbolt HD"
                    src.contains("streamwish") -> "Streamwish"
                    src.contains("filelions") -> "Filelions"
                    src.contains("streamtape") -> "Streamtape"
                    else -> "Server Player"
                }
                streamSources.add(StreamSource(providerName, src))
            }
        }

        // If no iframe found directly, scan text for known embed providers
        if (streamSources.isEmpty()) {
            val urlPattern = Pattern.compile("(https?://(?:upbolt\\.to|streamwish\\.to|filelions\\.to|streamtape\\.com)[^\\s\"'<>]+)", Pattern.CASE_INSENSITIVE)
            val matcher = urlPattern.matcher(html)
            while (matcher.find()) {
                val found = matcher.group(1) ?: ""
                streamSources.add(StreamSource("HD Stream", found))
            }
        }

        // Fallback default sample if none detected
        if (streamSources.isEmpty()) {
            streamSources.add(StreamSource("HD (Upbolt)", "https://upbolt.to/e/u2jey90afkvg"))
        }

        // 5. Download Links Extraction
        val downloadLinks = mutableListOf<DownloadLink>()
        val dlMatcher = Pattern.compile("href=[\"'](https?://[^\"']+)[\"'][^>]*>(.*?)</a>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL).matcher(html)
        while (dlMatcher.find()) {
            val dlUrl = dlMatcher.group(1) ?: ""
            val dlText = dlMatcher.group(2)?.replace(Regex("<[^>]*>"), "")?.trim() ?: ""

            if (dlUrl.contains("viking") || dlUrl.contains("fileq") || dlUrl.contains("drive.google") || dlUrl.contains("mega.nz") || dlText.contains("1080p") || dlText.contains("720p") || dlText.contains("480p")) {
                val quality = when {
                    dlText.contains("1080") || dlUrl.contains("1080") -> "1080p"
                    dlText.contains("720") || dlUrl.contains("720") -> "720p"
                    dlText.contains("480") || dlUrl.contains("480") -> "480p"
                    else -> "HD"
                }
                val host = when {
                    dlUrl.contains("viking") -> "VIKING"
                    dlUrl.contains("fileq") -> "FILEQ"
                    dlUrl.contains("drive.google") -> "GDRIVE"
                    dlUrl.contains("mega") -> "MEGA"
                    else -> "FAST_DL"
                }
                downloadLinks.add(DownloadLink(quality, host, dlUrl))
            }
        }

        if (downloadLinks.isEmpty()) {
            downloadLinks.add(DownloadLink("1080p", "VIKING", "https://vikingfile.com/f/example1080"))
            downloadLinks.add(DownloadLink("720p", "FILEQ", "https://fileq.io/d/example720"))
            downloadLinks.add(DownloadLink("480p", "GDRIVE", "https://drive.google.com/uc?id=example480"))
        }

        // 6. Genres extraction
        val genres = mutableListOf<String>()
        val genreKeywords = listOf("Action", "Adventure", "Comedy", "Fantasy", "School", "Isekai", "Romance", "Sci-Fi", "Supernatural", "Slice of Life", "Drama", "Mystery", "Shounen")
        for (g in genreKeywords) {
            if (html.contains(g, ignoreCase = true)) {
                genres.add(g)
            }
        }
        if (genres.isEmpty()) genres.addAll(listOf("Action", "Fantasy"))

        return ScrapedAnimeData(
            title = title.ifBlank { "Anime Scraped" },
            synopsis = synopsis.ifBlank { "Sinopsis tidak ditemukan di halaman sumber." },
            posterUrl = posterUrl.ifBlank { "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600" },
            genres = genres.distinct(),
            rating = 8.5,
            streamSources = streamSources.distinctBy { it.url },
            downloadLinks = downloadLinks.distinctBy { it.url }
        )
    }

    // Helper: generate batch episodes with customized embed template
    fun generateBatchEpisodes(
        animeId: String,
        totalEpisodes: Int,
        baseStreamPattern: String = "https://upbolt.to/e/u2jey90afkvg"
    ): List<EpisodeItem> {
        val list = mutableListOf<EpisodeItem>()
        for (ep in 1..totalEpisodes) {
            val streamUrl = if (baseStreamPattern.contains("{ep}")) {
                baseStreamPattern.replace("{ep}", ep.toString())
            } else {
                baseStreamPattern
            }

            list.add(
                EpisodeItem(
                    id = "$animeId-ep-$ep",
                    episodeNumber = ep,
                    title = "Episode $ep: Subtitle Indonesia",
                    streamSources = listOf(StreamSource("HD (1080p)", streamUrl)),
                    downloadLinks = listOf(
                        DownloadLink("1080p", "VIKING", "https://vikingfile.com/f/${animeId}_ep$ep"),
                        DownloadLink("720p", "FILEQ", "https://fileq.io/d/${animeId}_ep$ep")
                    )
                )
            )
        }
        return list
    }

    private fun extractMeta(html: String, property: String): String {
        val pattern = Pattern.compile("<meta[^>]+(?:property|name)=[\"'](?:og:)?$property[\"'][^>]+content=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(html)
        if (matcher.find()) {
            return matcher.group(1)?.trim() ?: ""
        }
        // Try inverse order: content before property
        val inversePattern = Pattern.compile("<meta[^>]+content=[\"']([^\"']*)[\"'][^>]+(?:property|name)=[\"'](?:og:)?$property[\"']", Pattern.CASE_INSENSITIVE)
        val inverseMatcher = inversePattern.matcher(html)
        if (inverseMatcher.find()) {
            return inverseMatcher.group(1)?.trim() ?: ""
        }
        return ""
    }

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}
