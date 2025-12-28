package com.campus.lostfound.ui.screen

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.campus.lostfound.data.model.ItemType
import com.campus.lostfound.data.model.LostFoundItem
import com.campus.lostfound.data.repository.LostFoundRepository
import com.campus.lostfound.ui.theme.FoundGreen
import com.campus.lostfound.ui.theme.FoundGreenLight
import com.campus.lostfound.ui.theme.LostRed
import com.campus.lostfound.ui.theme.LostRedLight
import com.campus.lostfound.util.ImageConverter
import com.campus.lostfound.util.WhatsAppUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import kotlin.Result

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    itemId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val viewModel: DetailViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DetailViewModel(context, itemId) as T
            }
        }
    )
    
    val item by viewModel.item.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isOwner by viewModel.isOwner.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Laporan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            // Sticky CTA Button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isOwner) {
                        // Owner actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onNavigateToEdit?.invoke(itemId) },
                                modifier = Modifier.weight(1f),
                                enabled = !(item?.isCompleted ?: false)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit")
                            }
                            
                            OutlinedButton(
                                onClick = { showCompleteDialog = true },
                                modifier = Modifier.weight(1f),
                                enabled = !(item?.isCompleted ?: false)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Selesai")
                            }
                            
                            OutlinedButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Hapus")
                            }
                        }
                    }
                    
                    // Contact Button
                    Button(
                        onClick = {
                            item?.let {
                                WhatsAppUtil.openWhatsApp(
                                    context = context,
                                    phoneNumber = it.whatsappNumber,
                                    itemName = it.itemName,
                                    type = if (it.type == ItemType.LOST) "barang hilang" else "barang ditemukan"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = item != null
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hubungi via WhatsApp", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = errorMessage ?: "Terjadi kesalahan",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        } else if (item != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
            ) {
                // Hero Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    if (item!!.imageUrl.isNotEmpty()) {
                        if (ImageConverter.isBase64Image(item!!.imageUrl)) {
                            val bitmapResult = remember(item!!.imageUrl) {
                                runCatching {
                                    val base64String = ImageConverter.extractBase64(item!!.imageUrl)
                                    val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                }
                            }
                            
                            bitmapResult.getOrNull()?.let { bitmap ->
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = item!!.itemName,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } ?: ImagePlaceholder(modifier = Modifier.fillMaxSize())
                        } else {
                            Image(
                                painter = rememberAsyncImagePainter(item!!.imageUrl),
                                contentDescription = item!!.itemName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        ImagePlaceholder(modifier = Modifier.fillMaxSize())
                    }
                    
                    // Badge Status di atas hero image
                    Surface(
                        color = if (item!!.type == ItemType.LOST) LostRedLight else FoundGreenLight,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (item!!.type == ItemType.LOST) "Hilang" else "Ditemukan",
                            color = if (item!!.type == ItemType.LOST) LostRed else FoundGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                
                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Item Name (Headline)
                    Text(
                        text = item!!.itemName,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Info Grid
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            InfoRow(
                                icon = Icons.Default.Category,
                                label = "Kategori",
                                value = item!!.category.displayName
                            )
                            
                            Divider()
                            
                            InfoRow(
                                icon = Icons.Default.LocationOn,
                                label = "Lokasi",
                                value = item!!.location
                            )
                            
                            Divider()
                            
                            InfoRow(
                                icon = Icons.Default.Schedule,
                                label = "Waktu",
                                value = item!!.getTimeAgo()
                            )
                        }
                    }
                    
                    // Description
                    if (item!!.description.isNotEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Deskripsi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item!!.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Completed Status
                    if (item!!.isCompleted) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Laporan ini telah ditandai selesai",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Laporan?") },
            text = {
                Text("Apakah Anda yakin ingin menghapus laporan \"${item?.itemName}\"? Tindakan ini tidak dapat dibatalkan.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteItem {
                            showDeleteDialog = false
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
    
    // Complete Confirmation Dialog
    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Tandai Selesai?") },
            text = {
                Text("Apakah Anda yakin ingin menandai laporan \"${item?.itemName}\" sebagai selesai?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.markAsCompleted {
                            showCompleteDialog = false
                        }
                    }
                ) {
                    Text("Ya, Tandai Selesai")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ImagePlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Image,
            contentDescription = "No image",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

class DetailViewModel(
    private val context: android.content.Context,
    private val itemId: String
) : ViewModel() {
    private val repository = LostFoundRepository(context)
    
    private val _item = MutableStateFlow<LostFoundItem?>(null)
    val item: StateFlow<LostFoundItem?> = _item.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isOwner = MutableStateFlow(false)
    val isOwner: StateFlow<Boolean> = _isOwner.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        loadItem()
    }
    
    private fun loadItem() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = repository.getItemById(itemId)
            result.onSuccess { loadedItem ->
                _item.value = loadedItem
                _isOwner.value = loadedItem.userId == repository.getCurrentUserId()
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Gagal memuat laporan"
            }
            
            _isLoading.value = false
        }
    }
    
    fun deleteItem(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val currentItem = _item.value ?: return@launch
            val result = repository.deleteItem(currentItem.id, currentItem.imageStoragePath)
            result.onSuccess {
                onSuccess()
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Gagal menghapus laporan"
            }
        }
    }
    
    fun markAsCompleted(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.markAsCompleted(itemId)
            result.onSuccess {
                loadItem() // Reload untuk update status
                onSuccess()
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Gagal menandai selesai"
            }
        }
    }
}

