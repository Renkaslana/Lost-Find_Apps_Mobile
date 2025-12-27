package com.campus.lostfound.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageConverter {
    /**
     * Convert image URI to Base64 string
     * Returns empty string if conversion fails
     */
    suspend fun uriToBase64(imageUri: Uri, context: Context): String {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                return ""
            }
            
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (bitmap == null) {
                return ""
            }
            
            // Compress bitmap to reduce size (max ~500KB)
            val compressedBitmap = compressBitmap(bitmap, maxSizeKB = 500)
            
            // Convert to Base64
            val outputStream = ByteArrayOutputStream()
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val byteArray = outputStream.toByteArray()
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            
            // Return as data URL format
            "data:image/jpeg;base64,$base64String"
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * Compress bitmap to target size
     */
    private fun compressBitmap(bitmap: Bitmap, maxSizeKB: Int): Bitmap {
        var quality = 90
        var stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        var sizeKB = stream.size() / 1024
        
        // Compress until size is acceptable
        while (sizeKB > maxSizeKB && quality > 30) {
            quality -= 10
            stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            sizeKB = stream.size() / 1024
        }
        
        return bitmap
    }
    
    /**
     * Check if string is Base64 image data URL
     */
    fun isBase64Image(url: String): Boolean {
        return url.startsWith("data:image")
    }
    
    /**
     * Extract Base64 string from data URL
     */
    fun extractBase64(dataUrl: String): String {
        return if (dataUrl.contains(",")) {
            dataUrl.substringAfter(",")
        } else {
            dataUrl
        }
    }
}

