package com.campus.lostfound.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.campus.lostfound.ui.viewmodel.NotificationViewModel
import com.campus.lostfound.util.WhatsAppUtil
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToDetail: ((String) -> Unit)? = null
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
        onNavigateToAdd = onNavigateToAdd,
        onNavigateToNotifications = onNavigateToNotifications,
        onNavigateToDetail = onNavigateToDetail
    )
}

@Composable
private fun HomeScreenContent(
    context: android.content.Context,
    viewModel: HomeViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToDetail: ((String) -> Unit)? = null
) {
    val items by viewModel.filteredItems.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    // Notification ViewModel untuk unread count
    val notificationViewModel: NotificationViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NotificationViewModel(context) as T
            }
        }
    )
    val notifications by notificationViewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount = notifications.count { !it.read }
    val hasUnreadNotifications = unreadCount > 0
    
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Modern Header with Gradient Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Bar with Title and Notification Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Laporan Terbaru di Kampus",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Temukan atau laporkan barang hilang",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                    }
                    
                    // Notification Icon - TANPA background, hanya ripple
                    Box {
                        IconButton(
                            onClick = { onNavigateToNotifications() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Filled.Notifications, 
                                contentDescription = "Notifikasi",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // Badge dot jika ada notifikasi baru
                        if (hasUnreadNotifications) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(12.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.error,
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
                
                // Modern Search Bar
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, MaterialTheme.shapes.large),
                    placeholder = { 
                        Text(
                            "Cari barang (tas, HP, kunci…)",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search, 
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Modern Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { 
                    viewModel.setFilter(null) 
                },
                label = { 
                    Text(
                        "Semua",
                        fontWeight = if (selectedFilter == null) FontWeight.Bold else FontWeight.Normal
                    )
                },
                leadingIcon = if (selectedFilter == null) {
                    {
                        Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
            
            FilterChip(
                selected = selectedFilter == ItemType.LOST,
                onClick = { 
                    viewModel.setFilter(ItemType.LOST) 
                },
                label = { 
                    Text(
                        "Hilang",
                        fontWeight = if (selectedFilter == ItemType.LOST) FontWeight.Bold else FontWeight.Normal
                    )
                },
                leadingIcon = if (selectedFilter == ItemType.LOST) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.error,
                    selectedLabelColor = MaterialTheme.colorScheme.onError
                )
            )
            
            FilterChip(
                selected = selectedFilter == ItemType.FOUND,
                onClick = { 
                    viewModel.setFilter(ItemType.FOUND) 
                },
                label = { 
                    Text(
                        "Ditemukan",
                        fontWeight = if (selectedFilter == ItemType.FOUND) FontWeight.Bold else FontWeight.Normal
                    )
                },
                leadingIcon = if (selectedFilter == ItemType.FOUND) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = com.campus.lostfound.ui.theme.FoundGreen,
                    selectedLabelColor = androidx.compose.ui.graphics.Color.White
                )
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
                itemsIndexed(items) { index, item ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(300, delayMillis = index * 50)) +
                                slideInVertically(
                                    initialOffsetY = { it / 2 },
                                    animationSpec = tween(300, delayMillis = index * 50)
                                )
                    ) {
                        ItemCard(
                            item = item,
                            onContactClick = {
                                WhatsAppUtil.openWhatsApp(
                                    context = context,
                                    phoneNumber = item.whatsappNumber,
                                    itemName = item.itemName,
                                    type = if (item.type == ItemType.LOST) "barang hilang" else "barang ditemukan"
                                )
                            },
                            onCardClick = {
                                onNavigateToDetail?.invoke(item.id)
                            }
                        )
                    }
                }
            }
        }
    }
    
}

