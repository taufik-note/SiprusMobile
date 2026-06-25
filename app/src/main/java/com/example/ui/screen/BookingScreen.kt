package com.example.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.component.FilterTriggerButton
import com.example.ui.component.HorizontalOptionList
import com.example.data.Role
import com.example.data.Ruangan
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    viewModel: AppViewModel,
    onNavigateToHistory: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val gedungList by viewModel.gedungList.collectAsState()
    val ruanganList by viewModel.ruanganList.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedTanggal by remember { mutableStateOf("2026-06-01") }
    var selectedJamMulai by remember { mutableStateOf("08:00") }
    var selectedJamSelesai by remember { mutableStateOf("10:00") }
    var selectedGedungId by remember { mutableStateOf<Int?>(null) }
    var searchKeyword by remember { mutableStateOf("") }
    var selectedLantaiFilter by remember { mutableStateOf("Semua") }
    var selectedTipeTipe by remember { mutableStateOf("Semua") }
    var minKapasitasFilter by remember { mutableStateOf(1) }
    var keperluanText by remember { mutableStateOf("") }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var successDialogMessage by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }

    var activeTab by remember { mutableStateOf("list") } // "list" atau "weekly"
    val weeklyAvailability by viewModel.weeklyAvailability.collectAsState()
    val isLoadingWeekly by viewModel.isLoadingWeekly.collectAsState()

    // Filter UI State
    var isFilterExpanded by remember { mutableStateOf(false) }
    
    // New Sub-Filter States
    var showLantaiDropdown by remember { mutableStateOf(false) }
    var showTipeDropdown by remember { mutableStateOf(false) }

    // Date Picker State
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    
    var showTimePickerMulai by remember { mutableStateOf(false) }
    var showTimePickerSelesai by remember { mutableStateOf(false) }

    val userRole = currentUser?.role ?: Role.GUEST
    val isGuest = userRole == Role.GUEST
    val selectedGedung = gedungList.find { it.id == selectedGedungId }

    val filteredRuangan = ruanganList.filter { room ->
        val matchGedung = selectedGedungId == null || room.gedungId == selectedGedungId
        val matchLantai = selectedLantaiFilter == "Semua" || room.lantai.toString() == selectedLantaiFilter.replace("Lt ", "")
        val matchKeyword = searchKeyword.isEmpty() || 
                room.nama.contains(searchKeyword, ignoreCase = true) || 
                room.kode.contains(searchKeyword, ignoreCase = true) ||
                (room.jenis.contains(searchKeyword, ignoreCase = true)) ||
                (room.fasilitas?.contains(searchKeyword, ignoreCase = true) == true)
        
        val matchTipe = selectedTipeTipe == "Semua" || room.jenis.contains(selectedTipeTipe, ignoreCase = true)
        val matchKapasitas = room.kapasitas >= minKapasitasFilter
        matchGedung && matchLantai && matchKeyword && matchKapasitas && matchTipe
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column {
                Text(text = "Pencarian Ruangan", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
                Text(text = if (isGuest) "Lihat daftar ruangan UNIMUS." else "Temukan dan reservasi ruangan UNIMUS.", fontSize = 11.sp, color = Color(0xFF64748B))
            }

            // TAB SELECTOR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                TabButton(
                    label = "Daftar",
                    isSelected = activeTab == "list",
                    icon = Icons.Default.List,
                    modifier = Modifier.weight(1f)
                ) { activeTab = "list" }
                
                TabButton(
                    label = "Sepekan",
                    isSelected = activeTab == "weekly",
                    icon = Icons.Default.DateRange,
                    modifier = Modifier.weight(1f)
                ) { 
                    activeTab = "weekly" 
                    viewModel.fetchWeeklyAvailability(
                        selectedTanggal, selectedJamMulai, selectedJamSelesai, 
                        selectedGedungId?.toString(), if(minKapasitasFilter > 1) minKapasitasFilter.toString() else null
                    )
                }
            }

            if (activeTab == "list") {
                // Jadwal & Lokasi Card
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("📅 JADWAL & LOKASI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterTriggerButton(label = selectedTanggal, selected = "", isExpanded = showDatePicker, modifier = Modifier.weight(1f)) { showDatePicker = true }
                            FilterTriggerButton(label = selectedJamMulai, selected = "", isExpanded = showTimePickerMulai, modifier = Modifier.weight(0.7f)) { showTimePickerMulai = true }
                            FilterTriggerButton(label = selectedJamSelesai, selected = "", isExpanded = showTimePickerSelesai, modifier = Modifier.weight(0.7f)) { showTimePickerSelesai = true }
                        }
                        var showGedungDropdown by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            FilterTriggerButton(label = if (selectedGedungId == null) "SEMUA GEDUNG" else selectedGedung?.nama?.uppercase() ?: "GEDUNG", selected = "", isExpanded = showGedungDropdown, modifier = Modifier.fillMaxWidth()) { showGedungDropdown = !showGedungDropdown }
                            DropdownMenu(expanded = showGedungDropdown, onDismissRequest = { showGedungDropdown = false }, modifier = Modifier.fillMaxWidth(0.9f)) {
                                DropdownMenuItem(text = { Text("Semua Gedung") }, onClick = { selectedGedungId = null; showGedungDropdown = false })
                                gedungList.forEach { g -> DropdownMenuItem(text = { Text(g.nama) }, onClick = { selectedGedungId = g.id; showGedungDropdown = false }) }
                            }
                        }
                    }
                }

                // Filter Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(onClick = { isFilterExpanded = !isFilterExpanded }, color = Color(0xFFF1F5F9), shape = RoundedCornerShape(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Tune, null, tint = Color(0xFF6366F1), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("SARING HASIL PENCARIAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                            }
                            Icon(imageVector = if (isFilterExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF64748B))
                        }
                    }
                    AnimatedVisibility(visible = isFilterExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                        Column(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(value = searchKeyword, onValueChange = { searchKeyword = it }, placeholder = { Text("Cari nama kelas, jenis, fasilitas...", fontSize = 12.sp) }, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().height(48.dp), leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp)) }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF4F46E5), focusedContainerColor = Color(0xFFF8FAFC), unfocusedContainerColor = Color(0xFFF8FAFC)))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterTriggerButton(label = "LANTAI", selected = "", isExpanded = showLantaiDropdown, modifier = Modifier.weight(1f)) { showLantaiDropdown = !showLantaiDropdown; showTipeDropdown = false }
                                FilterTriggerButton(label = "TIPE", selected = "", isExpanded = showTipeDropdown, modifier = Modifier.weight(1f)) { showTipeDropdown = !showTipeDropdown; showLantaiDropdown = false }
                                Surface(onClick = { minKapasitasFilter = if (minKapasitasFilter == 30) 1 else 30 }, color = if (minKapasitasFilter >= 30) Color(0xFFFFF7ED) else Color(0xFFF1F5F9), shape = RoundedCornerShape(10.dp), modifier = Modifier.height(40.dp)) {
                                    Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocalFireDepartment, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("≥ 30", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            AnimatedVisibility(visible = showLantaiDropdown) { HorizontalOptionList(options = listOf("Semua", "0", "1", "2"), selected = selectedLantaiFilter.replace("Lt ", ""), onSelected = { selectedLantaiFilter = if(it == "Semua") "Semua" else "Lt $it"; showLantaiDropdown = false }) }
                            AnimatedVisibility(visible = showTipeDropdown) { HorizontalOptionList(options = listOf("Semua", "Kelas", "Lab", "Rapat", "Aula"), selected = selectedTipeTipe, onSelected = { selectedTipeTipe = it; showTipeDropdown = false }) }
                        }
                    }
                }

                if (!isGuest) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FactCheck, null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("DETAIL KEPERLUAN", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            OutlinedTextField(value = keperluanText, onValueChange = { keperluanText = it }, placeholder = { Text("Tulis rincian di sini...", fontSize = 12.sp) }, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color(0xFFF8FAFC), unfocusedContainerColor = Color(0xFFF8FAFC), focusedBorderColor = Color(0xFF10B981)))
                        }
                    }
                }

                Text("Hasil Pencarian (${filteredRuangan.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                filteredRuangan.forEach { room ->
                    RoomSelectionCard(
                        room = room,
                        gedungName = gedungList.find { it.id == room.gedungId }?.nama ?: "Gedung",
                        isGuest = isGuest,
                        onBookClick = {
                            if (isGuest) { showErrorDialog = true } 
                            else if (keperluanText.isBlank()) { scope.launch { snackbarHostState.showSnackbar("Mohon isi rincian KEPERLUAN terlebih dahulu.") } } 
                            else {
                                viewModel.createBooking(room.id, selectedTanggal, selectedJamMulai, selectedJamSelesai, keperluanText) { success, msg ->
                                    if (success) { successDialogMessage = msg; showSuccessDialog = true } 
                                    else { scope.launch { snackbarHostState.showSnackbar("Gagal: $msg") } }
                                }
                            }
                        }
                    )
                }
            } else {
                // WEEKLY VIEW
                if (isLoadingWeekly) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF4F46E5))
                    }
                } else {
                    WeeklyAvailabilityTable(
                        dates = getNext7Days(selectedTanggal),
                        rooms = filteredRuangan,
                        availability = weeklyAvailability,
                        onCellClick = { date, room ->
                            selectedTanggal = date
                            activeTab = "list"
                        }
                    )
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    // DIALOGS & PICKERS
    if (showSuccessDialog) { AlertDialog(onDismissRequest = { showSuccessDialog = false; onNavigateToHistory() }, title = { Text("Sukses") }, text = { Text(successDialogMessage) }, confirmButton = { Button(onClick = { showSuccessDialog = false; onNavigateToHistory() }) { Text("Lihat Riwayat") } }) }
    if (showErrorDialog) { AlertDialog(onDismissRequest = { showErrorDialog = false }, title = { Text("Login Diperlukan") }, text = { Text("Silakan login sebagai Mahasiswa untuk melakukan reservasi.") }, confirmButton = { Button(onClick = { showErrorDialog = false }) { Text("Mengerti") } }) }
    if (showDatePicker) { DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { millis -> val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); selectedTanggal = formatter.format(Date(millis)) }; showDatePicker = false }) { Text("Set") } }, dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }) { DatePicker(state = datePickerState) } }
    if (showTimePickerMulai) { WheelTimePickerDialog(title = "Pilih Jam Mulai", initialTime = selectedJamMulai, onDismiss = { showTimePickerMulai = false }, onConfirm = { time -> selectedJamMulai = time; showTimePickerMulai = false }) }
    if (showTimePickerSelesai) { WheelTimePickerDialog(title = "Pilih Jam Selesai", initialTime = selectedJamSelesai, onDismiss = { showTimePickerSelesai = false }, onConfirm = { time -> selectedJamSelesai = time; showTimePickerSelesai = false }) }
}

@Composable
fun TabButton(label: String, isSelected: Boolean, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) Color.White else Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        shadowElevation = if (isSelected) 2.dp else 0.dp,
        modifier = modifier.height(40.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = if (isSelected) Color(0xFF4F46E5) else Color(0xFF64748B), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFF1E293B) else Color(0xFF64748B))
        }
    }
}

@Composable
fun WeeklyAvailabilityTable(
    dates: List<String>,
    rooms: List<Ruangan>,
    availability: Map<String, List<Ruangan>>,
    onCellClick: (String, Ruangan) -> Unit
) {
    val horizontalScrollState = rememberScrollState()
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
            // Header
            Row(modifier = Modifier.background(Color(0xFFF8FAFC)).padding(vertical = 12.dp)) {
                Box(modifier = Modifier.width(120.dp).padding(start = 16.dp)) {
                    Text("RUANGAN", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B))
                }
                dates.forEach { date ->
                    Box(modifier = Modifier.width(100.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(formatDayIndo(date), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Text(date.substring(5), fontSize = 9.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }

            // Body
            rooms.forEach { room ->
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                    Column(modifier = Modifier.width(120.dp).padding(start = 16.dp)) {
                        Text(room.kode, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5))
                        Text(room.nama, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B), maxLines = 1)
                    }
                    
                    dates.forEach { date ->
                        val isAvailable = availability[date]?.any { it.id == room.id } ?: false
                        Box(modifier = Modifier.width(100.dp), contentAlignment = Alignment.Center) {
                            if (isAvailable) {
                                Surface(
                                    onClick = { onCellClick(date, room) },
                                    color = Color(0xFFECFDF5),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.2f))
                                ) {
                                    Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Check, null, tint = Color(0xFF10B981), modifier = Modifier.size(10.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("TERSEDIA", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF065F46))
                                    }
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Close, null, tint = Color(0xFFEF4444).copy(alpha = 0.3f), modifier = Modifier.size(10.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("PENUH", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444).copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getNext7Days(startDate: String): List<String> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dates = mutableListOf<String>()
    val calendar = Calendar.getInstance()
    try {
        calendar.time = sdf.parse(startDate) ?: Date()
        for (i in 0 until 7) {
            dates.add(sdf.format(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
    } catch (e: Exception) { }
    return dates
}

fun formatDayIndo(dateStr: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(dateStr)
        SimpleDateFormat("EEEE", Locale("id", "ID")).format(date!!)
    } catch (e: Exception) { dateStr }
}

@Composable
fun WheelTimePickerDialog(title: String, initialTime: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val initialHour = initialTime.substringBefore(":").toIntOrNull() ?: 8
    val initialMinute = initialTime.substringAfter(":").toIntOrNull() ?: 0
    var selectedHour by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }
    var manualHour by remember { mutableStateOf(String.format("%02d", initialHour)) }
    var manualMinute by remember { mutableStateOf(String.format("%02d", initialMinute)) }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 20.dp))
                Row(modifier = Modifier.fillMaxWidth().height(160.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    WheelPicker(count = 24, startIndex = initialHour, onItemSelected = { it -> selectedHour = it; manualHour = String.format("%02d", it) }, modifier = Modifier.weight(1f))
                    Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                    WheelPicker(count = 60, startIndex = initialMinute, onItemSelected = { it -> selectedMinute = it; manualMinute = String.format("%02d", it) }, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Button(onClick = { val h = manualHour.toIntOrNull()?.coerceIn(0, 23) ?: selectedHour; val m = manualMinute.toIntOrNull()?.coerceIn(0, 59) ?: selectedMinute; onConfirm(String.format("%02d:%02d", h, m)) }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))) { Text("Simpan") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelPicker(count: Int, startIndex: Int, onItemSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    val itemHeight = 40.dp
    val totalItems = 10000
    val centerIndex = totalItems / 2 - (totalItems / 2 % count) + startIndex
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = centerIndex - 1)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    LaunchedEffect(listState.isScrollInProgress) { if (!listState.isScrollInProgress) { val selectedIndex = listState.firstVisibleItemIndex + 1; onItemSelected(selectedIndex % count) } }
    Box(modifier = modifier.height(itemHeight * 3), contentAlignment = Alignment.Center) {
        Surface(modifier = Modifier.fillMaxWidth().height(itemHeight), color = Color(0xFFF1F5F9), shape = RoundedCornerShape(8.dp)) {}
        LazyColumn(state = listState, flingBehavior = snapFlingBehavior, modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            items(totalItems) { index ->
                val actualIndex = index % count
                Box(modifier = Modifier.fillMaxWidth().height(itemHeight), contentAlignment = Alignment.Center) {
                    val isSelected = listState.firstVisibleItemIndex + 1 == index
                    Text(text = String.format("%02d", actualIndex), fontSize = if (isSelected) 20.sp else 16.sp, fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal, color = if (isSelected) Color(0xFF4F46E5) else Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun RoomSelectionCard(room: Ruangan, gedungName: String, isGuest: Boolean, onBookClick: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(room.kode, color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    Text(room.nama, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(gedungName, fontSize = 11.sp, color = Color.Gray)
                }
                Surface(color = Color(0xFFF0FDFA), shape = RoundedCornerShape(8.dp)) {
                    Text("Lt ${room.lantai}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 9.sp, color = Color(0xFF0D9488))
                }
            }
            Spacer(Modifier.height(8.dp))
            Row {
                Icon(Icons.Default.People, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                Text(" Kap: ${room.kapasitas}", fontSize = 11.sp)
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Default.Category, null, tint = Color(0xFF6366F1), modifier = Modifier.size(14.dp))
                Text(" ${room.jenis}", fontSize = 11.sp)
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onBookClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(vertical = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isGuest) Color.Gray else Color(0xFF4F46E5))) {
                Text(if (isGuest) "Login untuk Meminjam" else "Ajukan Reservasi", fontSize = 12.sp)
            }
        }
    }
}
