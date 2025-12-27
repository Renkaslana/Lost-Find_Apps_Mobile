package com.campus.lostfound.util

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun rememberImagePicker(
    onImageSelected: (Uri?) -> Unit
): ImagePickerLauncher {
    val context = LocalContext.current
    
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            onImageSelected(cameraImageUri)
        }
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        onImageSelected(uri)
    }
    
    return remember {
        ImagePickerLauncher(
            cameraLauncher = cameraLauncher,
            galleryLauncher = galleryLauncher,
            context = context,
            onUriCreated = { uri ->
                cameraImageUri = uri
            }
        )
    }
}

class ImagePickerLauncher(
    val cameraLauncher: androidx.activity.result.ActivityResultLauncher<Uri>,
    val galleryLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    val context: Context,
    val onUriCreated: (Uri) -> Unit
) {
    fun pickFromGallery() {
        galleryLauncher.launch("image/*")
    }
    
    fun takePhoto() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        val imageUri = android.net.Uri.fromFile(imageFile)
        onUriCreated(imageUri)
        cameraLauncher.launch(imageUri)
    }
}

