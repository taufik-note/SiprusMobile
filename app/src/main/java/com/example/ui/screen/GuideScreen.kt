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

@Composable
fun GuideScreen() {
    val scrollState = rememberScrollState()
    var activeInfoTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Column {
            Text("Pusat Bantuan", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
            Text("Panduan penggunaan sistem UniRoom.", fontSize = 12.sp, color = Color(0xFF64748B))
        }

        // Tabs
        Surface(color = Color(0xFFEEF2F6), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(4.dp)) {
                GuideTabItem(text = "Alur", isSelected = activeInfoTab == 0, modifier = Modifier.weight(1f)) { activeInfoTab = 0 }
                GuideTabItem(text = "Aturan", isSelected = activeInfoTab == 1, modifier = Modifier.weight(1f)) { activeInfoTab = 1 }
                GuideTabItem(text = "Kontak", isSelected = activeInfoTab == 2, modifier = Modifier.weight(1f)) { activeInfoTab = 2 }
            }
        }

        // Content
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                when (activeInfoTab) {
                    0 -> AlurContent()
                    1 -> AturanContent()
                    2 -> KontakContent()
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun GuideTabItem(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) Color.White else Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier,
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFF4F46E5) else Color(0xFF64748B))
        }
    }
}

@Composable
fun AlurContent() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GuideStep(num = "1", title = "Pilih & Ajukan", desc = "Cari ruangan yang tersedia dan isi detail kegiatan Anda.")
        GuideStep(num = "2", title = "Verifikasi RT", desc = "Admin Rumah Tangga akan meninjau pengajuan Anda.")
        GuideStep(num = "3", title = "Persetujuan Final", desc = "Kepala RT memberikan otorisasi akhir.")
    }
}

@Composable
fun AturanContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("• Jagalah kebersihan ruangan.", fontSize = 13.sp, color = Color(0xFF475569))
        Text("• Gunakan sesuai jadwal yang disetujui.", fontSize = 13.sp, color = Color(0xFF475569))
        Text("• Matikan AC dan lampu setelah selesai.", fontSize = 13.sp, color = Color(0xFF475569))
    }
}

@Composable
fun KontakContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Biro Rumah Tangga UNIMUS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text("📞 Ext. 204 (GKB Rektorat)", fontSize = 13.sp, color = Color(0xFF475569))
        Text("✉ bauk@unimus.ac.id", fontSize = 13.sp, color = Color(0xFF475569))
    }
}

@Composable
fun GuideStep(num: String, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(color = Color(0xFF4F46E5), shape = RoundedCornerShape(8.dp), modifier = Modifier.size(24.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text(num, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Text(desc, fontSize = 12.sp, color = Color(0xFF64748B))
        }
    }
}
