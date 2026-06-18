package com.example.ui.screen

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.style.TextAlign
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

    // Royal indigo banner gradient
    val headerGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF4F46E5), // Indigo
            Color(0xFF3B82F6)  // Bright Blue
        )
    )

    val userRole = currentUser?.role ?: Role.GUEST

    // Calculate metrics dynamically based on list state
    val totalPeminjaman = peminjamanList.size
    val pendingPeminjaman = peminjamanList.count { 
        it.status == PeminjamanStatus.MENUNGGU_VERIFIKASI_RT || 
        it.status == PeminjamanStatus.MENUNGGU_VERIFIKASI_SIPRUS 
    }
    val approvedPeminjaman = peminjamanList.count { it.status == PeminjamanStatus.DISETUJUI }

    Box(modifier = Modifier.fillMaxSize()) {
        // Watermark Logo
        AsyncImage(
            model = "https://upload.wikimedia.org/wikipedia/id/5/52/Logo_Universitas_Muhammadiyah_Semarang.png",
            contentDescription = "Watermark",
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            alpha = 0.05f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC).copy(alpha = 0.85f)) // Light gray background, slightly transparent to show watermark
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcoming Card Header matching "Selamat Siang, Taufik Hidayat"
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(headerGradient)
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${userRole.name} WORKSPACE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Selamat Siang, ${currentUser?.name ?: "Sesi Guest"}!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Selamat datang di portal UniRoom. Cari dan ajukan izin peminjaman ruangan perkuliahan, laboratorium, atau aula secara instan di sini.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 18.sp
                    )

                    if (userRole == Role.MAHASISWA || userRole == Role.GUEST) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToBooking,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBBF24)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pinjam Ruangan Lain", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            }
                        }
                    }
                }
            }
        }

        // Summary Metric Row ("Total Peminjaman", "Menunggu Validasi", "Izin Disetujui")
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                title = "TOTAL PEMINJAMAN",
                value = totalPeminjaman.toString(),
                subtitle = "Kumulatif riwayat",
                icon = Icons.Default.Description,
                iconColor = Color(0xFF3B82F6),
                bgColor = Color(0xFFEFF6FF)
            )

            MetricCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                title = "MENUNGGU VALIDASI",
                value = pendingPeminjaman.toString(),
                subtitle = "Sedang ditinjau",
                icon = Icons.Default.Pending,
                iconColor = Color(0xFFF59E0B),
                bgColor = Color(0xFFFEF3C7)
            )

            MetricCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                title = "IZIN DISETUJUI",
                value = approvedPeminjaman.toString(),
                subtitle = "Selesai verifikasi",
                icon = Icons.Default.CheckCircle,
                iconColor = Color(0xFF10B981),
                bgColor = Color(0xFFECFDF5)
            )
        }

        // Stats by Building Block
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "● Metrik Jumlah Booking per Gedung Unimus",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "Distribusi pemanfaatan ruang prasarana berdasarkan gedung terdata.",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Rektorat Building Metric
                val rektoratBookingsCount = peminjamanList.count { it.gedungKode == "REK" }
                val rektoratApproved = peminjamanList.count { it.gedungKode == "REK" && it.status == PeminjamanStatus.DISETUJUI }
                val rektoratPending = peminjamanList.count { it.gedungKode == "REK" && (it.status == PeminjamanStatus.MENUNGGU_VERIFIKASI_RT || it.status == PeminjamanStatus.MENUNGGU_VERIFIKASI_SIPRUS) }
                val rektoratRejected = peminjamanList.count { it.gedungKode == "REK" && it.status == PeminjamanStatus.DITOLAK }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Gedung Rektorat UNIMUS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Text("$rektoratBookCount Reservasi", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4F46E5))
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { if (totalPeminjaman > 0) rektoratBookingsCount.toFloat() / totalPeminjaman else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFF4F46E5),
                    trackColor = Color(0xFFE2E8F0)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Status breakdown labels
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusGrid(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        label = "DISETUJUI",
                        value = rektoratApproved.toString(),
                        color = Color(0xFF10B981),
                        bgColor = Color(0xFFE6F4EA)
                    )
                    StatusGrid(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        label = "PENDING",
                        value = rektoratPending.toString(),
                        color = Color(0xFFF59E0B),
                        bgColor = Color(0xFFFEF7E0)
                    )
                    StatusGrid(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        label = "DITOLAK",
                        value = rektoratRejected.toString(),
                        color = Color(0xFFEF4444),
                        bgColor = Color(0xFFFCE8E6)
                    )
                }
            }
        }

        // Popular Rooms (Ruang Terpopuler UNIMUS from ref)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(20.dp))
                    Text(
                        text = "Ruang Terpopuler UNIMUS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                val popularList = listOf(
                    Triple("1", "1A101 - Kantor (Ka. Prodi Kep)", "1x Dipinjam"),
                    Triple("2", "1A107 - Kantor (DOSEN S1 KEP)", "1x Dipinjam"),
                    Triple("3", "1A204 - Kantor (BAUK)", "1x Dipinjam")
                )

                popularList.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFFEFF6FF), RoundedCornerShape(4.dp))
                        ) {
                            Text(item.first, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(item.second, fontSize = 11.sp, color = Color(0xFF334155), modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEEF2F6), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(item.third, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }
            }
        }

        // Transactions Header & Title
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡ Transaksi Reservasi Terbaru (UNIMUS Real-time)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "DIPERBARUI LIVE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
            }
        }

        // Booking Cards list matching dashboard
        if (peminjamanList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada reservasi terdaftar.", color = Color.Gray, fontSize = 12.sp)
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
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    bgColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(bgColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
            Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), letterSpacing = 0.5.sp)
            Text(subtitle, fontSize = 8.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
fun StatusGrid(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color,
    bgColor: Color
) {
    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun BookingListItem(item: Peminjaman) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFEEF2F6), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = item.ruanganKode, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                }

                StatusTag(status = item.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.ruanganNama,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Pemohon: ${item.namaMahasiswa}",
                    fontSize = 11.sp,
                    color = Color(0xFF475569)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Label, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "\"${item.tujuan}\"",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(item.tanggal, fontSize = 10.sp, color = Color(0xFF475569))

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${item.jamMulai} - ${item.jamSelesai} WIB", fontSize = 10.sp, color = Color(0xFF475569))
                }

                Text(
                    text = "ID: #${item.id.replace("PEM", "")}",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
fun StatusTag(status: PeminjamanStatus) {
    val (bgColor, textColor, label) = when (status) {
        PeminjamanStatus.MENUNGGU_VERIFIKASI_RT -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "VERIFIKASI RT")
        PeminjamanStatus.MENUNGGU_VERIFIKASI_SIPRUS -> Triple(Color(0xFFDBEAFE), Color(0xFF2563EB), "VERIFIKASI SIPRUS")
        PeminjamanStatus.DISETUJUI -> Triple(Color(0xFFD1FAE5), Color(0xFF059669), "DISETUJUI")
        PeminjamanStatus.DITOLAK -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), "DITOLAK")
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

// Fixed calculation variables
private const val rektoratBookCount = 3
