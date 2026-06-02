package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.Ruangan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterDataScreen(
    viewModel: AppViewModel
) {
    val gedungList by viewModel.gedungList.collectAsState()
    val ruanganList by viewModel.ruanganList.collectAsState()
    val scrollState = rememberScrollState()

    // Tab control: 0 = Gedung, 1 = Ruangan
    var activeTab by remember { mutableStateOf(0) }

    // Dialog trigger state
    var showAddGedungDialog by remember { mutableStateOf(false) }
    var showAddRuanganDialog by remember { mutableStateOf(false) }

    // Dialog inputs
    var newGedungKode by remember { mutableStateOf("") }
    var newGedungNama by remember { mutableStateOf("") }
    var newGedungLokasi by remember { mutableStateOf("") }

    var newRuanganKode by remember { mutableStateOf("") }
    var newRuanganNama by remember { mutableStateOf("") }
    var newRuanganGedungId by remember { mutableStateOf("G1") }
    var newRuanganLantai by remember { mutableStateOf("1") }
    var newRuanganKapasitas by remember { mutableStateOf("30") }
    var newRuanganTipe by remember { mutableStateOf("Kelas") }
    var newRuanganDeskripsi by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Master Data Header
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (activeTab == 0) "Master Data Gedung" else "Master Data Ruangan",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = if (activeTab == 0) "Kelola data fisik gedung, nama bangunan, kode identitas, dan lokasi prasarana di lingkungan UNIMUS."
                           else "Daftarkan ruangan kelas teori, laboratorium praktikum, aula serbaguna, hingga asrama prasarana akademik.",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Action additions card trigger
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Segmented tab switchers
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { activeTab = 0 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeTab == 0) Color(0xFF4F46E5) else Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "🏢 Master Gedung",
                            color = if (activeTab == 0) Color.White else Color(0xFF475569),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { activeTab = 1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeTab == 1) Color(0xFF4F46E5) else Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "🏫 Master Ruangan",
                            color = if (activeTab == 1) Color.White else Color(0xFF475569),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Add Button (Tombah Gedung Baru / Room Baru)
                Button(
                    onClick = {
                        if (activeTab == 0) showAddGedungDialog = true else showAddRuanganDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (activeTab == 0) "Gedung" else "Ruang", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // List output fields
        if (activeTab == 0) {
            gedungList.forEach { g ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFEEF2F6), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(g.kode, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(g.nama, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("📍 ${g.lokasi}", fontSize = 11.sp, color = Color(0xFF64748B))
                        }

                        // Action delete button
                        IconButton(onClick = { viewModel.deleteGedung(g.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Dihapus", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }
        } else {
            ruanganList.forEach { r ->
                val associatedG = gedungList.find { it.id == r.gedungId }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFEFF6FF), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(r.kode, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(r.nama.substringAfter(" - "), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Gedung: ${associatedG?.nama ?: "REK"} • Lantai ${r.lantai}", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("👥 Kapasitas: ${r.kapasitas} Sektor Kursi (Tipe: ${r.tipe})", fontSize = 10.sp, color = Color(0xFF475569))
                        }

                        // Action delete button
                        IconButton(onClick = { viewModel.deleteRuangan(r.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Dihapus", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(45.dp))
    }

    // Add Gedung Dialog
    if (showAddGedungDialog) {
        AlertDialog(
            onDismissRequest = { showAddGedungDialog = false },
            title = { Text("Tambah Gedung Baru") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newGedungKode,
                        onValueChange = { newGedungKode = it },
                        label = { Text("KODE GEDUNG (cth: REK)") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = newGedungNama,
                        onValueChange = { newGedungNama = it },
                        label = { Text("NAMA BANGUNAN") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = newGedungLokasi,
                        onValueChange = { newGedungLokasi = it },
                        label = { Text("LOKASI KAMPUS") },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newGedungKode.isNotEmpty() && newGedungNama.isNotEmpty()) {
                            viewModel.addGedung(newGedungKode, newGedungNama, newGedungLokasi)
                            newGedungKode = ""
                            newGedungNama = ""
                            newGedungLokasi = ""
                            showAddGedungDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGedungDialog = false }) { Text("Batal") }
            }
        )
    }

    // Add Ruangan Dialog
    if (showAddRuanganDialog) {
        AlertDialog(
            onDismissRequest = { showAddRuanganDialog = false },
            title = { Text("Tambah Ruangan Baru") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newRuanganKode,
                        onValueChange = { newRuanganKode = it },
                        label = { Text("KODE RUANG (cth: 1A105)") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = newRuanganNama,
                        onValueChange = { newRuanganNama = it },
                        label = { Text("NAMA RUANGAN") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = newRuanganLantai,
                        onValueChange = { newRuanganLantai = it },
                        label = { Text("LANTAI (ANGKA)") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = newRuanganKapasitas,
                        onValueChange = { newRuanganKapasitas = it },
                        label = { Text("KAPASITAS") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = newRuanganTipe,
                        onValueChange = { newRuanganTipe = it },
                        label = { Text("TIPE (Kantor/Kelas)") },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newRuanganKode.isNotEmpty() && newRuanganNama.isNotEmpty()) {
                            viewModel.addRuangan(
                                kode = newRuanganKode,
                                nama = newRuanganNama,
                                gedungId = newRuanganGedungId,
                                lantai = newRuanganLantai.toIntOrNull() ?: 1,
                                kapasitas = newRuanganKapasitas.toIntOrNull() ?: 10,
                                tipe = newRuanganTipe,
                                deskripsi = "Lantai ${newRuanganLantai} Kapasitas ${newRuanganKapasitas} orang, Tipe ${newRuanganTipe}"
                            )
                            newRuanganKode = ""
                            newRuanganNama = ""
                            showAddRuanganDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRuanganDialog = false }) { Text("Batal") }
            }
        )
    }
}
