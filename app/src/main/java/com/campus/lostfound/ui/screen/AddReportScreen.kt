package com.campus.lostfound.ui.screen

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.campus.lostfound.data.model.Category
import com.campus.lostfound.data.model.ItemType
import com.campus.lostfound.ui.viewmodel.AddReportViewModel
import com.campus.lostfound.ui.viewmodel.AddReportUiState
import com.campus.lostfound.util.WhatsAppUtil
import com.campus.lostfound.util.rememberImagePicker
import com.campus.lostfound.util.ImagePickerLauncher

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
    
    // Step-based UI state
    var currentStep by remember { mutableIntStateOf(1) }
    val totalSteps = 3
    
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
            Column {
                TopAppBar(
                    title = { Text("Tambah Laporan", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
                // Progress Indicator
                LinearProgressIndicator(
                    progress = currentStep.toFloat() / totalSteps,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
                // Step Indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(totalSteps) { step ->
                        StepIndicator(
                            step = step + 1,
                            isActive = step + 1 == currentStep,
                            isCompleted = step + 1 < currentStep
                        )
                    }
                }
            }
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
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            // Step Content
            when (currentStep) {
                1 -> Step1Content(
                    uiState = uiState,
                    viewModel = viewModel
                )
                2 -> Step2Content(
                    uiState = uiState,
                    viewModel = viewModel
                )
                3 -> Step3Content(
                    uiState = uiState,
                    viewModel = viewModel,
                    showImageSourceDialog = showImageSourceDialog,
                    onShowImageSourceDialog = { showImageSourceDialog = it },
                    imagePicker = imagePicker,
                    tempImageUri = tempImageUri,
                    onTempImageUriChange = { tempImageUri = it }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kembali")
                    }
                    
                    Button(
                        onClick = {
                            if (currentStep < totalSteps) {
                                currentStep++
                            } else {
                                viewModel.submitReport(onNavigateBack)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        shape = RoundedCornerShape(28.dp),
                        enabled = when (currentStep) {
                            1 -> uiState.itemName.isNotBlank() && uiState.location.isNotBlank()
                            2 -> {
                                val isPhoneValid = uiState.whatsappNumber.isBlank() || 
                                                  WhatsAppUtil.isValidIndonesianPhoneNumber(uiState.whatsappNumber)
                                uiState.whatsappNumber.isNotBlank() && isPhoneValid
                            }
                            else -> !uiState.isLoading
                        }
                    ) {
                        if (currentStep < totalSteps) {
                            Text("Lanjut")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        } else {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mengirim...")
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Kirim Laporan")
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            if (currentStep < totalSteps) {
                                currentStep++
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.itemName.isNotBlank() && uiState.location.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text("Lanjut")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
            
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Batal")
            }
        }
    }
    

}

// Step 1: Info Barang
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step1Content(
    uiState: AddReportUiState,
    viewModel: AddReportViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Informasi Barang",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        // Item Type Selection
        Text(
            text = "Jenis Laporan *",
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
            onValueChange = { value: String -> viewModel.setItemName(value) },
            label = { Text("Nama Barang *") },
            placeholder = { Text("Contoh: Tas Hitam, HP Samsung") },
            leadingIcon = {
                Icon(Icons.Default.Edit, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            isError = uiState.itemName.isBlank(),
            supportingText = {
                if (uiState.itemName.isBlank()) {
                    Text(
                        text = "Nama barang wajib diisi",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text("Contoh: Tas Hitam, HP Samsung")
                }
            }
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
                leadingIcon = {
                    Icon(Icons.Default.Category, contentDescription = null)
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = MaterialTheme.shapes.medium
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
            onValueChange = { value: String -> viewModel.setLocation(value) },
            label = { Text("Lokasi *") },
            placeholder = { Text("Contoh: Perpustakaan Lt. 2") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.LocationOn, contentDescription = null)
            },
            shape = MaterialTheme.shapes.medium,
            isError = uiState.location.isBlank(),
            supportingText = {
                if (uiState.location.isBlank()) {
                    Text(
                        text = "Lokasi wajib diisi",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text("Contoh: Perpustakaan Lt. 2")
                }
            }
        )
    }
}

// Step 2: Kontak & Deskripsi
@Composable
private fun Step2Content(
    uiState: AddReportUiState,
    viewModel: AddReportViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Kontak & Deskripsi",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
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
            onValueChange = { value: String -> viewModel.setWhatsAppNumber(value) },
            label = { Text("Nomor WhatsApp *") },
            placeholder = { Text("08123456789 atau 628123456789") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = null)
            },
            shape = MaterialTheme.shapes.medium,
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
            onValueChange = { value: String -> viewModel.setDescription(value) },
            label = { Text("Deskripsi (Opsional)") },
            placeholder = { Text("Tambahkan detail seperti ciri khusus, warna, dll...") },
            leadingIcon = {
                Icon(Icons.Default.Description, contentDescription = null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5,
            shape = MaterialTheme.shapes.medium,
            supportingText = {
                Text(
                    text = "${uiState.description.length}/500",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        )
    }
}

// Step 3: Upload Foto
@Composable
private fun Step3Content(
    uiState: AddReportUiState,
    viewModel: AddReportViewModel,
    showImageSourceDialog: Boolean,
    onShowImageSourceDialog: (Boolean) -> Unit,
    imagePicker: ImagePickerLauncher,
    tempImageUri: Uri?,
    onTempImageUriChange: (Uri?) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Foto Barang",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Tambahkan foto untuk memudahkan pencarian",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (uiState.imageUri != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box {
                    Image(
                        painter = rememberAsyncImagePainter(uiState.imageUri),
                        contentDescription = "Selected image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.setImageUri(null)
                                onTempImageUriChange(null)
                            }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove image",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        } else {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onShowImageSourceDialog(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Ambil atau Pilih Foto",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Ketuk untuk memilih foto",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    
    // Image Source Dialog
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { onShowImageSourceDialog(false) },
            title = { 
                Text(
                    "Pilih Sumber Foto",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Gallery Option
                    Surface(
                        onClick = {
                            onShowImageSourceDialog(false)
                            imagePicker.pickFromGallery()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Photo, 
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Pilih dari Galeri",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Camera Option
                    Surface(
                        onClick = {
                            onShowImageSourceDialog(false)
                            imagePicker.takePhoto()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.PhotoCamera, 
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Ambil Foto",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onShowImageSourceDialog(false) }) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun StepIndicator(
    step: Int,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = when {
                isCompleted -> MaterialTheme.colorScheme.primary
                isActive -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier.size(32.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (isCompleted) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = step.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

