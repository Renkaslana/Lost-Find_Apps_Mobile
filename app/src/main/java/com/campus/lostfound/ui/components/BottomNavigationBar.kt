package com.campus.lostfound.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.campus.lostfound.navigation.Screen

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Beranda") },
            selected = currentRoute == Screen.Home.route,
            onClick = { onNavigate(Screen.Home.route) }
        )
        
        NavigationBarItem(
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            label = { Text("Tambah") },
            selected = currentRoute == Screen.Add.route,
            onClick = { onNavigate(Screen.Add.route) }
        )
        
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = null) },
            label = { Text("Aktivitas") },
            selected = currentRoute == Screen.Activity.route,
            onClick = { onNavigate(Screen.Activity.route) }
        )
        
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("Pengaturan") },
            selected = currentRoute == Screen.Settings.route,
            onClick = { onNavigate(Screen.Settings.route) }
        )
    }
}

