# 🔥 Setup Firestore Index - Solusi Error FAILED_PRECONDITION

## ❌ Error yang Terjadi

```
FAILED_PRECONDITION: The query requires an index
```

**Penyebab:**
Firestore memerlukan **composite index** untuk query yang menggunakan:
- `whereEqualTo()` + `orderBy()` pada field berbeda
- Multiple `whereEqualTo()` + `orderBy()`

## ✅ Solusi: Buat Composite Index

### Cara 1: Klik Link dari Error (PALING MUDAH)

1. **Buka link yang ada di error log:**
   ```
   https://console.firebase.google.com/v1/r/project/campus-lost-and-found-1f5ca/firestore/indexes?create_composite=...
   ```

2. Link akan langsung membuka halaman **Create Index** di Firebase Console

3. Klik **"Create Index"** (tombol biru)

4. **Tunggu index dibuat** (biasanya 1-5 menit)

5. Setelah selesai, aplikasi akan otomatis menggunakan index

### Cara 2: Manual Create Index

1. Buka **Firebase Console** → **Firestore Database**

2. Klik tab **"Indexes"** (di atas, sebelah Rules)

3. Klik tombol **"Create Index"** (tombol biru besar)

4. **Isi form Create Index:**

   **Collection ID:** `items`
   
   **Fields to index:**
   - Field 1: `isCompleted` → Type: **Ascending**
   - Field 2: `createdAt` → Type: **Descending**
   
   Klik **"Create"**

5. **Buat index kedua** (untuk filter by type):
   
   **Collection ID:** `items`
   
   **Fields to index:**
   - Field 1: `type` → Type: **Ascending**
   - Field 2: `isCompleted` → Type: **Ascending**
   - Field 3: `createdAt` → Type: **Descending**
   
   Klik **"Create"**

6. **Buat index ketiga** (untuk user items):
   
   **Collection ID:** `items`
   
   **Fields to index:**
   - Field 1: `userId` → Type: **Ascending**
   - Field 2: `createdAt` → Type: **Descending**
   
   Klik **"Create"**

7. **Tunggu semua index selesai dibuat** (status: "Enabled")

## 📋 Index yang Diperlukan

Aplikasi memerlukan **3 composite index**:

### Index 1: Filter by isCompleted + Order by createdAt
```
Collection: items
Fields:
  - isCompleted (Ascending)
  - createdAt (Descending)
```

### Index 2: Filter by type + isCompleted + Order by createdAt
```
Collection: items
Fields:
  - type (Ascending)
  - isCompleted (Ascending)
  - createdAt (Descending)
```

### Index 3: Filter by userId + Order by createdAt
```
Collection: items
Fields:
  - userId (Ascending)
  - createdAt (Descending)
```

## ⚡ Solusi Sementara (Sudah Diimplementasi)

Saya sudah menambahkan **fallback mechanism** di kode:
- Jika index belum ada, aplikasi akan:
  1. Menggunakan query sederhana (tanpa orderBy)
  2. Filter dan sort di client side (di aplikasi)
  3. Aplikasi **TIDAK AKAN CRASH** lagi

**Tapi:** Untuk performa optimal, tetap buat index di Firestore.

## 🎯 Langkah Setelah Index Dibuat

1. **Tunggu index selesai** (status: "Enabled" di Firebase Console)

2. **Restart aplikasi** (tutup dan buka lagi)

3. **Test fitur:**
   - Filter by type (Hilang/Ditemukan)
   - Lihat laporan di Beranda
   - Lihat laporan di Aktivitas

4. Error seharusnya **tidak muncul lagi**

## ⚠️ Catatan Penting

- **Index creation GRATIS** (termasuk di free tier)
- **Tidak ada limit** untuk jumlah index
- **Index dibuat otomatis** jika klik link dari error
- **Waktu pembuatan:** 1-5 menit (tergantung jumlah data)

## 🔍 Verifikasi Index

1. Buka Firebase Console → **Firestore Database** → **Indexes**
2. Pastikan ada **3 index** dengan status **"Enabled"**
3. Jika masih "Building", tunggu sampai selesai

---

**Setelah index dibuat, aplikasi akan berjalan dengan lancar tanpa error!** 🎉

