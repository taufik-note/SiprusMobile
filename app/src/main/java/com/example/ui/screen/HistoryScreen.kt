package com.example.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.component.FilterTriggerButton
import com.example.ui.component.HorizontalOptionList
import com.example.ui.component.StatusTag
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
    var selectedStatusFilter by remember { mutableStateOf("Semua") }
    var isFilterExpanded by remember { mutableStateOf(false) }
    var showStatusOptions by remember { mutableStateOf(false) }
    var showSuccessExport by remember { mutableStateOf(false) }

    val userRole = currentUser?.role ?: Role.GUEST

    val filteredList = peminjamanList.filter { p ->
        val matchesStatus = when (selectedStatusFilter) {
            "Semua" -> true
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
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(text = if (userRole == Role.MAHASISWA) "Riwayat Saya" else "Rekapitulasi", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                Text(text = "Pantau status reservasi ruangan Anda.", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF475569))
            }
            IconButton(
                onClick = { showSuccessExport = true }, 
                modifier = Modifier.shadow(1.dp, RoundedCornerShape(12.dp)).background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.PictureAsPdf, null, tint = Color.White)
            }
        }

        // Filter Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                onClick = { isFilterExpanded = !isFilterExpanded },
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, null, tint = Color(0xFF6366F1), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("SARING RIWAYAT", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                    }
                    Icon(
                        imageVector = if (isFilterExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFF1E293B)
                    )
                }
            }

            AnimatedVisibility(
                visible = isFilterExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(16.dp))
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = filterKeyword,
                        onValueChange = { filterKeyword = it },
                        placeholder = { Text("Cari nama, ruangan, keperluan...", fontSize = 13.sp, color = Color.Gray) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp), tint = Color.Black) },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold, color = Color.Black),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4F46E5),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor = Color.Black
                        )
                    )

                    FilterTriggerButton(
                        label = "STATUS: ${selectedStatusFilter.uppercase()}",
                        selected = "",
                        isExpanded = showStatusOptions,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        showStatusOptions = !showStatusOptions
                    }

                    AnimatedVisibility(visible = showStatusOptions) {
                        HorizontalOptionList(
                            options = listOf("Semua", "Menunggu RT", "Menunggu Kepala", "Disetujui", "Ditolak", "Revisi"),
                            selected = selectedStatusFilter,
                            onSelected = { selectedStatusFilter = it; showStatusOptions = false }
                        )
                    }
                }
            }
        }

        Text(text = "DAFTAR RESERVASI (${filteredList.size})", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B))

        if (filteredList.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            ) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Tidak ada data riwayat reservasi.", color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        } else {
            filteredList.asReversed().forEach { item ->
                HistoryRowCard(item = item)
            }
        }
        Spacer(Modifier.height(40.dp))
    }

    if (showSuccessExport) {
        AlertDialog(
            onDismissRequest = { showSuccessExport = false },
            title = { Text("Export Berhasil", fontWeight = FontWeight.Black) },
            text = { Text("Riwayat peminjaman telah dikonversi ke format PDF.", color = Color.Black) },
            confirmButton = {
                Button(onClick = { showSuccessExport = false }) {
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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(8.dp), shadowElevation = 1.dp) {
                    Text("REF #${item.id}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp), fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                }
                StatusTag(status = item.status)
            }
            Spacer(Modifier.height(12.dp))
            Text(text = item.ruang?.nama ?: "Ruangan", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.Black)
            Text(text = "${item.ruang?.gedung?.nama ?: "Gedung"} • Lantai ${item.ruang?.lantai ?: "-"}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
            
            if (!item.keperluan.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Keperluan: ${item.keperluan}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            }

            if (item.catatanRt != null || item.catatanKepala != null || item.catatanRevisi != null) {
                Surface(color = Color(0xFFFFF7ED), shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(top = 12.dp).fillMaxWidth(), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFED7AA))) {
                    Column(Modifier.padding(12.dp)) {
                        Text("CATATAN VALIDASI:", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFC2410C))
                        Spacer(Modifier.height(4.dp))
                        if (item.catatanRt != null) Text("RT: ${item.catatanRt}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF431407))
                        if (item.catatanKepala != null) Text("Siprus: ${item.catatanKepala}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF431407))
                        if (item.catatanRevisi != null) Text("Revisi: ${item.catatanRevisi}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF431407))
                    }
                }
            }
            
            HorizontalDivider(Modifier.padding(vertical = 16.dp), color = Color(0xFFF1F5F9))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, null, tint = Color(0xFF4F46E5), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("${item.tanggal}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.AccessTime, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("${item.waktuMulai} - ${item.waktuSelesai}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
            }
        }
    }
}
