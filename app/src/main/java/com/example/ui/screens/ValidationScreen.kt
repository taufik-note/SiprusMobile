package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppViewModel
import com.example.data.Peminjaman
import com.example.data.PeminjamanStatus
import com.example.data.Role

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidationScreen(
    viewModel: AppViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val peminjamanList by viewModel.peminjamanList.collectAsState()
    val ruanganList by viewModel.ruanganList.collectAsState()
    val scrollState = rememberScrollState()

    val userRole = currentUser?.role ?: Role.GUEST

    // Tabs control: 0 = "Perlu Validasi", 1 = "Semua Jadwal"
    var selectedTab by remember { mutableStateOf(0) }

    // Dropdown state for relocation "Alihkan Ruangan"
    var selectedRelocateId by remember { mutableStateOf<String?>(null) }
    var showAltRoomDropdown by remember { mutableStateOf(false) }

    // Logs state trace
    val systemLogs by viewModel.systemLogs.collectAsState()

    // Determine relevant list items based on role
    // Admin RT handles Level 1
    // Kepala RT handles Level 2
    val needsValidationList = peminjamanList.filter { p ->
        if (userRole == Role.ADMIN_RT) {
            p.status == PeminjamanStatus.MENUNGGU_VERIFIKASI_RT
        } else if (userRole == Role.KEPALA_RT) {
            p.status == PeminjamanStatus.MENUNGGU_VERIFIKASI_SIPRUS
        } else {
            // Mahasiswa / Guest doesn't validate
            p.status == PeminjamanStatus.MENUNGGU_VERIFIKASI_RT || p.status == PeminjamanStatus.MENUNGGU_VERIFIKASI_SIPRUS
        }
    }

    val displayList = if (selectedTab == 0) needsValidationList else peminjamanList

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Authority Level indicator badge
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFF5F3FF), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (userRole == Role.ADMIN_RT) "WEWENANG BIRO: ADMIN_RT" else "WEWENANG BIRO: KEPALA_RT",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C3AED)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (userRole == Role.ADMIN_RT) "Verifikasi Tingkat 1 & Peralihan RT" else "Persetujuan Final & Otorisasi Kebijakan RT",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Text(
                    text = "Validasi pengajuan, kelola bentrok jadwal, dan lakukan peralihan ruangan instan jika universitas memerlukan ruangan tersebut.",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Interactive Sandbox simulation box
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Interactive Sandbox: Simulasi Bentrok / Kasus Peralihan Ruang",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                }

                Text(
                    text = "Ingin melihat bagaimana sistem menangani Peralihan Ruangan (Relocation)? Anda dapat mencontohkan kasus di mana mahasiswa sudah menjadwalkan acara, namun universitas tiba-tiba harus memakai ruangan yang sama untuk rapat penting Rektorat Kampus.",
                    fontSize = 11.sp,
                    color = Color(0xFF1E40AF),
                    lineHeight = 16.sp
                )

                Button(
                    onClick = { viewModel.triggerSimulasiBentrok() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("1. Mulai Contoh Simulasi Bentrok ➔", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Status Logs from operations
        if (systemLogs.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🔔 STATUS SYSTEM LOG (REAL-TIME):", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                    Text(
                        text = systemLogs.first(),
                        fontSize = 11.sp,
                        color = Color.White,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Segmented Tabs Control ("Perlu Validasi", "Semua Jadwal")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { selectedTab = 0 },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedTab == 0) Color(0xFFEFF6FF) else Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (selectedTab == 0) Color(0xFF2563EB) else Color(0xFFE2E8F0))
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "☑ Perlu Validasi (${needsValidationList.size})",
                    fontSize = 12.sp,
                    color = if (selectedTab == 0) Color(0xFF1E40AF) else Color(0xFF475569),
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = { selectedTab = 1 },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedTab == 1) Color(0xFFEFF6FF) else Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (selectedTab == 1) Color(0xFF2563EB) else Color(0xFFE2E8F0))
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "⇄ Semua Jadwal (${peminjamanList.size})",
                    fontSize = 12.sp,
                    color = if (selectedTab == 1) Color(0xFF1E40AF) else Color(0xFF475569),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Main List of Queue Items block
        if (displayList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Tidak ada berkas pengajuan dalam daftar ini.", fontSize = 12.sp, color = Color.Gray)
            }
        } else {
            displayList.forEach { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Title bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ID Peminjaman #${item.id.replace("PEM", "")}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4F46E5)
                            )

                            val tierText = when (item.status) {
                                PeminjamanStatus.MENUNGGU_VERIFIKASI_RT -> "MENUNGGU TINGKAT 1"
                                PeminjamanStatus.MENUNGGU_VERIFIKASI_SIPRUS -> "MENUNGGU TINGKAT 2"
                                PeminjamanStatus.DISETUJUI -> "DISETUJUI"
                                PeminjamanStatus.DITOLAK -> "DITOLAK"
                            }
                            val tierColor = when (item.status) {
                                PeminjamanStatus.MENUNGGU_VERIFIKASI_RT -> Color(0xFFD97706)
                                PeminjamanStatus.MENUNGGU_VERIFIKASI_SIPRUS -> Color(0xFF2563EB)
                                PeminjamanStatus.DISETUJUI -> Color(0xFF059669)
                                PeminjamanStatus.DITOLAK -> Color(0xFFDC2626)
                            }
                            Box(
                                modifier = Modifier
                                    .background(tierColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(tierText, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = tierColor)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Left border indicator layout
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Sider indicator
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(130.dp)
                                    .background(
                                        when (item.status) {
                                            PeminjamanStatus.MENUNGGU_VERIFIKASI_RT -> Color(0xFFD97706)
                                            PeminjamanStatus.MENUNGGU_VERIFIKASI_SIPRUS -> Color(0xFF2563EB)
                                            PeminjamanStatus.DISETUJUI -> Color(0xFF059669)
                                            PeminjamanStatus.DITOLAK -> Color(0xFFDC2626)
                                        },
                                        RoundedCornerShape(4.dp)
                                    )
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Details
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                // User Block
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(item.namaMahasiswa, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                        Text(item.emailMahasiswa, fontSize = 10.sp, color = Color(0xFF64748B))
                                    }
                                }

                                // Room details
                                Text(
                                    text = "🏢 ${item.ruanganNama}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "Gedung ${item.gedungNama} • Lantai ${item.lantai} (Kapasitas: ${item.kapasitas} Sektor)",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )

                                // Date time
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("📅 ${item.tanggal}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF475569))
                                    Text("⏰ ${item.jamMulai} - ${item.jamSelesai} WIB", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF475569))
                                }

                                // Purpose
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text("TUJUAN ACARA KEGIATAN:", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                        Text("\"${item.tujuan}\"", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
                                    }
                                }
                            }
                        }

                        // Validation Panel Controls Column (Sesuai Tombol di mock-up)
                        val isPendingForMe = (userRole == Role.ADMIN_RT && item.status == PeminjamanStatus.MENUNGGU_VERIFIKASI_RT) ||
                                (userRole == Role.KEPALA_RT && item.status == PeminjamanStatus.MENUNGGU_VERIFIKASI_SIPRUS)

                        if (isPendingForMe) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(12.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Approve button
                                Button(
                                    onClick = {
                                        if (userRole == Role.ADMIN_RT) {
                                            viewModel.adminApprove(item.id)
                                        } else {
                                            viewModel.kepalaApprove(item.id)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Setuju (Lanjutkan)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                // Reject button
                                OutlinedButton(
                                    onClick = {
                                        if (userRole == Role.ADMIN_RT) {
                                            viewModel.adminReject(item.id)
                                        } else {
                                            viewModel.kepalaReject(item.id)
                                        }
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFFEF2F2)),
                                    shape = RoundedCornerShape(6.dp),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFEE2E2))
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFEF4444))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Tolak Pengajuan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                }

                                // Relocate room button for Head of Household level conflict resolution (Alihkan Ruangan)
                                if (userRole == Role.KEPALA_RT) {
                                    Button(
                                        onClick = {
                                            selectedRelocateId = item.id
                                            showAltRoomDropdown = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2F6)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.SwapCalls, contentDescription = null, tint = Color(0xFF334155))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Alihkan Ruangan (Conflict Resolve)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // Dynamic relocation popup trigger
    if (showAltRoomDropdown && selectedRelocateId != null) {
        val nonConflictingRooms = ruanganList.filter { it.kode != "1A204" } // offer alternate rooms for relocation
        AlertDialog(
            onDismissRequest = {
                showAltRoomDropdown = false
                selectedRelocateId = null
            },
            title = { Text("Pilih Ruangan Alternatif") },
            text = {
                Column {
                    Text("Pilih ruangan kosong alternatif untuk dialihkan secara instan guna meredakan bentrok jadwal:", fontSize = 12.sp, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(12.dp))
                    nonConflictingRooms.take(5).forEach { r ->
                        Card(
                            onClick = {
                                selectedRelocateId?.let { id ->
                                    viewModel.kepalaRelocate(id, r.kode)
                                }
                                showAltRoomDropdown = false
                                selectedRelocateId = null
                            },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(r.nama, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Lt ${r.lantai}", fontSize = 10.sp, color = Color(0xFF0284C7))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showAltRoomDropdown = false
                    selectedRelocateId = null
                }) {
                    Text("Batal")
                }
            }
        )
    }
}
