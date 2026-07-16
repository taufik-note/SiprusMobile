package com.example.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import com.example.utils.PdfHelper
import com.example.data.Peminjaman
import com.example.data.PeminjamanStatus
import com.example.data.Role
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: AppViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val peminjamanList by viewModel.peminjamanList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
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

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = { viewModel.refreshDataFromServer() },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = if (userRole == Role.MAHASISWA) "Riwayat Saya" else "Rekapitulasi", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                    Text(text = "Pantau status reservasi ruangan Anda.", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF475569))
                }
                if (userRole != Role.MAHASISWA) {
                    IconButton(
                        onClick = { showSuccessExport = true }, 
                        modifier = Modifier.shadow(1.dp, RoundedCornerShape(12.dp)).background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.PictureAsPdf, null, tint = Color.White)
                    }
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
                filteredList.forEach { item ->
                    HistoryRowCard(item = item, onPrintClick = { 
                        PdfHelper.generatePermissionLetter(context, item)
                    })
                }
            }
            Spacer(Modifier.height(40.dp))
        }
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
fun HistoryRowCard(item: Peminjaman, onPrintClick: () -> Unit) {
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

            if (item.status == PeminjamanStatus.DISETUJUI) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onPrintClick,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFECFDF5),
                        contentColor = Color(0xFF065F46)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
                ) {
                    Icon(Icons.Default.Description, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Cetak Surat Izin", fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }

            // Timeline Section
            var isTimelineExpanded by remember { mutableStateOf(false) }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            
            TextButton(
                onClick = { isTimelineExpanded = !isTimelineExpanded },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isTimelineExpanded) "Sembunyikan Detail" else "Lihat Detail & Timeline",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4F46E5)
                    )
                    Icon(
                        imageVector = if (isTimelineExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF4F46E5)
                    )
                }
            }

            AnimatedVisibility(visible = isTimelineExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp, start = 4.dp)) {
                    BookingTimeline(item)
                }
            }
        }
    }
}

@Composable
fun BookingTimeline(item: Peminjaman) {
    val steps = mutableListOf<TimelineStep>()
    val updatedTime = formatTimelineDate(item.updatedAt) ?: formatTimelineDate(item.createdAt)
    
    // 1. Diajukan (Selalu ada)
    steps.add(TimelineStep("Diajukan", "Pesanan telah dikirim ke sistem", formatTimelineDate(item.createdAt), true))

    // 2. Logika Verifikasi RT (Tingkat 1)
    if (item.status == PeminjamanStatus.MENUNGGU_RT) {
        steps.add(TimelineStep("Menunggu Verifikasi RT", "Menunggu pengecekan admin rumah tangga", updatedTime, false, true))
    } else {
        if (item.status == PeminjamanStatus.DITOLAK_RT) {
            steps.add(TimelineStep("Ditolak Admin RT", item.catatanRt ?: "Tidak disetujui oleh unit RT", updatedTime, true, isError = true))
        } else if (item.status == PeminjamanStatus.BUTUH_REVISI) {
            steps.add(TimelineStep("Butuh Revisi", item.catatanRevisi ?: "Data perlu diperbaiki", updatedTime, true, isWarning = true))
        } else {
            // Sudah melewati RT
            steps.add(TimelineStep("Terverifikasi RT", "Telah diperiksa oleh Admin Rumah Tangga", updatedTime, true))
            
            // 3. Logika Verifikasi Kepala (Tingkat 2)
            if (item.status == PeminjamanStatus.MENUNGGU_KEPALA) {
                steps.add(TimelineStep("Menunggu Verifikasi Kepala", "Menunggu persetujuan akhir dari Kepala Siprus", updatedTime, false, true))
            } else if (item.status == PeminjamanStatus.DITOLAK_KEPALA) {
                steps.add(TimelineStep("Ditolak Kepala Siprus", item.catatanKepala ?: "Ditolak pada verifikasi akhir", updatedTime, true, isError = true))
            } else if (item.status == PeminjamanStatus.DISETUJUI) {
                steps.add(TimelineStep("Terverifikasi Kepala", "Persetujuan akhir telah diberikan", updatedTime, true))
                steps.add(TimelineStep("Peminjaman Disetujui", "Ruangan siap digunakan sesuai jadwal", updatedTime, true))
            }
        }
    }

    Column {
        steps.forEachIndexed { index, step ->
            TimelineRow(
                step = step,
                isLast = index == steps.size - 1
            )
        }
    }
}

/** Helper untuk memformat tampilan tanggal dan jam di timeline */
fun formatTimelineDate(dateStr: String?): String? {
    if (dateStr == null || dateStr.isBlank()) return null
    return try {
        // Backend biasanya kirim ISO (2026-07-07T15:21:16.000Z atau 2026-07-07 15:21:16)
        val cleanDate = dateStr.substringBefore(".") // Hilangkan milidetik jika ada
        
        val inputSdf = if (cleanDate.contains("T")) {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        } else {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        }
        
        val date = inputSdf.parse(cleanDate)
        val outputSdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        outputSdf.format(date!!)
    } catch (e: Exception) {
        // Jika gagal parsing, coba format yang lebih sederhana jika memungkinkan
        null
    }
}

data class TimelineStep(
    val title: String,
    val desc: String,
    val time: String?,
    val isCompleted: Boolean,
    val isCurrent: Boolean = false,
    val isError: Boolean = false,
    val isWarning: Boolean = false
)

@Composable
fun TimelineRow(step: TimelineStep, isLast: Boolean) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
            val color = when {
                step.isError -> Color(0xFFEF4444)
                step.isWarning -> Color(0xFFF59E0B)
                step.isCompleted -> Color(0xFF10B981)
                step.isCurrent -> Color(0xFF4F46E5)
                else -> Color(0xFFE2E8F0)
            }
            
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, CircleShape)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(color.copy(alpha = 0.3f))
                )
            }
        }
        
        Column(modifier = Modifier.padding(start = 12.dp, bottom = 20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = step.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (step.isCompleted || step.isCurrent) Color(0xFF1E293B) else Color.Gray
                )
                if (step.time != null) {
                    Text(
                        text = step.time,
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Text(step.desc, fontSize = 11.sp, color = Color(0xFF64748B), lineHeight = 14.sp)
        }
    }
}
