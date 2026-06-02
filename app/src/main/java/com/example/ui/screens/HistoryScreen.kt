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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppViewModel
import com.example.data.Peminjaman
import com.example.data.PeminjamanStatus
import com.example.data.Role

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: AppViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val peminjamanList by viewModel.peminjamanList.collectAsState()
    val scrollState = rememberScrollState()

    var filterKeyword by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("Semua Status") }

    // Mock date entry values
    var filterDateStart by remember { mutableStateOf("mm/dd/yyyy") }
    var filterDateEnd by remember { mutableStateOf("mm/dd/yyyy") }

    var showSuccessExport by remember { mutableStateOf(false) }

    val userRole = currentUser?.role ?: Role.GUEST

    // Filter list
    val filteredList = peminjamanList.filter { p ->
        // If student, they only see their own bookings. If Admin or Siprus, they see all rekapitulasi!
        val belongsToUser = if (userRole == Role.MAHASISWA) {
            p.emailMahasiswa == currentUser?.email
        } else {
            true
        }

        val matchesStatus = when (selectedStatusFilter) {
            "Semua Status" -> true
            "Menunggu RT" -> p.status == PeminjamanStatus.MENUNGGU_VERIFIKASI_RT
            "Menunggu Siprus" -> p.status == PeminjamanStatus.MENUNGGU_VERIFIKASI_SIPRUS
            "Disetujui" -> p.status == PeminjamanStatus.DISETUJUI
            "Ditolak" -> p.status == PeminjamanStatus.DITOLAK
            else -> true
        }

        val matchesKeyword = p.namaMahasiswa.contains(filterKeyword, ignoreCase = true) ||
                p.ruanganNama.contains(filterKeyword, ignoreCase = true) ||
                p.tujuan.contains(filterKeyword, ignoreCase = true)

        belongsToUser && matchesStatus && matchesKeyword
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Report Header Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (userRole == Role.MAHASISWA) "Riwayat Peminjaman Ruangan" else "Laporan Rekapitulasi Peminjaman",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "Pantau terus status permohonan peminjaman prasarana UniRoom Anda.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Mock Export PDF Button matching screenshot
                IconButton(
                    onClick = { showSuccessExport = true },
                    modifier = Modifier.background(Color(0xFF0F172A), RoundedCornerShape(8.dp)),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "Cetak Laporan / PDF")
                }
            }
        }

        // Custom filter drawer block ("Filter Laporan Khusus")
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("☲ Saring Laporan Khusus", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = filterDateStart,
                        onValueChange = { filterDateStart = it },
                        label = { Text("TANGGAL MULAI") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = filterDateEnd,
                        onValueChange = { filterDateEnd = it },
                        label = { Text("TANGGAL SELESAI") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Choose Status Row
                var statusExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = selectedStatusFilter,
                        onValueChange = {},
                        label = { Text("STATUS VERIFIKASI") },
                        readOnly = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { statusExpanded = !statusExpanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        listOf("Semua Status", "Menunggu RT", "Menunggu Siprus", "Disetujui", "Ditolak").forEach { statusLabel ->
                            DropdownMenuItem(
                                text = { Text(statusLabel) },
                                onClick = {
                                    selectedStatusFilter = statusLabel
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                // Word Search
                OutlinedTextField(
                    value = filterKeyword,
                    onValueChange = { filterKeyword = it },
                    placeholder = { Text("Cari kata kunci mahasiswa, keperluan, nama ruang...") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (filterKeyword.isNotEmpty() || selectedStatusFilter != "Semua Status") {
                            IconButton(onClick = {
                                filterKeyword = ""
                                selectedStatusFilter = "Semua Status"
                            }) {
                                Icon(Icons.Default.SettingsBackupRestore, contentDescription = "Reset Filter")
                            }
                        }
                    }
                )
            }
        }

        // List outputs
        Text(
            text = "DAFTAR RESERVASI (${filteredList.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B)
        )

        if (filteredList.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tidak ada riwayat peminjaman sesuai kriteria filter.", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            filteredList.asReversed().forEach { item ->
                HistoryRowCard(item = item)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    if (showSuccessExport) {
        AlertDialog(
            onDismissRequest = { showSuccessExport = false },
            title = { Text("Export Laporan") },
            text = { Text("Dokumen rekapitulasi UniRoom berhasil dibuat! Ekspor data Anda disiapkan dalam cetakan PDF/Excel di lokal penyimpanan Unduhan perangkat.") },
            confirmButton = {
                TextButton(onClick = { showSuccessExport = false }) {
                    Text("Tutup", color = Color(0xFF10B981))
                }
            }
        )
    }
}

@Composable
fun HistoryRowCard(item: Peminjaman) {
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
                        .background(Color(0xFFEFF6FF), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Ref #${item.id.replace("PEM", "")}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                }

                StatusTag(status = item.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.ruanganNama,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Text(
                text = "Gedung ${item.gedungNama} • Lantai ${item.lantai}",
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Circle, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(6.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Pemohon: ${item.namaMahasiswa}",
                    fontSize = 11.sp,
                    color = Color(0xFF475569)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Circle, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(6.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Keperluan: \"${item.tujuan}\"",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(item.tanggal, fontSize = 11.sp, color = Color(0xFF475569))

                    Spacer(modifier = Modifier.width(10.dp))

                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${item.jamMulai} - ${item.jamSelesai} WIB", fontSize = 11.sp, color = Color(0xFF475569))
                }

                Text(
                    text = "Diajukan: ${item.diajukanPada}",
                    fontSize = 9.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}
