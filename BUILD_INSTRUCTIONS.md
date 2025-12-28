# 🛠️ PETUNJUK BUILD - Campus Lost & Found

## ⚠️ SEBELUM BUILD

### 1. Pastikan Environment Ready
- ✅ Android Studio versi terbaru (Hedgehog atau lebih baru)
- ✅ JDK 17 atau 18 terinstall
- ✅ Android SDK 34 terinstall
- ✅ Gradle 9.0 (akan auto-download)

### 2. Check Dependencies
File `build.gradle.kts` sudah include:
- Jetpack Compose
- Firebase Firestore & Auth
- Coil untuk image loading
- Navigation Compose
- Material 3

---

## 🚀 LANGKAH BUILD

### Step 1: Sync Gradle
```
File → Sync Project with Gradle Files
```
**Atau:** Klik icon "Sync Now" yang muncul

**Tunggu sampai:**
- ✅ "Gradle sync finished" muncul di bottom
- ✅ Tidak ada error merah di build output

---

### Step 2: Clean Project
```
Build → Clean Project
```
**Tunggu sampai selesai** (biasanya 10-30 detik)

---

### Step 3: Rebuild Project
```
Build → Rebuild Project
```
**Tunggu sampai:**
- ✅ "Build successful" muncul
- ✅ Tidak ada compile errors

**Jika ada error:**
- Baca pesan error dengan teliti
- Pastikan semua import statement ada
- Check file `google-services.json` ada di `app/`

---

### Step 4: Run Application

#### Opsi A: Emulator
1. Buka AVD Manager (Device Manager)
2. Start emulator (minimal API 26 / Android 8.0)
3. Tunggu emulator fully loaded
4. Klik Run button (▶️) atau **Shift+F10**

#### Opsi B: Physical Device
1. Aktifkan Developer Options & USB Debugging
2. Sambungkan via USB
3. Pilih device di dropdown
4. Klik Run button (▶️)

---

## 🎬 FIRST RUN EXPERIENCE

### Yang Akan Anda Lihat:

1. **Splash Screen (1.8s)**
   - Logo scale-in + fade-in
   - Text "Campus Lost & Found" muncul
   - Gradient background biru
   - Transisi smooth ke Beranda

2. **Beranda**
   - Header gradient dengan icon notifikasi
   - Search bar modern
   - Filter chips (Semua/Hilang/Ditemukan)
   - Empty state jika belum ada data

3. **Navigation**
   - Bottom nav dengan 4 tab
   - Icon berubah saat diklik
   - Label bold saat aktif

---

## ✅ TESTING CHECKLIST

### Test Basic Flow:
- [ ] Splash screen muncul dengan animasi
- [ ] Beranda loading dengan benar
- [ ] Search bar bisa diklik & ketik
- [ ] Filter chips bisa diklik & berubah warna
- [ ] Bottom nav responsive (icon & label berubah)
- [ ] Click "Tambah" → form muncul
- [ ] Upload foto → preview muncul
- [ ] Submit laporan → kembali ke Beranda
- [ ] Laporan muncul di Beranda & Aktivitas
- [ ] Click "Hubungi" → WhatsApp terbuka
- [ ] Edit laporan di Aktivitas
- [ ] Tandai selesai laporan
- [ ] Hapus laporan
- [ ] Pengaturan → toggle notifikasi

### Test Animations:
- [ ] Fade in/out antar halaman
- [ ] Bottom nav icon scale saat diklik
- [ ] Filter chip animasi saat pilih
- [ ] Card ripple effect saat disentuh

---

## 🐛 TROUBLESHOOTING

### Error: "google-services.json not found"
**Solusi:** Pastikan file Firebase config ada di `app/google-services.json`

### Error: "Unresolved reference"
**Solusi:** 
1. File → Invalidate Caches → Invalidate and Restart
2. Tunggu rebuild selesai

### Error: Build gagal dengan Gradle error
**Solusi:**
1. Check koneksi internet (untuk download dependencies)
2. File → Project Structure → Check Gradle version
3. Clean & Rebuild

### Error: App crash saat buka
**Solusi:**
1. Check logcat untuk error message
2. Pastikan Firebase initialized dengan benar
3. Check internet connection (untuk Firestore)

### Warning: Deprecated Gradle features
**Status:** AMAN DIABAIKAN
- Warning ini dari Gradle 9.0 vs 10.0
- Tidak mempengaruhi functionality
- Akan fixed di Gradle update berikutnya

---

## 📱 DEVICE REQUIREMENTS

### Minimum:
- Android 8.0 (API 26)
- 2GB RAM
- 100MB storage space

### Recommended:
- Android 12+ (API 31+)
- 4GB RAM
- 200MB storage space

---

## 🎯 BUILD VARIANTS

Default: **debug**
- Faster build
- Logging enabled
- Untuk testing

Release: **release**
- Optimized
- ProGuard enabled
- Untuk production

---

## 📊 BUILD TIME

**First build:** 2-5 menit (download dependencies)
**Subsequent builds:** 30-60 detik
**Incremental builds:** 10-20 detik

---

## ✅ SUCCESS INDICATORS

Build berhasil jika:
- ✅ No red errors di Android Studio
- ✅ "BUILD SUCCESSFUL" di build output
- ✅ App installed di device/emulator
- ✅ Splash screen muncul
- ✅ Beranda loading dengan benar
- ✅ Navigation berfungsi

---

## 🆘 NEED HELP?

1. **Check Logcat** - Untuk runtime errors
2. **Check Build Output** - Untuk compile errors
3. **Clean & Rebuild** - Solusi 80% masalah
4. **Invalidate Caches** - Solusi persistent issues

---

**Happy Building! 🚀**

*Jika semua langkah diikuti, build akan sukses 100%*

