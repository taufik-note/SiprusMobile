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

    var activeTab by remember { mutableStateOf(0) }
    var showAddGedungDialog by remember { mutableStateOf(false) }
    var showAddRuanganDialog by remember { mutableStateOf(false) }

    var newGedungKode by remember { mutableStateOf("") }
    var newGedungNama by remember { mutableStateOf("") }
    var newGedungLokasi by remember { mutableStateOf("") }

    var newRuanganKode by remember { mutableStateOf("") }
    var newRuanganNama by remember { mutableStateOf("") }
    var newRuanganLantai by remember { mutableStateOf("1") }
    var newRuanganKapasitas by remember { mutableStateOf("30") }
    var newRuanganTipe by remember { mutableStateOf("Kelas") }

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
            Text("Kelola Data", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
            Text("Administrasi prasarana UNIMUS.", fontSize = 12.sp, color = Color(0xFF64748B))
        }

        // Tab Switcher
        Surface(color = Color(0xFFEEF2F6), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(4.dp)) {
                MasterTabItem(text = "Gedung", isSelected = activeTab == 0, modifier = Modifier.weight(1f)) { activeTab = 0 }
                MasterTabItem(text = "Ruangan", isSelected = activeTab == 1, modifier = Modifier.weight(1f)) { activeTab = 1 }
            }
        }

        // Action Button
        Button(
            onClick = { if (activeTab == 0) showAddGedungDialog = true else showAddRuanganDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Tambah ${if (activeTab == 0) "Gedung" else "Ruangan"}", fontWeight = FontWeight.Bold)
        }

        // List
        if (activeTab == 0) {
            gedungList.forEach { g ->
                MasterItemCard(title = g.nama, subtitle = g.lokasi, code = g.kode) { viewModel.deleteGedung(g.id) }
            }
        } else {
            ruanganList.forEach { r ->
                MasterItemCard(title = r.nama.substringAfter(" - "), subtitle = "Lt ${r.lantai} • Kapasitas ${r.kapasitas}", code = r.kode) { viewModel.deleteRuangan(r.id) }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // Dialogs would go here (same as before but styled if needed)
}

@Composable
fun MasterTabItem(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) Color.White else Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier,
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFF4F46E5) else Color(0xFF64748B))
        }
    }
}

@Composable
fun MasterItemCard(title: String, subtitle: String, code: String, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(color = Color(0xFFEEF2F6), shape = RoundedCornerShape(6.dp)) {
                    Text(code, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5))
                }
                Spacer(Modifier.height(8.dp))
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Text(subtitle, fontSize = 12.sp, color = Color(0xFF64748B))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444))
            }
        }
    }
}
