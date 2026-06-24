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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val peminjamanList by viewModel.peminjamanList.collectAsState()
    val scrollState = rememberScrollState()

    var filterKeyword by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("Semua Status") }
    var showSuccessExport by remember { mutableStateOf(false) }

    val userRole = currentUser?.role ?: Role.GUEST

    val filteredList = peminjamanList.filter { p ->
        val matchesStatus = when (selectedStatusFilter) {
            "Semua Status" -> true
            "Menunggu RT" -> p.status == PeminjamanStatus.MENUNGGU_RT
            "Menunggu Kepala" -> p.status == PeminjamanStatus.MENUNGGU_KEPALA
            "Disetujui" -> p.status == PeminjamanStatus.DISETUJUI
            "Ditolak" -> p.status == PeminjamanStatus.DITOLAK_RT || p.status == PeminjamanStatus.DITOLAK_KEPALA
            "Revisi" -> p.status == PeminjamanStatus.BUTUH_REVISI
            else -> true
        }
        val matchesKeyword = (p.user?.name ?: "").contains(filterKeyword, ignoreCase = true) ||
                (p.ruang?.nama ?: "").contains(filterKeyword, ignoreCase = true) ||
                p.keperluan.contains(filterKeyword, ignoreCase = true)

        matchesStatus && matchesKeyword
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC)).verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(text = if (userRole == Role.MAHASISWA) "Riwayat Saya" else "Rekapitulasi", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Text(text = "Pantau status reservasi ruangan Anda.", fontSize = 12.sp, color = Color.Gray)
            }
            IconButton(onClick = { showSuccessExport = true }, modifier = Modifier.background(Color(0xFF0F172A), RoundedCornerShape(12.dp))) {
                Icon(Icons.Default.PictureAsPdf, null, tint = Color.White)
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = filterKeyword, onValueChange = { filterKeyword = it }, placeholder = { Text("Cari...") },
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Search, null) }
                )
                var statusExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = selectedStatusFilter, onValueChange = {}, label = { Text("Filter Status") }, readOnly = true,
                        shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { statusExpanded = !statusExpanded }) { Icon(Icons.Default.ArrowDropDown, null) } }
                    )
                    DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                        listOf("Semua Status", "Menunggu RT", "Menunggu Kepala", "Disetujui", "Ditolak", "Revisi").forEach { label ->
                            DropdownMenuItem(text = { Text(label) }, onClick = { selectedStatusFilter = label; statusExpanded = false })
                        }
                    }
                }
            }
        }

        Text(text = "DAFTAR RESERVASI (${filteredList.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

        if (filteredList.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("Tidak ada data.") }
        } else {
            filteredList.asReversed().forEach { item ->
                HistoryRowCard(item = item)
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun HistoryRowCard(item: Peminjaman) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(6.dp)) {
                    Text("Ref #${item.id}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                StatusTag(status = item.status)
            }
            Spacer(Modifier.height(8.dp))
            Text(text = item.ruang?.nama ?: "Ruangan", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(text = "${item.ruang?.gedung?.nama ?: "Gedung"} • Lt ${item.ruang?.lantai ?: "-"}", fontSize = 12.sp, color = Color.Gray)
            
            if (item.catatanRt != null || item.catatanKepala != null) {
                Surface(color = Color(0xFFFFF7ED), shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(top = 8.dp).fillMaxWidth()) {
                    Column(Modifier.padding(8.dp)) {
                        Text("Catatan:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2410C))
                        if (item.catatanRt != null) Text("RT: ${item.catatanRt}", fontSize = 10.sp)
                        if (item.catatanKepala != null) Text("Kepala: ${item.catatanKepala}", fontSize = 10.sp)
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(12.dp))
            Row {
                Icon(Icons.Default.CalendarToday, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(14.dp))
                Text(" ${item.tanggal} ", fontSize = 11.sp)
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Default.AccessTime, null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                Text(" ${item.waktuMulai} - ${item.waktuSelesai}", fontSize = 11.sp)
            }
        }
    }
}
