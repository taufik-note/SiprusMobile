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
import androidx.compose.ui.draw.shadow
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
            alpha = 0.03f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.background(headerGradient).padding(24.dp)) {
                    Column {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${userRole.name} WORKSPACE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Selamat Datang, ${currentUser?.name ?: "Guest"}!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Kelola dan lacak ketersediaan ruangan kampus dalam satu genggaman.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        if (userRole == Role.MAHASISWA) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = onNavigateToBooking,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBBF24)),
                                shape = RoundedCornerShape(12.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF1E293B), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("PINJAM RUANGAN", fontWeight = FontWeight.Black, color = Color(0xFF1E293B), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(Modifier.weight(1f), "TOTAL", totalPeminjaman.toString(), Icons.Default.Description, Color(0xFF3B82F6))
                MetricCard(Modifier.weight(1f), "PENDING", pendingPeminjaman.toString(), Icons.Default.Pending, Color(0xFFF59E0B))
                MetricCard(Modifier.weight(1f), "SETUJU", approvedPeminjaman.toString(), Icons.Default.CheckCircle, Color(0xFF10B981))
            }

            Text("⚡ Reservasi Terbaru", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF0F172A))

            if (peminjamanList.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada data reservasi saat ini.", color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            } else {
                peminjamanList.asReversed().take(5).forEach { item ->
                    BookingListItem(item = item)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun MetricCard(modifier: Modifier, title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = modifier, 
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B))
        }
    }
}

@Composable
fun BookingListItem(item: Peminjaman) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(item.ruang?.kode ?: "ROOM", fontWeight = FontWeight.Black, color = Color(0xFF4F46E5), fontSize = 12.sp)
                StatusTag(item.status)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.ruang?.nama ?: "Nama Ruangan", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.Black)
            Text("Oleh: ${item.user?.name ?: "User"}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
            
            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, null, Modifier.size(14.dp), tint = Color(0xFF64748B))
                Text(" ${item.tanggal} ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.AccessTime, null, Modifier.size(14.dp), tint = Color(0xFF64748B))
                Text(" ${item.waktuMulai} - ${item.waktuSelesai}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
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
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp), border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))) {
        Text(label, color = color, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
    }
}
