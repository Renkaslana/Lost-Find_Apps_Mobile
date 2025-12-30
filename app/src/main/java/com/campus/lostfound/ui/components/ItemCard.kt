package com.campus.lostfound.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.campus.lostfound.data.model.ItemType
import com.campus.lostfound.data.model.LostFoundItem
import com.campus.lostfound.ui.theme.FoundGreen
import com.campus.lostfound.ui.theme.LostRed
import com.campus.lostfound.util.ImageConverter
import com.campus.lostfound.ui.theme.FoundGreenLight
import com.campus.lostfound.ui.theme.LostRedLight
import com.campus.lostfound.ui.theme.AccentTeal

@Composable
fun ItemCard(
    item: LostFoundItem,
    onContactClick: () -> Unit,
    onCardClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Premium elevation animation - lebih tinggi untuk premium feel
    val elevation by animateFloatAsState(
        targetValue = if (isPressed) 8f else 4f, // Increased baseline elevation
        animationSpec = tween(durationMillis = 150),
        label = "elevation"
    )
    
    // Scale animation untuk tactile feedback
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "scale"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp) // Reduced vertical padding for tighter spacing
            .scale(scale) // Add scale animation
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Custom ripple handled by scale
                onClick = { onCardClick?.invoke() }
            )
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(20.dp), // Increased corner radius for modern look
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header with status badge and actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Badge - More premium design
                Surface(
                    color = if (item.type == ItemType.LOST) LostRed else FoundGreen,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 1.dp
                ) {
                    Text(
                        text = if (item.type == ItemType.LOST) "Hilang" else "Ditemukan",
                        color = Color.White, // White text on colored background for better contrast
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                
                // Time stamp with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.getTimeAgo(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Main content area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Image section - improved styling
                if (item.imageUrl.isNotEmpty()) {
                    if (ImageConverter.isBase64Image(item.imageUrl)) {
                        val bitmapResult = remember(item.imageUrl) {
                            runCatching {
                                val base64String = ImageConverter.extractBase64(item.imageUrl)
                                val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                                android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            }
                        }
                        
                        bitmapResult.getOrNull()?.let { bitmap ->
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            ) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = item.itemName,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Subtle overlay for better readability if needed
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Color.Black.copy(alpha = 0.05f),
                                            RoundedCornerShape(16.dp)
                                        )
                                )
                            }
                        } ?: PremiumImagePlaceholder()
                    } else {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(item.imageUrl),
                                contentDescription = item.itemName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                } else {
                    PremiumImagePlaceholder()
                }
                
                // Content section - better hierarchy
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Item name - prominent typography
                    Text(
                        text = item.itemName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        minLines = 1
                    )
                    
                    // Location with better icon treatment
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = item.location,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    
                    // Category if needed
                    Text(
                        text = item.category.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            
            // Action button - full-width design
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onContactClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Hubungi",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PremiumImagePlaceholder() {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Image,
            contentDescription = "No image",
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
