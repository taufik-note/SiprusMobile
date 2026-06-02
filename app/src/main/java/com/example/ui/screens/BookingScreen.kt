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

    // Form inputs state
    var selectedTanggal by remember { mutableStateOf("2026-06-01") }
    var selectedJamMulai by remember { mutableStateOf("08:00") }
    var selectedJamSelesai by remember { mutableStateOf("10:00") }
    var selectedGedungId by remember { mutableStateOf("ALL") }
    var searchKeyword by remember { mutableStateOf("") }
    var selectedLantaiFilter by remember { mutableStateOf("Semua") }
    var selectedTipeFilter by remember { mutableStateOf("Semua Tipe") }
    var minKapasitasFilter by remember { mutableStateOf(1) }
    var keperluanText by remember { mutableStateOf("") }

    // Alert Dialog state
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successDialogMessage by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }

    // Filter buildings list
    val selectedGedung = gedungList.find { it.id == selectedGedungId }

    // Multi-tier filtering for the rooms based on UI parameters
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
        // Welcome and Headline block
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ManageSearch, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cari & Cek Ketersediaan Ruangan", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                }
                Text(
                    text = "Mencakup ketersediaan harian dan ketersediaan sepekan terintegrasi.",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Form Section (Langkah 1: Atur Jadwal & Gedung)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("📅 LANGKAH 1: ATUR JADWAL PENGAJUAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5))

                // Date Picker input
                OutlinedTextField(
                    value = selectedTanggal,
                    onValueChange = { selectedTanggal = it },
                    label = { Text("TANGGAL PEMAKAIAN (YYYY-MM-DD)") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = selectedJamMulai,
                        onValueChange = { selectedJamMulai = it },
                        label = { Text("MULAI JAM") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) }
                    )
                    OutlinedTextField(
                        value = selectedJamSelesai,
                        onValueChange = { selectedJamSelesai = it },
                        label = { Text("SELESAI JAM") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) }
                    )
                }

                // Building Selection
                var showGedungDropdown by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = if (selectedGedungId == "ALL") "Semua Gedung Kampus" else "${selectedGedung?.nama} (${selectedGedung?.kode})",
                        onValueChange = {},
                        label = { Text("TARGET GEDUNG") },
                        readOnly = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showGedungDropdown = !showGedungDropdown }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = showGedungDropdown,
                        onDismissRequest = { showGedungDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Semua Gedung Kampus") },
                            onClick = {
                                selectedGedungId = "ALL"
                                showGedungDropdown = false
                            }
                        )
                        gedungList.forEach { g ->
                            DropdownMenuItem(
                                text = { Text("${g.nama} (${g.kode})") },
                                onClick = {
                                    selectedGedungId = g.id
                                    showGedungDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Details Purpose form (Tulis Keperluan)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "✍️ DETAIL PEMINJAMAN RUANG (TULIS KEPERLUAN DISINI)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4F46E5)
                )

                OutlinedTextField(
                    value = keperluanText,
                    onValueChange = { keperluanText = it },
                    placeholder = { Text("cth: Praktikum Fisiologi Kelas A S1 Gizi, Seminar Nasional BEM FIKKES, Ujian Komprehensif") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp),
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Isi rincian keperluan di atas dahulu, lalu tekan tombol hijau 'Ajukan Reservasi' pada salah satu ruangan di bawah.",
                        fontSize = 10.sp,
                        color = Color(0xFF047857),
                        lineHeight = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Filter Controls section (Saring Hasil Instan - tanpa reload)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("⚙️ SARING HASIL INSTAN (TANPA RELOAD)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))

                // Text search keyword filter
                OutlinedTextField(
                    value = searchKeyword,
                    onValueChange = { searchKeyword = it },
                    placeholder = { Text("Cari kelas, jenis, fasilitas (cth: 1A101)") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchKeyword.isNotEmpty()) {
                            IconButton(onClick = { searchKeyword = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    }
                )

                // Floor Filters buttons
                Text("Lantai:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Semua", "Lt 1", "Lt 2", "Lt 3", "Lt 4").forEach { floor ->
                        val isSel = selectedLantaiFilter == floor
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSel) Color(0xFFEFF6FF) else Color(0xFFF1F5F9),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedLantaiFilter = floor }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(floor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isSel) Color(0xFF3B82F6) else Color(0xFF475569))
                        }
                    }
                }

                // Type Filters buttons
                Text("Tipe Ruangan:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Semua Tipe", "Kantor", "Kelas", "KM/WC").forEach { t ->
                        val isSel = selectedTipeFilter == t
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSel) Color(0xFFEFF6FF) else Color(0xFFF1F5F9),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedTipeFilter = t }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(t, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = if (isSel) Color(0xFF3B82F6) else Color(0xFF475569))
                        }
                    }
                }

                // Minimum capacity slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Kapasitas Minimum:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Text("≥ $minKapasitasFilter orang", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                    }
                    Slider(
                        value = minKapasitasFilter.toFloat(),
                        onValueChange = { minKapasitasFilter = it.toInt() },
                        valueRange = 1f..35f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFF59E0B),
                            activeTrackColor = Color(0xFFF59E0B)
                        )
                    )
                }
            }
        }

        // Room Search Results Count label
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📁 HASIL UTAMA (${filteredRuangan.size} RUANGAN TERSEDIA)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569)
            )

            Box(
                modifier = Modifier
                    .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("Hari ini • $selectedJamMulai - $selectedJamSelesai", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
            }
        }

        // Rooms Grid/Card List
        if (filteredRuangan.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.SearchOff, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Ruangan dengan kriteria tersebut tidak ditemukan.", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            }
        } else {
            filteredRuangan.forEach { room ->
                val associatedGedung = gedungList.find { it.id == room.gedungId }
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
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(room.kode, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                            }

                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE0F2FE), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Lantai ${room.lantai}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = room.nama,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )

                        Text(
                            text = associatedGedung?.nama ?: "Gedung UNIMUS",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = room.deskripsi,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                            Text(
                                text = "Kapasitas ${room.kapasitas} orang (Tipe ${room.tipe})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                if (currentUser == null || currentUser?.role == Role.GUEST) {
                                    showErrorDialog = true
                                } else if (keperluanText.isEmpty()) {
                                    keperluanText = "Penggunaan akademis / rapat organisasi"
                                    val success = viewModel.createBooking(
                                        ruanganCode = room.kode,
                                        tanggal = selectedTanggal,
                                        jamMulai = selectedJamMulai,
                                        jamSelesai = selectedJamSelesai,
                                        tujuan = "Penggunaan akademis / rapat organisasi"
                                    )
                                    if (success) {
                                        successDialogMessage = "Pengajuan untuk ${room.kode} berhasil dikirim! Silakan ikuti status persetujuan di tab Riwayat."
                                        showSuccessDialog = true
                                    }
                                } else {
                                    val success = viewModel.createBooking(
                                        ruanganCode = room.kode,
                                        tanggal = selectedTanggal,
                                        jamMulai = selectedJamMulai,
                                        jamSelesai = selectedJamSelesai,
                                        tujuan = keperluanText
                                    )
                                    if (success) {
                                        successDialogMessage = "Pengajuan untuk ${room.kode} berhasil dikirim! Silakan ikuti status persetujuan di tab Riwayat."
                                        showSuccessDialog = true
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ajukan Reservasi Ruang Ini", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // Success dialogue popup
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onNavigateToHistory()
            },
            title = { Text("Peminjaman Berhasil Diajukan") },
            text = { Text(successDialogMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateToHistory()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("OK, Lihat Riwayat")
                }
            }
        )
    }

    // Error dialogue popups (e.g. for guest accounts trying to book rooms)
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Akun Guest Dibatasi") },
            text = { Text("Sebagai Sesi Guest, Anda hanya diperbolehkan mengecek ketersediaan prasarana. Silakan masuk menggunakan akun demo Mahasiswa terlebih dahulu untuk mengajukan reservasi.") },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Text("Mengerti")
                }
            }
        )
    }
}
