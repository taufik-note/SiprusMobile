package com.example.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.ui.component.StatusTag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: AppViewModel
) {
    val peminjamanList by viewModel.peminjamanList.collectAsState()
    val scrollState = rememberScrollState()

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("Semua Status") }

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
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.TableChart, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Excel", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("PDF", fontSize = 12.sp)
                    }
                }
            }
        }

        // Filters (Mobile Layout)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("KRITERIA PENYARINGAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari mahasiswa atau ruangan...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF94A3B8)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF4F46E5))
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tgl Mulai", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        OutlinedTextField(
                            value = "dd/mm/yy",
                            onValueChange = {},
                            readOnly = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tgl Akhir", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        OutlinedTextField(
                            value = "dd/mm/yy",
                            onValueChange = {},
                            readOnly = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = statusFilter,
                        onValueChange = { },
                        label = { Text("Status") },
                        modifier = Modifier.weight(1f),
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        shape = RoundedCornerShape(8.dp),
                        readOnly = true
                    )
                    OutlinedButton(
                        onClick = { searchQuery = ""; statusFilter = "Semua Status" },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(56.dp).padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.FilterAltOff, null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // List Header
        Text(
            text = "HASIL REKAPITULASI (${peminjamanList.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            letterSpacing = 1.sp
        )

        // Result Cards
        if (peminjamanList.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Text("Data tidak ditemukan.", color = Color.Gray)
            }
        } else {
            peminjamanList.asReversed().forEach { item ->
                MobileReportCard(item)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun MobileReportCard(item: Peminjaman) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(6.dp)) {
                    Text(
                        text = "ID #${item.id.replace("PEM", "")}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }
                StatusTag(status = item.status)
            }

            Spacer(Modifier.height(12.dp))
            
            Text(item.namaMahasiswa, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
            Text(item.emailMahasiswa, fontSize = 11.sp, color = Color(0xFF64748B))
            
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("RUANGAN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                    Text(item.ruanganNama.substringBefore(" - "), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(item.gedungNama, fontSize = 11.sp, color = Color(0xFF64748B))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("WAKTU", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                    Text(item.tanggal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("${item.jamMulai} WIB", fontSize = 11.sp, color = Color(0xFF64748B))
                }
            }

            Spacer(Modifier.height(12.dp))
            Surface(color = Color(0xFFF8FAFC), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("KEPERLUAN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                    Text("\"${item.tujuan}\"", fontSize = 12.sp, color = Color(0xFF475569))
                }
            }
        }
    }
}
