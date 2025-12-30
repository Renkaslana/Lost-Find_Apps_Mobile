package com.campus.lostfound.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerBrush(showShimmer: Boolean = true): Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        )
        
        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnimation by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1200,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerTranslate"
        )
        
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnimation - 500f, translateAnimation - 500f),
            end = Offset(translateAnimation, translateAnimation)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent)
        )
    }
}

@Composable
fun ShimmerItemCard(modifier: Modifier = Modifier) {
    val brush = ShimmerBrush()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Image placeholder
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(brush)
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            
            // Location placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            
            // Time placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}

@Composable
fun ShimmerNotificationItem(modifier: Modifier = Modifier) {
    val brush = ShimmerBrush()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Circle icon placeholder
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(brush)
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Title
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            
            // Description line 1
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            
            // Description line 2
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            
            // Time
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.25f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}

@Composable
fun ShimmerLoadingList(
    itemCount: Int = 5,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        repeat(itemCount) {
            ShimmerItemCard()
        }
    }
}

@Composable
fun ShimmerNotificationList(
    itemCount: Int = 5,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        repeat(itemCount) {
            ShimmerNotificationItem()
        }
    }
}
