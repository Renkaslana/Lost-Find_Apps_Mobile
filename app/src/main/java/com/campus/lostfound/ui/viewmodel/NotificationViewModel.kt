package com.campus.lostfound.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campus.lostfound.data.model.NotificationItem
import com.campus.lostfound.data.model.NotificationType
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

class NotificationViewModel(private val context: Context) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val prefs: SharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // load cached notifications first (so new installs see nothing if none cached)
        loadCachedNotifications()
        loadNotifications()
    }

    private fun loadCachedNotifications() {
        try {
            val json = prefs.getString("cached_notifications", "") ?: ""
            if (json.isNotBlank()) {
                val arr = JSONArray(json)
                val list = mutableListOf<NotificationItem>()
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    NotificationItem.fromJsonObject(obj)?.let { list.add(it) }
                }
                // Map read flags using lastSeen + per-device read ids
                val mapped = list.map { item ->
                    item.copy(read = isNotificationRead(item.id, item.timestamp))
                }
                _notifications.value = mapped
            }
        } catch (ex: Exception) {
            Log.e("NotificationVM", "Failed to load cached notifications: ${ex.message}")
        }
    }

    private fun saveCachedNotifications(list: List<NotificationItem>) {
        try {
            val arr = JSONArray()
            list.forEach { arr.put(it.toJsonObject()) }
            prefs.edit().putString("cached_notifications", arr.toString()).apply()
        } catch (ex: Exception) {
            Log.e("NotificationVM", "Failed to save cached notifications: ${ex.message}")
        }
    }

    private fun getLastSeen(): Long {
        return prefs.getLong("lastSeen", 0L)
    }

    private fun setLastSeen(value: Long) {
        prefs.edit().putLong("lastSeen", value).apply()
    }

    private fun getReadIdSet(): MutableSet<String> {
        return prefs.getStringSet("read_notification_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
    }

    private fun setReadIdSet(set: Set<String>) {
        prefs.edit().putStringSet("read_notification_ids", set).apply()
    }

    private fun isNotificationRead(id: String, ts: Timestamp): Boolean {
        val lastSeen = getLastSeen()
        if (ts.toDate().time <= lastSeen) return true
        val readSet = getReadIdSet()
        return readSet.contains(id)
    }

    private fun loadNotifications() {
        _isLoading.value = true
        _errorMessage.value = null

        // Listen to global_notifications (server/Cloud Functions should write here)
        try {
            firestore.collection("global_notifications")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("NotificationVM", "Notification query error: ${error.message}")
                        _errorMessage.value = error.message
                        _isLoading.value = false
                        return@addSnapshotListener
                    }

                    val docs = snapshot?.documents ?: emptyList()
                    val items = docs.mapNotNull { doc ->
                        try {
                            val ni = doc.toObject(NotificationItem::class.java)?.copy(id = doc.id)
                            ni
                        } catch (ex: Exception) {
                            null
                        }
                    }

                    // Map read flag using lastSeen + per-device read ids
                    val mapped = items.map { item ->
                        item.copy(read = isNotificationRead(item.id, item.timestamp))
                    }

                    _notifications.value = mapped
                    // persist a local cache for offline / history (survives server TTL)
                    saveCachedNotifications(mapped)
                    _isLoading.value = false
                }
        } catch (ex: Exception) {
            Log.e("NotificationVM", "Failed to attach notifications listener", ex)
            _errorMessage.value = ex.message
            _isLoading.value = false
        }
    }

    fun markAsRead(notificationId: String) {
        // Mark a single notification as read for this device
        viewModelScope.launch {
            val readSet = getReadIdSet()
            if (!readSet.contains(notificationId)) {
                readSet.add(notificationId)
                setReadIdSet(readSet)
            }

            // update local state
            val updated = _notifications.value.map { n ->
                if (n.id == notificationId) n.copy(read = true) else n
            }
            _notifications.value = updated
            saveCachedNotifications(updated)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            // Set lastSeen to now and clear individual read ids
            setLastSeen(System.currentTimeMillis())
            setReadIdSet(emptySet())

            val updated = _notifications.value.map { it.copy(read = true) }
            _notifications.value = updated
            saveCachedNotifications(updated)
        }
    }

    fun getUnreadCount(): Int {
        return _notifications.value.count { !it.read }
    }
}

