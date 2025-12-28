package com.campus.lostfound.ui.screen

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.campus.lostfound.data.model.Category
import com.campus.lostfound.data.model.LostFoundItem
import com.campus.lostfound.ui.components.ItemCard
import com.campus.lostfound.ui.viewmodel.ActivityViewModel
import com.campus.lostfound.util.WhatsAppUtil
import com.campus.lostfound.util.rememberImagePicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen() {
    val context = LocalContext.current
    val viewModel: ActivityViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ActivityViewModel(context) as T
            }
        }
    )
    
    ActivityScreenContent(context = context, viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityScreenContent(
    context: android.content.Context,
    viewModel: ActivityViewModel
) {
    val myReports by viewModel.myReports.collectAsStateWithLifecycle()
    val myHistory by viewModel.history.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    
    var showDeleteDialog by remember { mutableStateOf<LostFoundItem?>(null) }
    var showCompleteDialog by remember { mutableStateOf<LostFoundItem?>(null) }
    var showEditDialog by remember { mutableStateOf<LostFoundItem?>(null) }
    
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Header + Tabs
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Aktivitas Saya",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Kelola laporan Anda",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Single screen with toggle filter: Aktif / Riwayat
            var showHistory by remember { mutableStateOf(false) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !showHistory,
                    onClick = { showHistory = false },
                    label = { Text("Aktif") }
                )
                FilterChip(
                    selected = showHistory,
                    onClick = { showHistory = true },
                    label = { Text("Riwayat") }
                )
            }
        }
        
        // Error Message
        errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        // Content
        val currentList = if (showHistory) myHistory else myReports
        when {
            isLoading && currentList.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
            }
            currentList.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                if (showHistory) Icons.Default.History else Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (showHistory) "Belum ada riwayat" else "Belum ada laporan",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (showHistory) "Laporan yang selesai akan muncul di sini" else "Buat laporan pertama Anda",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    itemsIndexed(currentList) { index, item ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(300, delayMillis = index * 50)) +
                                    slideInVertically(
                                        initialOffsetY = { it / 2 },
                                        animationSpec = tween(300, delayMillis = index * 50)
                                    )
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    ItemCard(
                                        item = item,
                                        onContactClick = {
                                            WhatsAppUtil.openWhatsApp(
                                                context = context,
                                                phoneNumber = item.whatsappNumber,
                                                itemName = item.itemName,
                                                type = if (item.type == com.campus.lostfound.data.model.ItemType.LOST) "barang hilang" else "barang ditemukan"
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Divider()

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { showEditDialog = item },
                                            modifier = Modifier.weight(1f),
                                            enabled = !item.isCompleted
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Edit")
                                        }

                                        OutlinedButton(
                                            onClick = { showCompleteDialog = item },
                                            modifier = Modifier.weight(1f),
                                            enabled = !item.isCompleted
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Selesai")
                                        }

                                        OutlinedButton(
                                            onClick = { showDeleteDialog = item },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                            )
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Hapus")
                                        }
                                    }

                                    if (item.isCompleted) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = MaterialTheme.shapes.small,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Laporan ini telah ditandai selesai",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
        } else {
            // Riwayat tab
            if (myHistory.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Belum ada riwayat",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Laporan yang selesai akan muncul di sini",
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
                    itemsIndexed(myHistory) { index, item ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(300, delayMillis = index * 50)) +
                                    slideInVertically(
                                        initialOffsetY = { it / 2 },
                                        animationSpec = tween(300, delayMillis = index * 50)
                                    )
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    ItemCard(
                                        item = item,
                                        onContactClick = {
                                            WhatsAppUtil.openWhatsApp(
                                                context = context,
                                                phoneNumber = item.whatsappNumber,
                                                itemName = item.itemName,
                                                type = if (item.type == com.campus.lostfound.data.model.ItemType.LOST) "barang hilang" else "barang ditemukan"
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = MaterialTheme.shapes.small,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Laporan ini telah diselesaikan",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Delete Confirmation Bottom Sheet
    showDeleteDialog?.let { item ->
        ModalBottomSheet(
            onDismissRequest = { showDeleteDialog = null },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Hapus Laporan?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Apakah Anda yakin ingin menghapus laporan \"${item.itemName}\"? Tindakan ini tidak dapat dibatalkan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDeleteDialog = null },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = {
                            viewModel.deleteReport(item) {
                                showDeleteDialog = null
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Hapus")
                    }
                }
            }
        }
    }
    
    // Complete Confirmation Bottom Sheet
    showCompleteDialog?.let { item ->
        ModalBottomSheet(
            onDismissRequest = { showCompleteDialog = null },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Tandai Selesai?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Apakah Anda yakin ingin menandai laporan \"${item.itemName}\" sebagai selesai?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showCompleteDialog = null },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = {
                            viewModel.markAsCompleted(item) {
                                showCompleteDialog = null
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ya, Tandai Selesai")
                    }
                }
            }
        }
    }
    
    // Edit Dialog
    showEditDialog?.let { item ->
        EditReportDialog(
            item = item,
            onDismiss = { showEditDialog = null },
            onSave = { updatedItem, imageUri ->
                viewModel.updateReport(
                    itemId = item.id,
                    itemName = updatedItem.itemName.takeIf { it != item.itemName },
                    category = updatedItem.category.takeIf { it != item.category },
                    location = updatedItem.location.takeIf { it != item.location },
                    description = updatedItem.description.takeIf { it != item.description },
                    whatsappNumber = updatedItem.whatsappNumber.takeIf { it != item.whatsappNumber },
                    imageUri = imageUri,
                    onSuccess = {
                        showEditDialog = null
                    }
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditReportDialog(
    item: LostFoundItem,
    onDismiss: () -> Unit,
    onSave: (LostFoundItem, Uri?) -> Unit
) {
    var itemName by remember { mutableStateOf(item.itemName) }
    var category by remember { mutableStateOf(item.category) }
    var location by remember { mutableStateOf(item.location) }
    var description by remember { mutableStateOf(item.description) }
    var whatsappNumber by remember { mutableStateOf(item.whatsappNumber) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val imagePicker = rememberImagePicker { uri ->
        imageUri = uri
    }
    
    val scrollState = rememberScrollState()
    val isPhoneValid = whatsappNumber.isBlank() || 
                      WhatsAppUtil.isValidIndonesianPhoneNumber(whatsappNumber)
    val formattedPreview = if (whatsappNumber.isNotBlank() && isPhoneValid) {
        WhatsAppUtil.formatPhoneNumber(whatsappNumber)
    } else {
        null
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Laporan", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Item Name
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Nama Barang *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Category
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category.displayName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Kategori *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        Category.values().forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                // Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Lokasi *") },
                    placeholder = { Text("Contoh: Perpustakaan Lt. 2") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                    }
                )
                
                // Image
                Text(
                    text = "Foto Barang",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                if (imageUri != null) {
                    Box {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Selected image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { imageUri = null },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove image",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else if (item.imageUrl.isNotEmpty()) {
                    // Show existing image
                    if (com.campus.lostfound.util.ImageConverter.isBase64Image(item.imageUrl)) {
                        // Base64 image - use remember with runCatching for error handling
                        val bitmapResult = remember(item.imageUrl) {
                            runCatching {
                                val base64String = com.campus.lostfound.util.ImageConverter.extractBase64(item.imageUrl)
                                val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                                android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            }
                        }
                        
                        bitmapResult.getOrNull()?.let { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Current image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } ?: run {
                            // Fallback jika decode gagal
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Image,
                                        contentDescription = "Image placeholder",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        Image(
                            painter = rememberAsyncImagePainter(item.imageUrl),
                            contentDescription = "Current image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                
                OutlinedButton(
                    onClick = { showImageSourceDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (item.imageUrl.isNotEmpty()) "Ganti Foto" else "Tambah Foto")
                }
                
                // WhatsApp Number
                OutlinedTextField(
                    value = whatsappNumber,
                    onValueChange = { whatsappNumber = it },
                    label = { Text("Nomor WhatsApp *") },
                    placeholder = { Text("08123456789 atau 628123456789") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null)
                    },
                    isError = !isPhoneValid && whatsappNumber.isNotBlank(),
                    supportingText = {
                        Column {
                            if (!isPhoneValid && whatsappNumber.isNotBlank()) {
                                Text(
                                    text = "Format nomor tidak valid",
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else if (formattedPreview != null && formattedPreview != whatsappNumber) {
                                Text(
                                    text = "Akan dikonversi ke: $formattedPreview",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } else {
                                Text(
                                    text = "Contoh: 08123456789 atau 628123456789",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                )
                
                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it.take(500) },
                    label = { Text("Deskripsi (Opsional)") },
                    placeholder = { Text("Tambahkan detail tambahan...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4,
                    supportingText = {
                        Text(
                            text = "${description.length}/500",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (itemName.isNotBlank() && location.isNotBlank() && 
                        whatsappNumber.isNotBlank() && isPhoneValid) {
                        val updatedItem = item.copy(
                            itemName = itemName,
                            category = category,
                            location = location,
                            description = description,
                            whatsappNumber = whatsappNumber
                        )
                        onSave(updatedItem, imageUri)
                    }
                },
                enabled = itemName.isNotBlank() && location.isNotBlank() && 
                         whatsappNumber.isNotBlank() && isPhoneValid
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
    
    // Image Source Dialog
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Pilih Sumber Foto") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            showImageSourceDialog = false
                            imagePicker.pickFromGallery()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Photo, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pilih dari Galeri")
                    }
                    TextButton(
                        onClick = {
                            showImageSourceDialog = false
                            imagePicker.takePhoto()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ambil Foto")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImageSourceDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

