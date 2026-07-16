package com.example.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import com.example.data.Peminjaman
import com.example.ui.component.FilterTriggerButton
import com.example.ui.component.HorizontalOptionList
import com.example.ui.component.StatusTag
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: AppViewModel
) {
    val peminjamanList by viewModel.peminjamanList.collectAsState()
    val gedungList by viewModel.gedungList.collectAsState()
    val scrollState = rememberScrollState()

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("Semua Status") }
    var selectedGedungId by remember { mutableStateOf<Int?>(null) }
    val sdfUI = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val currentDayUI = remember { sdfUI.format(Date()) }

    var startDate by remember { mutableStateOf(currentDayUI) }
    var endDate by remember { mutableStateOf(currentDayUI) }

    // Sub-Filter States
    var showGedungDropdown by remember { mutableStateOf(false) }
    var showStatusDropdown by remember { mutableStateOf(false) }

    // Date Picker States
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()

    val selectedGedungName = gedungList.find { it.id == selectedGedungId }?.nama ?: "Semua Gedung"

    // Logic to filter the list based on states
    val filteredList = peminjamanList.filter { item ->
        val matchSearch = searchQuery.isEmpty() || 
                (item.user?.name?.contains(searchQuery, ignoreCase = true) == true) ||
                (item.ruang?.nama?.contains(searchQuery, ignoreCase = true) == true) ||
                (item.keperluan.contains(searchQuery, ignoreCase = true))
        
        val matchStatus = statusFilter == "Semua Status" || 
                (item.status.name.replace("_", " ").contains(statusFilter, ignoreCase = true)) ||
                statusFilter.uppercase().replace(" ", "_").let { uiStatus ->
                    item.status.name == uiStatus
                }

        val matchGedung = selectedGedungId == null || item.ruang?.gedungId == selectedGedungId
        
        // Improve date comparison by normalizing formats
        val matchDate = try {
            val itemDate = item.tanggal // Assuming YYYY-MM-DD from backend
            
            val startMatch = if (startDate == "dd/mm/yyyy") true else {
                // Convert UI date (dd/MM/yyyy) to ISO (yyyy-MM-dd) for comparison
                val parts = startDate.split("/")
                val isoStart = "${parts[2]}-${parts[1]}-${parts[0]}"
                itemDate >= isoStart
            }
            
            val endMatch = if (endDate == "dd/mm/yyyy") true else {
                val parts = endDate.split("/")
                val isoEnd = "${parts[2]}-${parts[1]}-${parts[0]}"
                itemDate <= isoEnd
            }
            
            startMatch && endMatch
        } catch (e: Exception) {
            true // Fallback if date parsing fails
        }

        matchSearch && matchStatus && matchGedung && matchDate
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
        Column {
            Text(
                text = "Laporan",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1E293B)
            )
            Text(
                text = "Rekapitulasi penggunaan prasarana.",
                fontSize = 14.sp,
                color = Color(0xFF64748B)
            )
        }

        // Export Actions (Mobile Layout)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Analytics, null, tint = Color(0xFF4F46E5), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ekspor Data", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Refresh, null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF059669),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.TableChart, null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Excel", fontSize = 12.sp, color = Color.White)
                    }
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("PDF", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }

        // Filters (Mobile Layout)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FilterList, null, tint = Color(0xFF4F46E5), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("KRITERIA PENYARINGAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
                }
                
                // Date Pickers Row (Compact centered design)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterTriggerButton(
                        label = if (startDate == "dd/mm/yyyy") "TGL MULAI" else startDate,
                        selected = "",
                        isExpanded = showStartDatePicker,
                        modifier = Modifier.weight(1f)
                    ) {
                        showStartDatePicker = true
                    }
                    FilterTriggerButton(
                        label = if (endDate == "dd/mm/yyyy") "TGL AKHIR" else endDate,
                        selected = "",
                        isExpanded = showEndDatePicker,
                        modifier = Modifier.weight(1f)
                    ) {
                        showEndDatePicker = true
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterTriggerButton(
                        label = "GEDUNG",
                        selected = "",
                        isExpanded = showGedungDropdown,
                        modifier = Modifier.weight(1f)
                    ) {
                        showGedungDropdown = !showGedungDropdown
                        showStatusDropdown = false
                    }

                    FilterTriggerButton(
                        label = "STATUS",
                        selected = "",
                        isExpanded = showStatusDropdown,
                        modifier = Modifier.weight(1f)
                    ) {
                        showStatusDropdown = !showStatusDropdown
                        showGedungDropdown = false
                    }
                }

                // Horizontal Option Lists
                AnimatedVisibility(visible = showGedungDropdown) {
                    val options = listOf("Semua Gedung") + gedungList.map { it.nama }
                    HorizontalOptionList(
                        options = options,
                        selected = selectedGedungName,
                        onSelected = { name: String ->
                            selectedGedungId = gedungList.find { it.nama == name }?.id
                            showGedungDropdown = false
                        }
                    )
                }

                AnimatedVisibility(visible = showStatusDropdown) {
                    val options = listOf("Semua Status", "Menunggu RT", "Menunggu Kepala", "Disetujui", "Ditolak", "Revisi")
                    HorizontalOptionList(
                        options = options,
                        selected = statusFilter,
                        onSelected = { status: String ->
                            statusFilter = status
                            showStatusDropdown = false
                        }
                    )
                }

                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari mahasiswa, ruangan, atau keperluan...", fontSize = 12.sp, color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp)) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Color(0xFF4F46E5), 
                        focusedContainerColor = Color.White, 
                        unfocusedContainerColor = Color.White
                    )
                )

                OutlinedButton(
                    onClick = { 
                        searchQuery = ""
                        statusFilter = "Semua Status"
                        selectedGedungId = null
                        startDate = "dd/mm/yyyy"
                        endDate = "dd/mm/yyyy"
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Bersihkan Filter", fontSize = 12.sp)
                }
            }
        }

        // List Header
        Text(
            text = "HASIL REKAPITULASI (${filteredList.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            letterSpacing = 1.sp
        )

        // Result Cards
        if (filteredList.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Text("Data tidak ditemukan.", color = Color.Gray)
            }
        } else {
            filteredList.asReversed().forEach { item ->
                MobileReportCard(item)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // Date Picker Dialogs
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDatePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        startDate = formatter.format(Date(millis))
                    }
                    showStartDatePicker = false
                }) { Text("Set") }
            },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = startDatePickerState) }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDatePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        endDate = formatter.format(Date(millis))
                    }
                    showEndDatePicker = false
                }) { Text("Set") }
            },
            dismissButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = endDatePickerState) }
    }
}

@Composable
fun MobileReportCard(item: Peminjaman) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(6.dp)) {
                    Text(
                        text = "ID #${item.id}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }
                StatusTag(status = item.status)
            }

            Spacer(Modifier.height(12.dp))
            
            Text(item.user?.name ?: "Mahasiswa", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
            Text(item.user?.email ?: "", fontSize = 11.sp, color = Color(0xFF64748B))
            
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("RUANGAN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                    Text(item.ruang?.nama ?: "Ruangan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(item.ruang?.gedung?.nama ?: "Gedung", fontSize = 11.sp, color = Color(0xFF64748B))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("WAKTU", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                    Text(item.tanggal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("${item.waktuMulai} WIB", fontSize = 11.sp, color = Color(0xFF64748B))
                }
            }

            Spacer(Modifier.height(12.dp))
            Surface(color = Color(0xFFF8FAFC), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("KEPERLUAN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                    Text("\"${item.keperluan}\"", fontSize = 12.sp, color = Color(0xFF475569))
                }
            }
        }
    }
}
