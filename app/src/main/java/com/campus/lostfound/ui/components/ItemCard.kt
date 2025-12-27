package com.campus.lostfound.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.campus.lostfound.data.model.ItemType
import com.campus.lostfound.data.model.LostFoundItem
import com.campus.lostfound.ui.theme.FoundGreen
import com.campus.lostfound.ui.theme.FoundGreenLight
import com.campus.lostfound.ui.theme.LostRed
import com.campus.lostfound.ui.theme.LostRedLight
import com.campus.lostfound.util.ImageConverter

@Composable
fun ItemCard(
    item: LostFoundItem,
    onContactClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image
            if (item.imageUrl.isNotEmpty()) {
                // Check if Base64 image or URL
                if (ImageConverter.isBase64Image(item.imageUrl)) {
                    // Base64 image
                    val bitmapResult = remember(item.imageUrl) {
                        runCatching {
                            val base64String = ImageConverter.extractBase64(item.imageUrl)
                            val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        }
                    }
                    
                    bitmapResult.getOrNull()?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = item.itemName,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } ?: ImagePlaceholder()
                } else {
                    // URL image (untuk kompatibilitas dengan Firebase Storage jika digunakan nanti)
                    Image(
                        painter = rememberAsyncImagePainter(item.imageUrl),
                        contentDescription = item.itemName,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                ImagePlaceholder()
            }
            
            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Badge
                    Surface(
                        color = if (item.type == ItemType.LOST) LostRedLight else FoundGreenLight,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = if (item.type == ItemType.LOST) "Hilang" else "Ditemukan",
                            color = if (item.type == ItemType.LOST) LostRed else FoundGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    // Item Name
                    Text(
                        text = item.itemName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    
                    // Location
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = item.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Time
                    Text(
                        text = item.getTimeAgo(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Contact Button
                Button(
                    onClick = onContactClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hubungi")
                }
            }
        }
    }
}

@Composable
private fun ImagePlaceholder() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = "No image",
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
            )
        }
    }
}
