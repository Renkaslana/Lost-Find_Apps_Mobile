package com.campus.lostfound.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campus.lostfound.data.model.ItemType
import com.campus.lostfound.ui.components.ItemCard
import com.campus.lostfound.ui.viewmodel.HomeViewModel
import com.campus.lostfound.util.WhatsAppUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAdd: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(context) as T
            }
        }
    )
    
    HomeScreenContent(
        context = context,
        viewModel = viewModel,
        onNavigateToAdd = onNavigateToAdd
    )
}

@Composable
private fun HomeScreenContent(
    context: android.content.Context,
    viewModel: HomeViewModel,
    onNavigateToAdd: () -> Unit
) {
    val items by viewModel.filteredItems.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Laporan Terbaru di Kampus",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Temukan atau laporkan barang hilang",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Cari barang (tas, HP, kunci…)") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SuggestionChip(
                onClick = { viewModel.setFilter(null) },
                label = { Text("Semua") },
                colors = if (selectedFilter == null) {
                    SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                } else {
                    SuggestionChipDefaults.suggestionChipColors()
                }
            )
            SuggestionChip(
                onClick = { viewModel.setFilter(ItemType.LOST) },
                label = { Text("Hilang") },
                colors = if (selectedFilter == ItemType.LOST) {
                    SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                } else {
                    SuggestionChipDefaults.suggestionChipColors()
                }
            )
            SuggestionChip(
                onClick = { viewModel.setFilter(ItemType.FOUND) },
                label = { Text("Ditemukan") },
                colors = if (selectedFilter == ItemType.FOUND) {
                    SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                } else {
                    SuggestionChipDefaults.suggestionChipColors()
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Items List
        if (isLoading && items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Belum ada laporan",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Jadilah yang pertama melaporkan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(items) { item ->
                    ItemCard(
                        item = item,
                        onContactClick = {
                            WhatsAppUtil.openWhatsApp(
                                context = context,
                                phoneNumber = item.whatsappNumber,
                                itemName = item.itemName,
                                type = if (item.type == ItemType.LOST) "barang hilang" else "barang ditemukan"
                            )
                        }
                    )
                }
            }
        }
    }
}

