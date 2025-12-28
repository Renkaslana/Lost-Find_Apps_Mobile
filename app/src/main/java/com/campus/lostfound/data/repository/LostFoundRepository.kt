package com.campus.lostfound.data.repository

import android.content.Context
import android.net.Uri
import com.campus.lostfound.data.model.LostFoundItem
import com.campus.lostfound.data.model.ItemType
import com.campus.lostfound.util.ImageConverter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.Timestamp
import java.util.Date
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import android.util.Log
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.util.UUID

class LostFoundRepository(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: run {
            // Create anonymous user if not exists
            auth.signInAnonymously().addOnCompleteListener { }
            auth.currentUser?.uid ?: UUID.randomUUID().toString()
        }
    }

    private suspend fun ensureAuthenticated(): String {
        auth.currentUser?.let { return it.uid }

        return suspendCancellableCoroutine { cont ->
            val task = auth.signInAnonymously()
            val listener = { completedTask: com.google.android.gms.tasks.Task<com.google.firebase.auth.AuthResult> ->
                if (completedTask.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) cont.resume(uid) else cont.resumeWithException(Exception("Failed to obtain uid after anonymous sign-in"))
                } else {
                    cont.resumeWithException(completedTask.exception ?: Exception("Anonymous sign-in failed"))
                }
            }

            task.addOnCompleteListener(listener)

            cont.invokeOnCancellation {
                // no-op: listener will be GC'ed
            }
        }
    }
    
    fun getAllItems(type: ItemType? = null): Flow<List<LostFoundItem>> = callbackFlow {
        // Ensure user is authenticated before attaching listeners to avoid immediate PERMISSION_DENIED
        try {
            ensureAuthenticated()
        } catch (ex: Exception) {
            Log.e("LostFoundRepo", "Auth failed before listening: ${ex.message}")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val collection = firestore.collection("items")

        // Gunakan query sederhana dulu (tanpa filter type di query)
        // Filter type akan dilakukan di client side untuk menghindari index requirement
        // Note: Field name di Firestore adalah "completed" (bukan "isCompleted")
        val baseQuery = collection.whereEqualTo("completed", false)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val listenerRegistration = baseQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("LostFoundRepo", "Query error: ${error.message}")
                // Jika error, coba query tanpa orderBy
                if (error.message?.contains("index") == true ||
                    error.message?.contains("FAILED_PRECONDITION") == true) {

                    Log.d("LostFoundRepo", "Using fallback query without orderBy")
                    // Fallback: query tanpa orderBy
                    // Note: Field name di Firestore adalah "completed"
                    collection.whereEqualTo("completed", false)
                        .addSnapshotListener { fallbackSnapshot, fallbackError ->
                            if (fallbackError == null) {
                                val allItems = fallbackSnapshot?.documents?.mapNotNull { doc ->
                                    doc.toObject(LostFoundItem::class.java)?.copy(id = doc.id)
                                } ?: emptyList()

                                Log.d("LostFoundRepo", "Fallback query returned ${allItems.size} items")

                                // Filter dan sort di client side
                                val filtered = if (type != null) {
                                    allItems.filter { it.type == type }
                                } else {
                                    allItems
                                }

                                // Sort by createdAt descending
                                val sorted = filtered.sortedByDescending {
                                    it.createdAt.toDate().time
                                }

                                trySend(sorted)
                            } else {
                                Log.e("LostFoundRepo", "Fallback query also failed: ${fallbackError.message}")
                                // Jika masih error, kirim empty list but don't close the app
                                trySend(emptyList())
                            }
                        }
                    return@addSnapshotListener
                }
                // Error lain, emit empty list instead of closing flow to avoid crashing
                trySend(emptyList())
                return@addSnapshotListener
            }

            val items = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(LostFoundItem::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            Log.d("LostFoundRepo", "Query returned ${items.size} items, filter type: $type")

            // Filter by type di client side
            val filtered = if (type != null) {
                items.filter { it.type == type }
            } else {
                items
            }

            Log.d("LostFoundRepo", "After filtering: ${filtered.size} items")
            trySend(filtered)
        }

        awaitClose { listenerRegistration.remove() }
    }
    
    fun getUserItems(userId: String): Flow<List<LostFoundItem>> = callbackFlow {
        // Ensure authenticated before listening
        try {
            ensureAuthenticated()
        } catch (ex: Exception) {
            Log.e("LostFoundRepo", "Auth failed before userItems listen: ${ex.message}")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val query = firestore.collection("items")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Jika error karena index belum ada, gunakan fallback
                if (error.message?.contains("index") == true ||
                    error.message?.contains("FAILED_PRECONDITION") == true) {

                    // Fallback: query tanpa orderBy, sort di client
                    firestore.collection("items")
                        .whereEqualTo("userId", userId)
                        .addSnapshotListener { fallbackSnapshot, fallbackError ->
                            if (fallbackError == null) {
                                val allItems = fallbackSnapshot?.documents?.mapNotNull { doc ->
                                    doc.toObject(LostFoundItem::class.java)?.copy(id = doc.id)
                                } ?: emptyList()

                                // Sort di client side
                                val sorted = allItems.sortedByDescending {
                                    it.createdAt.toDate().time
                                }

                                trySend(sorted)
                            } else {
                                trySend(emptyList())
                            }
                        }
                    return@addSnapshotListener
                }
                Log.e("LostFoundRepo", "UserItems query error: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }

            val items = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(LostFoundItem::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            trySend(items)
        }

        awaitClose { listenerRegistration.remove() }
    }

    fun getUserHistory(userId: String): Flow<List<LostFoundItem>> = callbackFlow {
        // Ensure authenticated before listening to private history
        try {
            ensureAuthenticated()
        } catch (ex: Exception) {
            Log.e("LostFoundRepo", "Auth failed before history listen: ${ex.message}")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val query = firestore.collection("users").document(userId)
            .collection("history")
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // fallback: return empty list on error but don't close
                Log.e("LostFoundRepo", "History query error: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }

            val items = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(LostFoundItem::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            trySend(items)
        }

        awaitClose { listenerRegistration.remove() }
    }
    
    suspend fun addItem(item: LostFoundItem, imageUri: Uri?): Result<String> {
        return try {
            val userId = ensureAuthenticated()
            val itemWithUser = item.copy(userId = userId)
            
            // Convert image to Base64 if provided (opsional)
            val imageUrl = if (imageUri != null) {
                withContext(Dispatchers.IO) {
                    ImageConverter.uriToBase64(imageUri, context)
                }
            } else {
                ""
            }
            
            val finalItem = itemWithUser.copy(
                imageUrl = imageUrl,
                imageStoragePath = "", // Tidak perlu untuk Base64
                isCompleted = false // Pastikan false (akan disimpan sebagai "completed" di Firestore)
            )
            
            val docRef = firestore.collection("items").add(finalItem).await()
            android.util.Log.d("LostFoundRepo", "Item saved with ID: ${docRef.id}")

            // TEMPORARY: write a persistent global notification from client-side so UI shows it immediately.
            // This is a temporary measure until server-side Cloud Function is available.
            // Safety: simple rate-limit and minimal fields. Revert this when Cloud Functions are deployed.
            try {
                val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                val lastNotif = prefs.getLong("last_notif_ts", 0L)
                val now = System.currentTimeMillis()
                val rateLimitMs = 30_000L // 30 seconds per device

                if (now - lastNotif >= rateLimitMs) {
                    val expireAtMillis = now + 7L * 24 * 60 * 60 * 1000 // 7 days

                    val fmt = java.text.SimpleDateFormat("d MMM yyyy, HH:mm", java.util.Locale("id", "ID"))
                    val createdAtStr = fmt.format(java.util.Date())
                    val notif = hashMapOf<String, Any?>(
                        "title" to ("Laporan Baru: ${finalItem.itemName ?: "Barang"}"),
                        "body" to ("Laporan untuk \"${finalItem.itemName ?: "barang"}\" dibuat pada $createdAtStr"),
                        "timestamp" to FieldValue.serverTimestamp(),
                        "expireAt" to Timestamp(Date(expireAtMillis)),
                        "data" to hashMapOf("itemId" to docRef.id, "type" to "NEW_REPORT"),
                        "userId" to userId
                    )

                    firestore.collection("global_notifications")
                        .add(notif)
                        .addOnSuccessListener {
                            prefs.edit().putLong("last_notif_ts", now).apply()
                            android.util.Log.d("LostFoundRepo", "Notification written: ${it.id}")
                        }
                        .addOnFailureListener { e ->
                            android.util.Log.e("LostFoundRepo", "Failed to write notification: ${e.message}")
                        }
                } else {
                    android.util.Log.d("LostFoundRepo", "Notification rate-limited (skip write)")
                }
            } catch (ex: Exception) {
                android.util.Log.e("LostFoundRepo", "Notification write error: ${ex.message}")
            }

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteItem(itemId: String, imageStoragePath: String): Result<Unit> {
        return try {
            val userId = ensureAuthenticated()
            
            // Verify ownership
            val item = firestore.collection("items").document(itemId).get().await()
                .toObject(LostFoundItem::class.java)
            
            if (item == null) {
                return Result.failure(Exception("Laporan tidak ditemukan"))
            }
            
            if (item.userId != userId) {
                android.util.Log.w("LostFoundRepo", "Delete attempt by unauthorized user. Item userId: ${item.userId}, Current userId: $userId")
                return Result.failure(Exception("Anda tidak memiliki izin untuk menghapus laporan ini"))
            }
            
            // Note: Base64 images are stored in Firestore document
            // They will be automatically deleted when document is deleted
            // No need to delete separately
            
            // Delete document (image akan ikut terhapus)
            firestore.collection("items").document(itemId).delete().await()
            android.util.Log.d("LostFoundRepo", "Item deleted successfully: $itemId")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("LostFoundRepo", "Error deleting item: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun markAsCompleted(itemId: String): Result<Unit> {
        return try {
            val userId = ensureAuthenticated()

            // Verify ownership
            val itemDoc = firestore.collection("items").document(itemId).get().await()
            val item = itemDoc.toObject(LostFoundItem::class.java)

            if (item?.userId != userId) {
                return Result.failure(Exception("Anda tidak memiliki izin untuk mengubah laporan ini"))
            }

            // Archive to user's private history then remove from public collection
            val archiveRef = firestore.collection("users").document(userId)
                .collection("history").document(itemId)

            val archivedItem = item.copy(isCompleted = true)

            archiveRef.set(archivedItem).await()
            // TEMPORARY: write a persistent global notification so UI shows completion
            try {
                val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                val lastNotif = prefs.getLong("last_notif_ts", 0L)
                val now = System.currentTimeMillis()
                val rateLimitMs = 30_000L

                if (now - lastNotif >= rateLimitMs) {
                    val expireAtMillis = now + 7L * 24 * 60 * 60 * 1000
                    val fmt = java.text.SimpleDateFormat("d MMM yyyy, HH:mm", java.util.Locale("id", "ID"))
                    val completedAtStr = fmt.format(java.util.Date())
                    val notif = hashMapOf<String, Any?>(
                        "title" to ("Laporan Selesai: ${item.itemName ?: "Barang"}"),
                        "body" to ("Laporan untuk \"${item.itemName ?: "barang"}\" telah diselesaikan pada $completedAtStr"),
                        "timestamp" to FieldValue.serverTimestamp(),
                        "expireAt" to Timestamp(Date(expireAtMillis)),
                        "data" to hashMapOf("itemId" to itemId, "type" to "REPORT_COMPLETED"),
                        "userId" to userId
                    )

                    firestore.collection("global_notifications")
                        .add(notif)
                        .addOnSuccessListener {
                            prefs.edit().putLong("last_notif_ts", now).apply()
                            android.util.Log.d("LostFoundRepo", "Completion notification written: ${it.id}")
                        }
                        .addOnFailureListener { e ->
                            android.util.Log.e("LostFoundRepo", "Failed to write completion notification: ${e.message}")
                        }
                } else {
                    android.util.Log.d("LostFoundRepo", "Completion notification rate-limited (skip write)")
                }
            } catch (ex: Exception) {
                android.util.Log.e("LostFoundRepo", "Completion notification error: ${ex.message}")
            }

            firestore.collection("items").document(itemId).delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getItemById(itemId: String): Result<LostFoundItem> {
        return try {
            val doc = firestore.collection("items").document(itemId).get().await()
            val item = doc.toObject(LostFoundItem::class.java)?.copy(id = doc.id)
            if (item != null) {
                Result.success(item)
            } else {
                Result.failure(Exception("Laporan tidak ditemukan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateItem(
        itemId: String,
        itemName: String? = null,
        category: com.campus.lostfound.data.model.Category? = null,
        location: String? = null,
        description: String? = null,
        whatsappNumber: String? = null,
        imageUri: Uri? = null
    ): Result<Unit> {
        return try {
            val userId = ensureAuthenticated()
            
            // Verify ownership
            val existingItem = firestore.collection("items").document(itemId).get().await()
                .toObject(LostFoundItem::class.java)
            
            if (existingItem?.userId != userId) {
                return Result.failure(Exception("Anda tidak memiliki izin untuk mengubah laporan ini"))
            }
            
            val updates = mutableMapOf<String, Any>()
            
            if (itemName != null) updates["itemName"] = itemName
            if (category != null) updates["category"] = category.name
            if (location != null) updates["location"] = location
            if (description != null) updates["description"] = description
            if (whatsappNumber != null) {
                // Format nomor ke format internasional
                val formattedNumber = com.campus.lostfound.util.WhatsAppUtil.formatPhoneNumber(whatsappNumber)
                updates["whatsappNumber"] = formattedNumber
            }
            
            // Update image jika ada
            if (imageUri != null) {
                val imageUrl = withContext(Dispatchers.IO) {
                    ImageConverter.uriToBase64(imageUri, context)
                }
                if (imageUrl.isNotEmpty()) {
                    updates["imageUrl"] = imageUrl
                }
            }
            
            if (updates.isNotEmpty()) {
                firestore.collection("items").document(itemId)
                    .update(updates).await()
                android.util.Log.d("LostFoundRepo", "Item updated: $itemId")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("LostFoundRepo", "Error updating item: ${e.message}")
            Result.failure(e)
        }
    }
}

