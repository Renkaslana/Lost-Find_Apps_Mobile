package com.campus.lostfound.ui.screen

import android.net.Uri
import androidx.compose.foundation.Image
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
import com.campus.lostfound.data.model.Category
import com.campus.lostfound.data.model.ItemType
import com.campus.lostfound.ui.viewmodel.AddReportViewModel
import com.campus.lostfound.util.WhatsAppUtil
import com.campus.lostfound.util.rememberImagePicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReportScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AddReportViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AddReportViewModel(context) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val imagePicker = rememberImagePicker { uri ->
        tempImageUri = uri
        viewModel.setImageUri(uri)
    }
    
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
            viewModel.resetState()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Laporan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error Message
            uiState.errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Item Type Selection
            Text(
                text = "Jenis Laporan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = uiState.itemType == ItemType.LOST,
                    onClick = { viewModel.setItemType(ItemType.LOST) },
                    label = { Text("Barang Hilang") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = uiState.itemType == ItemType.FOUND,
                    onClick = { viewModel.setItemType(ItemType.FOUND) },
                    label = { Text("Barang Ditemukan") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Item Name
            OutlinedTextField(
                value = uiState.itemName,
                onValueChange = { viewModel.setItemName(it) },
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
                    value = uiState.category.displayName,
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
                    Category.values().forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.displayName) },
                            onClick = {
                                viewModel.setCategory(category)
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            // Location
            OutlinedTextField(
                value = uiState.location,
                onValueChange = { viewModel.setLocation(it) },
                label = { Text("Lokasi *") },
                placeholder = { Text("Contoh: Perpustakaan Lt. 2") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                }
            )
            
            // Image Upload
            Text(
                text = "Foto Barang",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (uiState.imageUri != null) {
                Box {
                    Image(
                        painter = rememberAsyncImagePainter(uiState.imageUri),
                        contentDescription = "Selected image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = {
                            viewModel.setImageUri(null)
                            tempImageUri = null
                        },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove image",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { showImageSourceDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ambil Foto / Pilih dari Galeri")
                }
            }
            
            // WhatsApp Number
            val isPhoneValid = uiState.whatsappNumber.isBlank() || 
                              WhatsAppUtil.isValidIndonesianPhoneNumber(uiState.whatsappNumber)
            val formattedPreview = if (uiState.whatsappNumber.isNotBlank() && isPhoneValid) {
                WhatsAppUtil.formatPhoneNumber(uiState.whatsappNumber)
            } else {
                null
            }
            
            OutlinedTextField(
                value = uiState.whatsappNumber,
                onValueChange = { viewModel.setWhatsAppNumber(it) },
                label = { Text("Nomor WhatsApp *") },
                placeholder = { Text("08123456789 atau 628123456789") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null)
                },
                isError = !isPhoneValid && uiState.whatsappNumber.isNotBlank(),
                supportingText = {
                    Column {
                        if (!isPhoneValid && uiState.whatsappNumber.isNotBlank()) {
                            Text(
                                text = "Format nomor tidak valid",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else if (formattedPreview != null && formattedPreview != uiState.whatsappNumber) {
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
                value = uiState.description,
                onValueChange = { viewModel.setDescription(it) },
                label = { Text("Deskripsi (Opsional)") },
                placeholder = { Text("Tambahkan detail tambahan...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                supportingText = {
                    Text(
                        text = "${uiState.description.length}/500",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Submit Button
            Button(
                onClick = { viewModel.submitReport(onNavigateBack) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Kirim Laporan", fontWeight = FontWeight.Bold)
            }
            
            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Batal")
            }
        }
    }
    
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

