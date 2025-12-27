# Campus Lost & Found

Aplikasi Android modern untuk melaporkan dan menemukan barang hilang di kampus menggunakan Kotlin dan Jetpack Compose.

## 🚀 Fitur

- ✅ **Laporkan Barang Hilang/Ditemukan** - Form lengkap dengan upload foto
- ✅ **Pencarian Real-time** - Cari barang berdasarkan nama, lokasi, atau deskripsi
- ✅ **Filter** - Filter berdasarkan jenis laporan (Hilang/Ditemukan)
- ✅ **Hubungi via WhatsApp** - Langsung hubungi pelapor via WhatsApp
- ✅ **Kelola Laporan** - Hapus atau tandai selesai laporan milik sendiri
- ✅ **UI Modern** - Material 3 Design dengan dark mode support
- ✅ **Real-time Updates** - Update otomatis menggunakan Firestore

## 🛠️ Teknologi

- **Kotlin** - Bahasa pemrograman utama
- **Jetpack Compose** - UI modern declarative
- **Material 3** - Design system terbaru
- **Firebase Firestore** - Database real-time
- **Firebase Storage** - Penyimpanan gambar
- **Firebase Auth** - Autentikasi anonymous
- **Coil** - Image loading library
- **Navigation Compose** - Navigasi antar screen

## 📋 Prasyarat

- Android Studio Otter 2 / Koala atau lebih baru
- Android SDK 24+ (minimum)
- Android SDK 34 (target)
- Kotlin 1.9.20+
- Firebase project (untuk production)

## 🔧 Setup

### 1. Clone Repository

```bash
git clone <repository-url>
cd Kampus_Lost&Found
```

### 2. Setup Firebase

1. Buka [Firebase Console](https://console.firebase.google.com/)
2. Buat project baru atau gunakan project yang sudah ada
3. Tambahkan aplikasi Android dengan package name: `com.campus.lostfound`
4. Download `google-services.json`
5. Letakkan file tersebut di folder `app/`

### 3. Konfigurasi Firestore

Buka Firebase Console → Firestore Database:

1. Buat database baru (mode production atau test)
2. Tambahkan collection dengan nama `items`
3. Atur security rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /items/{itemId} {
      // Public read
      allow read: if true;
      
      // Anyone can create
      allow create: if request.auth != null;
      
      // Only owner can update/delete
      allow update, delete: if request.auth != null && 
        resource.data.userId == request.auth.uid;
    }
  }
}
```

### 4. Konfigurasi Firebase Storage

1. Buka Firebase Console → Storage
2. Enable Storage jika belum
3. Atur security rules:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /items/{allPaths=**} {
      allow read: if true;
      allow write: if request.auth != null;
      allow delete: if request.auth != null;
    }
  }
}
```

### 5. Build & Run

1. Buka project di Android Studio
2. Sync Gradle files
3. Run aplikasi di device fisik atau emulator

## 📱 Struktur Aplikasi

### Bottom Navigation (4 Tab)

1. **Beranda** - Lihat semua laporan terbaru
2. **Tambah** - Buat laporan baru
3. **Aktivitas** - Kelola laporan milik sendiri
4. **Pengaturan** - Pengaturan aplikasi

### Screen Details

#### Beranda
- Header dengan judul
- Search bar untuk mencari barang
- Filter chips (Semua, Hilang, Ditemukan)
- List card laporan dengan:
  - Thumbnail foto
  - Badge status
  - Nama barang
  - Lokasi
  - Waktu
  - Tombol hubungi WhatsApp

#### Tambah Laporan
- Form bertahap dengan validasi
- Pilih jenis laporan (Hilang/Ditemukan)
- Input nama barang, kategori, lokasi
- Upload foto (Kamera/Galeri)
- Input nomor WhatsApp
- Deskripsi opsional

#### Aktivitas
- List laporan milik user
- Tombol "Tandai Selesai"
- Tombol "Hapus" dengan konfirmasi
- Status completed indicator

#### Pengaturan
- Tema (Ikuti sistem)
- Notifikasi toggle
- Informasi privasi
- Tentang aplikasi

## 🎨 Design System

- **Primary Color**: Biru (#2196F3)
- **Secondary Color**: Teal (#009688)
- **Lost Badge**: Merah lembut (#EF5350)
- **Found Badge**: Hijau (#66BB6A)
- **Material 3**: Mengikuti Material Design 3 guidelines

## 🔐 Keamanan

- User authentication menggunakan Firebase Anonymous Auth
- Setiap user memiliki anonymous ID
- User hanya bisa menghapus laporan milik sendiri
- Laporan bersifat public read untuk semua user
- Nomor WhatsApp hanya digunakan untuk komunikasi

## 📦 Build APK

Untuk build APK release:

```bash
./gradlew assembleRelease
```

APK akan berada di: `app/build/outputs/apk/release/`

## 🐛 Troubleshooting

### Error: google-services.json tidak ditemukan
- Pastikan file `google-services.json` ada di folder `app/`
- Pastikan package name di Firebase Console sesuai dengan `applicationId` di `build.gradle.kts`

### Error: Permission denied
- Pastikan permission untuk Camera dan Storage sudah di-set di AndroidManifest.xml
- Untuk Android 13+, pastikan permission runtime sudah di-handle

### Error: Firestore connection failed
- Pastikan Firestore sudah di-enable di Firebase Console
- Pastikan security rules sudah di-set dengan benar
- Pastikan device/emulator memiliki koneksi internet

## 📝 Catatan

- Aplikasi menggunakan anonymous authentication, jadi tidak perlu login
- Data laporan tersimpan di Firestore dengan struktur yang sudah ditentukan
- Gambar disimpan di Firebase Storage dengan path `items/{uuid}.jpg`
- Aplikasi siap untuk production setelah setup Firebase lengkap

## 👨‍💻 Pengembang

Dibuat untuk project kampus dengan teknologi modern Android development.

## 📄 Lisensi

Project ini dibuat untuk keperluan akademik.

