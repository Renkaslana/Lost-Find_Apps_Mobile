package com.campus.lostfound.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campus.lostfound.data.model.ItemType
import com.campus.lostfound.data.model.LostFoundItem
import com.campus.lostfound.data.repository.LostFoundRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val context: Context,
    private val repository: LostFoundRepository = LostFoundRepository(context)
) : ViewModel() {
    
    private val _selectedFilter = MutableStateFlow<ItemType?>(null)
    val selectedFilter: StateFlow<ItemType?> = _selectedFilter.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _items = MutableStateFlow<List<LostFoundItem>>(emptyList())
    val items: StateFlow<List<LostFoundItem>> = _items.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private var currentJob: kotlinx.coroutines.Job? = null
    
    val filteredItems: StateFlow<List<LostFoundItem>> = combine(
        _items,
        _selectedFilter,
        _searchQuery
    ) { items, filter, query ->
        val filtered = if (filter != null) {
            items.filter { it.type == filter }
        } else {
            items
        }
        
        if (query.isBlank()) {
            filtered
        } else {
            val lowerQuery = query.lowercase()
            filtered.filter {
                it.itemName.lowercase().contains(lowerQuery) ||
                it.location.lowercase().contains(lowerQuery) ||
                it.description.lowercase().contains(lowerQuery)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    init {
        loadItems()
    }
    
    fun loadItems() {
        // Cancel previous job jika ada
        currentJob?.cancel()
        
        currentJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getAllItems(_selectedFilter.value).collect { itemsList ->
                    _items.value = itemsList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
                // Log error jika perlu
                e.printStackTrace()
            }
        }
    }
    
    fun setFilter(type: ItemType?) {
        _selectedFilter.value = type
        loadItems()
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

