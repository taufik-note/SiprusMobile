package com.example.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.viewmodel.AppViewModel
import com.example.data.Peminjaman
import com.example.data.PeminjamanStatus
import com.example.data.Role

@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    onNavigateToBooking: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val peminjamanList by viewModel.peminjamanList.collectAsState()
    val scrollState = rememberScrollState()

    val headerGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF4F46E5), Color(0xFF3B82F6))
    )

    val userRole = currentUser?.role ?: Role.GUEST

    val totalPeminjaman = peminjamanList.size
    val pendingPeminjaman = peminjamanList.count { 
        it.status == PeminjamanStatus.MENUNGGU_RT || it.status == PeminjamanStatus.MENUNGGU_KEPALA 
    }
    val approvedPeminjaman = peminjamanList.count { it.status == PeminjamanStatus.DISETUJUI }

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = "https://upload.wikimedia.org/wikipedia/id/5/52/Logo_Universitas_Muhammadiyah_Semarang.png",
            contentDescription = null,
            modifier = Modifier.fillMaxSize().padding(32.dp),
            alpha = 0.05f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC).copy(alpha = 0.85f))
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.background(headerGradient).padding(20.dp)) {
                    Column {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${userRole.name} WORKSPACE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Selamat Siang, ${currentUser?.name ?: "Guest"}!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Selamat datang di portal UniRoom. Cari dan ajukan izin peminjaman ruangan secara instan.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        if (userRole == Role.MAHASISWA) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onNavigateToBooking,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBBF24)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pinjam Ruangan", color = Color(0xFF1E293B))
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(Modifier.weight(1f), "TOTAL", totalPeminjaman.toString(), Icons.Default.Description, Color(0xFF3B82F6))
                MetricCard(Modifier.weight(1f), "PENDING", pendingPeminjaman.toString(), Icons.Default.Pending, Color(0xFFF59E0B))
                MetricCard(Modifier.weight(1f), "SETUJU", approvedPeminjaman.toString(), Icons.Default.CheckCircle, Color(0xFF10B981))
            }

            Text("⚡ Reservasi Terbaru", fontWeight = FontWeight.Bold, fontSize = 14.sp)

            if (peminjamanList.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada reservasi.", color = Color.Gray)
                }
            } else {
                peminjamanList.asReversed().take(5).forEach { item ->
                    BookingListItem(item = item)
                }
            }
        }
    }
}

@Composable
fun MetricCard(modifier: Modifier, title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(title, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
fun BookingListItem(item: Peminjaman) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(item.ruang?.kode ?: "ROOM", fontWeight = FontWeight.Bold)
                StatusTag(item.status)
            }
            Text(item.ruang?.nama ?: "Nama Ruangan", fontSize = 12.sp)
            Text("Oleh: ${item.user?.name ?: "User"}", fontSize = 11.sp, color = Color.Gray)
            Divider(Modifier.padding(vertical = 8.dp))
            Row {
                Icon(Icons.Default.CalendarToday, null, Modifier.size(12.dp))
                Text(" ${item.tanggal} ", fontSize = 10.sp)
                Icon(Icons.Default.AccessTime, null, Modifier.size(12.dp))
                Text(" ${item.waktuMulai} - ${item.waktuSelesai}", fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun StatusTag(status: PeminjamanStatus) {
    val (color, label) = when (status) {
        PeminjamanStatus.MENUNGGU_RT -> Color(0xFFF59E0B) to "WAIT RT"
        PeminjamanStatus.MENUNGGU_KEPALA -> Color(0xFF3B82F6) to "WAIT KEPALA"
        PeminjamanStatus.DISETUJUI -> Color(0xFF10B981) to "SETUJU"
        PeminjamanStatus.DITOLAK_RT, PeminjamanStatus.DITOLAK_KEPALA -> Color(0xFFEF4444) to "DITOLAK"
        PeminjamanStatus.BUTUH_REVISI -> Color(0xFF8B5CF6) to "REVISI"
    }
    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
        Text(label, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
    }
}
