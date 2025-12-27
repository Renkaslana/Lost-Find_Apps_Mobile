package com.campus.lostfound.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class LostFoundItem(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val type: ItemType = ItemType.LOST,
    val itemName: String = "",
    val category: Category = Category.OTHER,
    val location: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val whatsappNumber: String = "",
    @PropertyName("completed")
    val isCompleted: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val imageStoragePath: String = ""
) {
    fun getTimeAgo(): String {
        val now = System.currentTimeMillis()
        val created = createdAt.toDate().time
        val diff = now - created
        
        return when {
            diff < 60000 -> "Baru saja"
            diff < 3600000 -> "${diff / 60000} menit lalu"
            diff < 86400000 -> "${diff / 3600000} jam lalu"
            diff < 604800000 -> "${diff / 86400000} hari lalu"
            else -> "${diff / 604800000} minggu lalu"
        }
    }
}

enum class ItemType {
    LOST, FOUND
}

enum class Category(val displayName: String) {
    ELECTRONICS("Elektronik"),
    ACCESSORIES("Aksesoris"),
    DOCUMENTS("Dokumen"),
    OTHER("Lainnya")
}

