package com.example.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Notification
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pusat Notifikasi", fontWeight = FontWeight.Black, color = Color(0xFF0F172A)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        TextButton(onClick = { viewModel.markAllNotificationsRead() }) {
                            Icon(Icons.Default.DoneAll, null, modifier = Modifier.size(18.dp), tint = Color(0xFF4F46E5))
                            Spacer(Modifier.width(6.dp))
                            Text("Baca Semua", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color(0xFF4F46E5))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.NotificationsNone,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color(0xFFE2E8F0)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Belum ada pemberitahuan baru.", fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
            ) {
                items(notifications) { item ->
                    NotificationCard(item)
                }
            }
        }
    }
}

@Composable
fun NotificationCard(item: Notification) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (item.dibaca) Color.White else Color(0xFFEFF6FF)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (item.dibaca) Color(0xFFF1F5F9) else Color(0xFF4F46E5),
                shape = CircleShape,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (item.dibaca) Color(0xFF94A3B8) else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.pesan,
                    fontSize = 13.sp,
                    fontWeight = if (item.dibaca) FontWeight.Medium else FontWeight.Black,
                    color = if (item.dibaca) Color(0xFF475569) else Color(0xFF0F172A),
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.createdAt,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
            }
            
            if (!item.dibaca) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF4F46E5), CircleShape)
                        .padding(start = 8.dp)
                )
            }
        }
    }
}
