# Rencana Implementasi: Perbaikan Login, Desain Interaktif, Tipografi Khas, dan Scraper Anime

Dokumen ini merinci peningkatan menyeluruh pada aplikasi **Serenime ID** berdasarkan masukan pengguna.

---

## 1. Perbaikan & Fleksibilitas Login (Emulator Quick Login)
- **Masalah**: Pada emulator browser / streaming Android, Google Play Services belum memiliki akun Google yang terdaftar sehingga Credential Manager tidak menampilkan daftar akun.
- **Solusi**:
  - Pertahankan Google Sign-In via `CredentialManager` untuk perangkat fisik nyata.
  - Tambahkan tombol **"Quick Login sebagai Admin"** (`realverizon@gmail.com`) dan **"Quick Login sebagai Pengguna"** khusus untuk lingkungan emulator / preview testing.
  - Pengguna dapat langsung masuk ke status Administrator dengan satu sentuhan dan langsung menguji Admin Panel tanpa kendala akun di emulator.

---

## 2. Tipografi Berkarakter Khas & Animasi Interaktif
- **Font Khas (Google Fonts)**:
  - Mengunduh font lokal menggunakan `font-util install` (misal: `Outfit` atau `Urbanist`) yang memiliki karakter modern, futuristik, dan tajam khas platform anime streaming.
  - Diterapkan ke seluruh hierarki teks: Judul, Rating, Badge Episode, dan Konten.
- **Animasi & Interaktivitas Jetpack Compose**:
  - Efek transisi halus antar tab dan halaman (*fade* dan *slide*).
  - Micro-interactions pada tombol (efek *scale / bounce* saat ditekan).
  - Glowing red neon aura pada kartu featured dan lencana admin.
  - Skeleton loading dengan efek shimmer gradien dinamis.

---

## 3. Peningkatan Admin Panel (Kategori Komprehensif)
- **Kategori & Metadata Anime Lengkap**:
  - Judul Romaji & Judul Asli.
  - Tipe Format (TV Series, Movie, OVA, Special).
  - Musim Rilis (Spring, Summer, Fall, Winter) & Tahun Rilis.
  - Studio Animasi (misal: MAPPA, Ufotable, Bones).
  - Status (Ongoing, Tamat) & Skor Rating.
  - Multi-genre selector interaktif (Action, Isekai, Romance, Fantasy, dll.).
- **Manajemen Episode & Link Lengkap**:
  - Tambah, edit, dan hapus episode massal.
  - Input embed player multi-server (Server 1: Upbolt, Server 2: Streamwish, Server 3: Filelions).
  - Input tautan download per resolusi (360p, 480p, 720p, 1080p) dan penyedia cloud (Viking, FileQ, Google Drive, Mega).

---

## 4. Fitur Scraper Otomatis di Admin Panel
- **Pusat Ekstraksi Web Anime**:
  - **Ekstrak Metadata Judul & Sinopsis**: Input URL halaman detail anime untuk menarik otomatis judul, sinopsis, poster, dan genre.
  - **Ekstrak Link Video Embed & Server Download**: Deteksi pola iframe / embed player (Upbolt, Streamwish) dan link unduhan resolusi 480p/720p/1080p.
  - **Generator Episode Massal**: Opsi untuk otomatis membuat urutan episode 1 sampai N sekaligus dengan template tautan server streaming.
  - Integrasi tombol "Terapkan ke Form" dan "Simpan Langsung ke Firestore".

---

## Verifikasi & Pengujian
- Uji Quick Login di emulator untuk memastikan status Admin langsung aktif.
- Uji coba pengisian anime dan episode melalui fitur Scraper.
- Verifikasi kompilasi Gradle bebas error dengan `compile_applet`.
