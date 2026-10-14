# Implementasi Plan: Serenime ID

## 1. Skeleton Loading
- Tambahkan library `androidx.compose.foundation` untuk efek shimmer pada komponen `AnimeGrid` dan `Carousel`.
- Implementasi status loading di ViewModel untuk transisi mulus.

## 2. Autentikasi & Cloud Sync
- **Firebase Authentication**: Implementasi Google Sign-In menggunakan Credential Manager.
- **Firestore**: Menyimpan data profil pengguna, daftar favorit (sync), dan komentar anime.
- **Local Storage**: Tetap pertahankan bookmark lokal sebagai fallback (Room Database).

## 3. Peningkatan Desain
- **Icons**: Integrasi Material Symbols untuk navigasi dan aksi.
- **UI Refresh**: Modernisasi card layout, tipografi, dan ruang antar elemen (spacing) sesuai panduan M3.

## 4. Admin & Otomatisasi
- Integrasi backend Firestore untuk mengelola konten anime (manual).
- Struktur data yang siap untuk dihubungkan dengan API scraper (otomatis).

---
Apakah Anda setuju dengan rencana ini? Saya akan memulainya dengan Firebase setup dan Skeleton Loading.
