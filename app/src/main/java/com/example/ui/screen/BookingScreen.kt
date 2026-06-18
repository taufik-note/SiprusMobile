package com.example.ui.screen

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppViewModel
import com.example.data.Gedung
import com.example.data.Role
import com.example.data.Ruangan

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

    var selectedTanggal by remember { mutableStateOf("2026-06-01") }
    var selectedJamMulai by remember { mutableStateOf("08:00") }
    var selectedJamSelesai by remember { mutableStateOf("10:00") }
    var selectedGedungId by remember { mutableStateOf("ALL") }
    var searchKeyword by remember { mutableStateOf("") }
    var selectedLantaiFilter by remember { mutableStateOf("Semua") }
    var selectedTipeFilter by remember { mutableStateOf("Semua Tipe") }
    var minKapasitasFilter by remember { mutableStateOf(1) }
    var keperluanText by remember { mutableStateOf("") }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var successDialogMessage by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }

    val userRole = currentUser?.role ?: Role.GUEST
    val isGuest = userRole == Role.GUEST
    val selectedGedung = gedungList.find { it.id == selectedGedungId }

    val filteredRuangan = ruanganList.filter { room ->
        val matchGedung = selectedGedungId == "ALL" || room.gedungId == selectedGedungId
        val matchLantai = selectedLantaiFilter == "Semua" || room.lantai.toString() == selectedLantaiFilter.replace("Lt ", "")
        val matchTipe = selectedTipeFilter == "Semua Tipe" || room.tipe.lowercase() == selectedTipeFilter.lowercase()
        val matchKeyword = searchKeyword.isEmpty() || room.nama.contains(searchKeyword, ignoreCase = true) || room.kode.contains(searchKeyword, ignoreCase = true)
        val matchKapasitas = room.kapasitas >= minKapasitasFilter
        matchGedung && matchLantai && matchTipe && matchKeyword && matchKapasitas
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Headline
        Column {
            Text(
                text = "Pencarian Ruangan",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = if (isGuest) "Lihat daftar ruangan UNIMUS yang tersedia." else "Temukan dan reservasi ruangan UNIMUS secara instan.",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }

        // Schedule & Building Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("📅 ATUR JADWAL & LOKASI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5), letterSpacing = 1.sp)

                OutlinedTextField(
                    value = searchKeyword,
                    onValueChange = { searchKeyword = it },
                    placeholder = { Text("Cari ruangan...") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF4F46E5)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF4F46E5))
                )

                OutlinedTextField(
                    value = selectedTanggal,
                    onValueChange = { selectedTanggal = it },
                    label = { Text("Tanggal Pemakaian") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.DateRange, null, tint = Color(0xFF4F46E5)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF4F46E5))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = selectedJamMulai,
                        onValueChange = { selectedJamMulai = it },
                        label = { Text("Mulai") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.AccessTime, null, tint = Color(0xFF4F46E5)) }
                    )
                    OutlinedTextField(
                        value = selectedJamSelesai,
                        onValueChange = { selectedJamSelesai = it },
                        label = { Text("Selesai") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.AccessTime, null, tint = Color(0xFF4F46E5)) }
                    )
                }

                var showGedungDropdown by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = if (selectedGedungId == "ALL") "Semua Gedung" else "${selectedGedung?.nama}",
                        onValueChange = {},
                        label = { Text("Target Gedung") },
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Business, null, tint = Color(0xFF4F46E5)) },
                        trailingIcon = {
                            IconButton(onClick = { showGedungDropdown = !showGedungDropdown }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = showGedungDropdown,
                        onDismissRequest = { showGedungDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.8f).background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Semua Gedung Kampus") },
                            onClick = { selectedGedungId = "ALL"; showGedungDropdown = false }
                        )
                        gedungList.forEach { g ->
                            DropdownMenuItem(
                                text = { Text("${g.nama} (${g.kode})") },
                                onClick = { selectedGedungId = g.id; showGedungDropdown = false }
                            )
                        }
                    }
                }
            }
        }

        // Purpose Card (Hidden for Guest)
        if (!isGuest) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("✍️ DETAIL KEPERLUAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5), letterSpacing = 1.sp)

                    OutlinedTextField(
                        value = keperluanText,
                        onValueChange = { keperluanText = it },
                        placeholder = { Text("cth: Rapat Organisasi BEM, Praktikum Lab...") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF4F46E5))
                    )
                }
            }
        }

        // Search Result Title
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hasil Pencarian (${filteredRuangan.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155)
            )
            Icon(Icons.Default.Tune, null, tint = Color(0xFF4F46E5), modifier = Modifier.size(20.dp))
        }

        // Room List
        if (filteredRuangan.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("Ruangan tidak ditemukan.", color = Color.Gray)
            }
        } else {
            filteredRuangan.forEach { room ->
                RoomSelectionCard(
                    room = room,
                    gedungName = gedungList.find { it.id == room.gedungId }?.nama ?: "Gedung UNIMUS",
                    isGuest = isGuest,
                    onBookClick = {
                        if (isGuest) {
                            showErrorDialog = true
                        } else {
                            val tujuan = if (keperluanText.isEmpty()) "Kegiatan Mahasiswa" else keperluanText
                            viewModel.createBooking(
                                room.kode,
                                selectedTanggal,
                                selectedJamMulai,
                                selectedJamSelesai,
                                tujuan
                            ) { success, message ->
                                if (success) {
                                    successDialogMessage = message
                                    showSuccessDialog = true
                                } else {
                                    // Anda bisa menambahkan toast atau error dialog di sini jika gagal
                                }
                            }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false; onNavigateToHistory() },
            title = { Text("Sukses", fontWeight = FontWeight.Bold) },
            text = { Text(successDialogMessage) },
            confirmButton = {
                Button(onClick = { showSuccessDialog = false; onNavigateToHistory() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))) {
                    Text("Lihat Riwayat")
                }
            }
        )
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Login Diperlukan", fontWeight = FontWeight.Bold) },
            text = { Text("Sesi Guest hanya diizinkan untuk memantau ketersediaan. Silakan login sebagai Mahasiswa untuk melakukan reservasi.") },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))) {
                    Text("Mengerti")
                }
            }
        )
    }
}

@Composable
fun RoomSelectionCard(
    room: Ruangan,
    gedungName: String,
    isGuest: Boolean,
    onBookClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = Color(0xFFEEF2F6),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = room.kode,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3B82F6)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = room.nama, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
                    Text(text = gedungName, fontSize = 12.sp, color = Color(0xFF64748B))
                }
                
                Surface(
                    color = Color(0xFFF0FDFA),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCCFBF1))
                ) {
                    Text(
                        text = "Lt ${room.lantai}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D9488)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.People, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Kapasitas: ${room.kapasitas} Orang", fontSize = 12.sp, color = Color(0xFF475569))
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.Category, null, tint = Color(0xFF6366F1), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(room.tipe, fontSize = 12.sp, color = Color(0xFF475569))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBookClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGuest) Color(0xFF94A3B8) else Color(0xFF4F46E5)
                )
            ) {
                Text(
                    if (isGuest) "Login untuk Meminjam" else "Ajukan Reservasi Ruang", 
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
