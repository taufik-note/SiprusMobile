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
import androidx.compose.ui.draw.shadow
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
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Column {
            Text("Pusat Bantuan", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
            Text("Panduan penggunaan sistem UniRoom UNIMUS.", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF475569))
        }

        // Tabs WITH SHADOW
        Surface(
            color = Color(0xFFF1F5F9), 
            shape = RoundedCornerShape(14.dp), 
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                GuideTabItem(text = "Alur Kerja", isSelected = activeInfoTab == 0, modifier = Modifier.weight(1f)) { activeInfoTab = 0 }
                GuideTabItem(text = "Aturan", isSelected = activeInfoTab == 1, modifier = Modifier.weight(1f)) { activeInfoTab = 1 }
                GuideTabItem(text = "Kontak", isSelected = activeInfoTab == 2, modifier = Modifier.weight(1f)) { activeInfoTab = 2 }
            }
        }

        // Content Card WITH SHADOW
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
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
        modifier = modifier.height(40.dp),
        shadowElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Black, color = if (isSelected) Color(0xFF0F172A) else Color(0xFF64748B))
        }
    }
}

@Composable
fun AlurContent() {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        GuideStep(num = "1", title = "Pilih & Ajukan", desc = "Temukan ruangan yang kosong melalui menu Cari, isi detail keperluan, lalu tekan tombol Ajukan.")
        GuideStep(num = "2", title = "Verifikasi RT", desc = "Admin Rumah Tangga akan memvalidasi permohonan Anda. Pantau status di menu Riwayat.")
        GuideStep(num = "3", title = "Otorisasi Final", desc = "Kepala RT memberikan persetujuan akhir. Setelah itu, ruangan siap digunakan sesuai jadwal.")
    }
}

@Composable
fun AturanContent() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        RuleItem("Dilarang membawa makanan/minuman berbau menyengat ke dalam ruangan.")
        RuleItem("Wajib menjaga kebersihan dan kerapian kursi setelah pemakaian.")
        RuleItem("Pastikan AC, lampu, dan proyektor telah dimatikan saat meninggalkan ruangan.")
        RuleItem("Gunakan ruangan sesuai dengan durasi waktu yang telah disetujui.")
    }
}

@Composable
fun RuleItem(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
    }
}

@Composable
fun KontakContent() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("BIRO RUMAH TANGGA UNIMUS", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF0F172A))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Phone, null, tint = Color(0xFF4F46E5), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text("Ext. 204 (Lantai 2 GKB Rektorat)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Email, null, tint = Color(0xFF4F46E5), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text("bauk@unimus.ac.id", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
fun GuideStep(num: String, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            color = Color(0xFF4F46E5), 
            shape = RoundedCornerShape(10.dp), 
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(num, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF0F172A))
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF475569))
        }
    }
}
