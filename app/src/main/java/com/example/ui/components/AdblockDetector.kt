package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun AdblockDetector(onAdblockDetected: () -> Unit) {
  var isAdblockDetected by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    val detected = withContext(Dispatchers.IO) {
      try {
        val url = URL("https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js")
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 3000
        connection.connect()
        false // Not detected
      } catch (e: Exception) {
        true // Detected (blocked)
      }
    }
    if (detected) {
      isAdblockDetected = true
      onAdblockDetected()
    }
  }

  if (isAdblockDetected) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)) {
      Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text("Adblocker atau Private DNS Terdeteksi!", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Mohon matikan Adblock / Private DNS Anda untuk mendukung Serenime ID agar tetap gratis.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
      }
    }
  }
}
