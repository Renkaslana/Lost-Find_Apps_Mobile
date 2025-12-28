package com.campus.lostfound.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.campus.lostfound.navigation.Screen

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(
            screen = Screen.Home,
            label = "Beranda",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        BottomNavItem(
            screen = Screen.Add,
            label = "Tambah",
            selectedIcon = Icons.Filled.AddCircle,
            unselectedIcon = Icons.Outlined.AddCircle
        ),
        BottomNavItem(
            screen = Screen.Activity,
            label = "Aktivitas",
            selectedIcon = Icons.Filled.List,
            unselectedIcon = Icons.Outlined.List
        ),
        BottomNavItem(
            screen = Screen.Settings,
            label = "Pengaturan",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings
        )
    )
    
    NavigationBar(
        modifier = modifier.shadow(
            elevation = 8.dp,
            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            NavigationBarItemWithAnimation(
                item = item,
                currentRoute = currentRoute,
                onNavigate = onNavigate
            )
        }
    }
}

@Composable
private fun RowScope.NavigationBarItemWithAnimation(
    item: BottomNavItem,
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val isSelected = currentRoute == item.screen.route
    
    // Animasi warna icon
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 300),
        label = "iconColor"
    )
    
    // Animasi skala icon (1.0 → 1.1 saat selected)
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "iconScale"
    )
    
    NavigationBarItem(
        icon = {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier.scale(iconScale)
            )
        },
        label = { 
            Text(
                text = item.label,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = iconColor
            )
        },
        selected = isSelected,
        onClick = { 
            if (currentRoute != item.screen.route) {
                onNavigate(item.screen.route)
            }
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

