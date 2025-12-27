package com.campus.lostfound.data.repository

import android.content.Context
import android.net.Uri
import com.campus.lostfound.data.model.LostFoundItem
import com.campus.lostfound.data.model.ItemType
import com.campus.lostfound.util.ImageConverter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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
    
    fun getAllItems(type: ItemType? = null): Flow<List<LostFoundItem>> = callbackFlow {
        val collection = firestore.collection("items")
        
        // Gunakan query sederhana dulu (tanpa filter type di query)
        // Filter type akan dilakukan di client side untuk menghindari index requirement
        // Note: Field name di Firestore adalah "completed" (bukan "isCompleted")
        val baseQuery = collection.whereEqualTo("completed", false)
            .orderBy("createdAt", Query.Direction.DESCENDING)
        
        val listenerRegistration = baseQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.e("LostFoundRepo", "Query error: ${error.message}")
                // Jika error, coba query tanpa orderBy
                if (error.message?.contains("index") == true || 
                    error.message?.contains("FAILED_PRECONDITION") == true) {
                    
                    android.util.Log.d("LostFoundRepo", "Using fallback query without orderBy")
                    // Fallback: query tanpa orderBy
                    // Note: Field name di Firestore adalah "completed"
                    collection.whereEqualTo("completed", false)
                        .addSnapshotListener { fallbackSnapshot, fallbackError ->
                            if (fallbackError == null) {
                                val allItems = fallbackSnapshot?.documents?.mapNotNull { doc ->
                                    doc.toObject(LostFoundItem::class.java)?.copy(id = doc.id)
                                } ?: emptyList()
                                
                                android.util.Log.d("LostFoundRepo", "Fallback query returned ${allItems.size} items")
                                
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
                                android.util.Log.e("LostFoundRepo", "Fallback query also failed: ${fallbackError.message}")
                                // Jika masih error, kirim empty list
                                trySend(emptyList())
                            }
                        }
                    return@addSnapshotListener
                }
                // Error lain, tutup flow
                close(error)
                return@addSnapshotListener
            }
            
            val items = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(LostFoundItem::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            
            android.util.Log.d("LostFoundRepo", "Query returned ${items.size} items, filter type: $type")
            
            // Filter by type di client side
            val filtered = if (type != null) {
                items.filter { it.type == type }
            } else {
                items
            }
            
            android.util.Log.d("LostFoundRepo", "After filtering: ${filtered.size} items")
            trySend(filtered)
        }
        
        awaitClose { listenerRegistration.remove() }
    }
    
    fun getUserItems(userId: String): Flow<List<LostFoundItem>> = callbackFlow {
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
                close(error)
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
            val userId = getCurrentUserId()
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
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteItem(itemId: String, imageStoragePath: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            
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
            val userId = getCurrentUserId()
            
            // Verify ownership
            val item = firestore.collection("items").document(itemId).get().await()
                .toObject(LostFoundItem::class.java)
            
            if (item?.userId != userId) {
                return Result.failure(Exception("Anda tidak memiliki izin untuk mengubah laporan ini"))
            }
            
            firestore.collection("items").document(itemId)
                .update("completed", true).await() // Field name di Firestore adalah "completed"
            Result.success(Unit)
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
            val userId = getCurrentUserId()
            
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

