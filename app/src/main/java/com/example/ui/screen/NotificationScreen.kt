package com.example.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Peminjaman
import com.example.data.PeminjamanStatus
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val peminjamanList by viewModel.peminjamanList.collectAsState()
    
    // Reverse the list to show newest first
    val notifications = peminjamanList.asReversed()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifikasi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF8FAFC)
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
                        Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFFE2E8F0)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Belum ada notifikasi", color = Color(0xFF64748B))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { item ->
                    NotificationItem(item)
                }
            }
        }
    }
}

@Composable
fun NotificationItem(item: Peminjaman) {
    val (statusColor, statusText) = when (item.status) {
        PeminjamanStatus.MENUNGGU_RT -> Color(0xFFF59E0B) to "Sedang Menunggu Verifikasi RT"
        PeminjamanStatus.MENUNGGU_KEPALA -> Color(0xFF3B82F6) to "Sedang Menunggu Verifikasi Kepala RT"
        PeminjamanStatus.DISETUJUI -> Color(0xFF10B981) to "Peminjaman Anda telah DISETUJUI"
        PeminjamanStatus.DITOLAK_RT, PeminjamanStatus.DITOLAK_KEPALA -> Color(0xFFEF4444) to "Peminjaman Anda DITOLAK"
        PeminjamanStatus.BUTUH_REVISI -> Color(0xFF8B5CF6) to "Peminjaman Anda membutuhkan REVISI"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.ruang?.nama ?: "Ruangan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "${item.tanggal} • ${item.waktuMulai}",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
