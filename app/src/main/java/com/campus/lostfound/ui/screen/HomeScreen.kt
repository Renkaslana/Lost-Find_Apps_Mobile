package com.campus.lostfound.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.campus.lostfound.ui.theme.LostRed
import com.campus.lostfound.ui.theme.FoundGreen
import com.campus.lostfound.util.WhatsAppUtil
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically

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
    
    // Notification ViewModel untuk unread count (local storage)
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
    
    val listState = rememberLazyListState()
    val headerCollapsed by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 50 }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Premium Header with Sophisticated Gradient & Spacing
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                        ),
                        startY = 0f,
                        endY = 400f // Longer gradient for sophistication
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp), // Increased padding for premium feel
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Top Bar - Enhanced with better spacing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Lost & Found Kampus",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-0.5).sp // Tighter letter spacing for premium look
                        )
                        Text(
                            text = "Temukan atau laporkan barang hilang dengan mudah",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            letterSpacing = 0.sp
                        )
                    }
                    
                    // Premium Notification Icon with enhanced styling
                    Box(
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        val bellInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        val bellPressed by bellInteraction.collectIsPressedAsState()
                        val bellScale by animateFloatAsState(
                            targetValue = if (bellPressed) 0.9f else 1f,
                            animationSpec = tween(100)
                        )

                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .scale(bellScale),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.15f),
                            onClick = { onNavigateToNotifications() },
                            interactionSource = bellInteraction
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Notifications,
                                    contentDescription = "Notifikasi",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Enhanced notification badge with better positioning and count display
                        if (hasUnreadNotifications) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp),
                                shape = CircleShape,
                                color = LostRed,
                                shadowElevation = 2.dp
                            ) {
                                Box(
                                    modifier = Modifier
                                        .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                                        .padding(horizontal = 5.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Premium Search Bar with enhanced styling
                var searchFocused by remember { mutableStateOf(false) }
                val searchElevation by animateFloatAsState(
                    targetValue = if (searchFocused) 12f else 4f, // Higher elevation for premium
                    animationSpec = tween(300)
                )
                val searchScale by animateFloatAsState(
                    targetValue = if (searchFocused) 1.02f else 1f,
                    animationSpec = tween(300)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(searchScale),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = searchElevation.dp,
                    color = Color.White
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Cari barang hilang atau ditemukan...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = if (searchQuery.isNotBlank()) {
                            {
                                Surface(
                                    modifier = Modifier.size(32.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    onClick = { viewModel.setSearchQuery("") }
                                ) {
                                    Icon(
                                        Icons.Filled.Close, 
                                        contentDescription = "Clear",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .padding(2.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else null,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Premium Filter Chips with enhanced styling
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Semua Filter
            FilterChip(
                selected = selectedFilter == null,
                onClick = { viewModel.setFilter(null) },
                label = { 
                    Text(
                        "Semua",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selectedFilter == null) FontWeight.SemiBold else FontWeight.Medium
                    )
                },
                leadingIcon = if (selectedFilter == null) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null,
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == null,
                    borderColor = if (selectedFilter == null) Color.Transparent else MaterialTheme.colorScheme.outline,
                    selectedBorderColor = Color.Transparent,
                    borderWidth = 1.dp
                )
            )
            
            // Hilang Filter
            FilterChip(
                selected = selectedFilter == ItemType.LOST,
                onClick = { viewModel.setFilter(ItemType.LOST) },
                label = { 
                    Text(
                        "Hilang",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selectedFilter == ItemType.LOST) FontWeight.SemiBold else FontWeight.Medium
                    )
                },
                leadingIcon = if (selectedFilter == ItemType.LOST) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null,
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = LostRed,
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == ItemType.LOST,
                    borderColor = if (selectedFilter == ItemType.LOST) Color.Transparent else MaterialTheme.colorScheme.outline,
                    selectedBorderColor = Color.Transparent,
                    borderWidth = 1.dp
                )
            )
            
            // Ditemukan Filter
            FilterChip(
                selected = selectedFilter == ItemType.FOUND,
                onClick = { viewModel.setFilter(ItemType.FOUND) },
                label = { 
                    Text(
                        "Ditemukan",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selectedFilter == ItemType.FOUND) FontWeight.SemiBold else FontWeight.Medium
                    )
                },
                leadingIcon = if (selectedFilter == ItemType.FOUND) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null,
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = FoundGreen,
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == ItemType.FOUND,
                    borderColor = if (selectedFilter == ItemType.FOUND) Color.Transparent else MaterialTheme.colorScheme.outline,
                    selectedBorderColor = Color.Transparent,
                    borderWidth = 1.dp
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                com.campus.lostfound.ui.components.EmptyStateIllustration.EmptyStateHomeIllustration()
            }
        } else {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
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

