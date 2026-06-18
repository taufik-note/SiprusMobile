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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppViewModel
import com.example.data.Peminjaman
import com.example.data.PeminjamanStatus
import com.example.data.Role
import com.example.ui.component.StatusTag

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
    var showSuccessExport by remember { mutableStateOf(false) }

    val userRole = currentUser?.role ?: Role.GUEST

    val filteredList = peminjamanList.filter { p ->
        val belongsToUser = if (userRole == Role.MAHASISWA) p.emailMahasiswa == currentUser?.email else true
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
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (userRole == Role.MAHASISWA) "Riwayat Saya" else "Rekapitulasi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "Pantau status reservasi ruangan Anda.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            IconButton(
                onClick = { showSuccessExport = true },
                modifier = Modifier.background(Color(0xFF0F172A), RoundedCornerShape(12.dp)),
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.PictureAsPdf, null)
            }
        }

        // Search & Filter
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = filterKeyword,
                    onValueChange = { filterKeyword = it },
                    placeholder = { Text("Cari ruangan...") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF64748B)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF4F46E5))
                )

                var statusExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = selectedStatusFilter,
                        onValueChange = {},
                        label = { Text("Filter Status") },
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { statusExpanded = !statusExpanded }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f).background(Color.White)
                    ) {
                        listOf("Semua Status", "Menunggu RT", "Menunggu Siprus", "Disetujui", "Ditolak").forEach { statusLabel ->
                            DropdownMenuItem(
                                text = { Text(statusLabel) },
                                onClick = { selectedStatusFilter = statusLabel; statusExpanded = false }
                            )
                        }
                    }
                }
            }
        }

        // List
        Text(
            text = "DAFTAR RESERVASI (${filteredList.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            letterSpacing = 1.sp
        )

        if (filteredList.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("Tidak ada data.", color = Color.Gray)
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
            title = { Text("Ekspor Berhasil", fontWeight = FontWeight.Bold) },
            text = { Text("Dokumen PDF berhasil disimpan ke folder Download.") },
            confirmButton = {
                Button(onClick = { showSuccessExport = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
fun HistoryRowCard(item: Peminjaman) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(6.dp)) {
                    Text(
                        text = "Ref #${item.id.replace("PEM", "")}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E40AF)
                    )
                }
                StatusTag(status = item.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = item.ruanganNama, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
            Text(text = "Gedung ${item.gedungNama} • Lt ${item.lantai}", fontSize = 12.sp, color = Color(0xFF64748B))

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(item.tanggal, fontSize = 11.sp, color = Color(0xFF475569))
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Icon(Icons.Default.AccessTime, null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("${item.jamMulai} WIB", fontSize = 11.sp, color = Color(0xFF475569))
            }
        }
    }
}
