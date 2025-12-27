# Struktur Project Campus Lost & Found

## 📁 Struktur Folder

```
Kampus_Lost&Found/
├── app/
│   ├── build.gradle.kts          # App-level Gradle config
│   ├── proguard-rules.pro        # ProGuard rules
│   ├── google-services.json      # Firebase config (harus di-download)
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/com/campus/lostfound/
│           │   ├── MainActivity.kt              # Entry point
│           │   ├── data/
│           │   │   ├── model/
│           │   │   │   └── LostFoundItem.kt    # Data model
│           │   │   └── repository/
│           │   │       └── LostFoundRepository.kt  # Firebase operations
│           │   ├── navigation/
│           │   │   └── Navigation.kt            # Navigation graph
│           │   ├── ui/
│           │   │   ├── components/
│           │   │   │   ├── BottomNavigationBar.kt  # Bottom nav
│           │   │   │   └── ItemCard.kt            # Reusable card
│           │   │   ├── screen/
│           │   │   │   ├── HomeScreen.kt       # Beranda
│           │   │   │   ├── AddReportScreen.kt  # Tambah laporan
│           │   │   │   ├── ActivityScreen.kt   # Aktivitas
│           │   │   │   └── SettingsScreen.kt   # Pengaturan
│           │   │   ├── theme/
│           │   │   │   ├── Color.kt            # Color definitions
│           │   │   │   ├── Theme.kt            # Theme setup
│           │   │   │   └── Type.kt             # Typography
│           │   │   └── viewmodel/
│           │   │       ├── HomeViewModel.kt
│           │   │       ├── AddReportViewModel.kt
│           │   │       ├── ActivityViewModel.kt
│           │   │       └── SettingsViewModel.kt
│           │   └── util/
│           │       ├── ImagePicker.kt          # Image picker helper
│           │       └── WhatsAppUtil.kt          # WhatsApp integration
│           └── res/
│               ├── values/
│               │   ├── strings.xml
│               │   ├── colors.xml
│               │   └── themes.xml
│               └── xml/
│                   ├── backup_rules.xml
│                   ├── data_extraction_rules.xml
│                   └── file_paths.xml           # FileProvider paths
├── build.gradle.kts              # Project-level Gradle
├── settings.gradle.kts           # Gradle settings
├── gradle.properties             # Gradle properties
├── .gitignore                    # Git ignore rules
├── README.md                     # Main documentation
├── SETUP_FIREBASE.md             # Firebase setup guide
├── QUICK_START.md                # Quick start guide
└── PROJECT_STRUCTURE.md          # This file
```

## 📦 Komponen Utama

### 1. Data Layer

#### `LostFoundItem.kt`
- Data model untuk laporan
- Enum: `ItemType` (LOST, FOUND), `Category`
- Function: `getTimeAgo()` untuk format waktu

#### `LostFoundRepository.kt`
- Firebase Firestore operations
- Firebase Storage operations
- Anonymous authentication
- CRUD operations untuk items

### 2. UI Layer

#### Screens
- **HomeScreen**: List semua laporan dengan search & filter
- **AddReportScreen**: Form untuk membuat laporan baru
- **ActivityScreen**: Kelola laporan milik sendiri
- **SettingsScreen**: Pengaturan aplikasi

#### Components
- **BottomNavigationBar**: Bottom navigation dengan 4 tab
- **ItemCard**: Card reusable untuk menampilkan laporan

#### Theme
- **Color.kt**: Definisi warna (Primary, Secondary, dll)
- **Theme.kt**: Material 3 theme setup dengan dark mode
- **Type.kt**: Typography definitions

### 3. ViewModel Layer

- **HomeViewModel**: State management untuk HomeScreen
- **AddReportViewModel**: State management untuk AddReportScreen
- **ActivityViewModel**: State management untuk ActivityScreen
- **SettingsViewModel**: State management untuk SettingsScreen

### 4. Navigation

#### `Navigation.kt`
- Navigation graph menggunakan Navigation Compose
- 4 routes: Home, Add, Activity, Settings
- Screen sealed class untuk type-safe navigation

### 5. Utilities

#### `ImagePicker.kt`
- Helper untuk pick image dari gallery
- Helper untuk take photo dari camera
- Menggunakan Activity Result API

#### `WhatsAppUtil.kt`
- Function untuk buka WhatsApp dengan pesan otomatis
- Fallback ke web WhatsApp jika app tidak terinstall

## 🔄 Data Flow

```
User Action
    ↓
UI Screen
    ↓
ViewModel
    ↓
Repository
    ↓
Firebase (Firestore/Storage)
    ↓
Repository (Flow)
    ↓
ViewModel (StateFlow)
    ↓
UI Screen (Recomposition)
```

## 🎯 Key Features Implementation

### Real-time Updates
- Menggunakan Firestore `addSnapshotListener`
- Flow-based reactive programming
- Auto-update UI saat data berubah

### Image Upload
- Upload ke Firebase Storage
- Path disimpan di Firestore
- Auto-delete saat hapus laporan

### WhatsApp Integration
- Intent-based integration
- Auto-fill message dengan detail laporan
- Fallback ke web WhatsApp

### User Management
- Anonymous authentication
- User ID untuk ownership verification
- Hanya owner bisa hapus/update

## 📝 File Penting

### Build Configuration
- `build.gradle.kts`: Dependencies & build config
- `settings.gradle.kts`: Project settings
- `gradle.properties`: Gradle properties

### Firebase
- `google-services.json`: **HARUS di-download dari Firebase Console**
- `file_paths.xml`: FileProvider paths untuk camera

### Manifest
- `AndroidManifest.xml`: App configuration, permissions, activities

## 🚀 Build Output

- **APK**: `app/build/outputs/apk/release/app-release.apk`
- **AAB**: `app/build/outputs/bundle/release/app-release.aab`

## 📚 Dependencies

Lihat `app/build.gradle.kts` untuk daftar lengkap dependencies:
- Compose BOM
- Material 3
- Navigation Compose
- Firebase (Firestore, Storage, Auth)
- Coil (Image loading)
- Lifecycle & ViewModel

## 🔐 Security

- Anonymous auth untuk user identification
- Firestore security rules untuk data protection
- Storage security rules untuk file protection
- User hanya bisa hapus laporan milik sendiri

---

**Struktur ini mengikuti Android best practices dengan clean architecture principles.**

