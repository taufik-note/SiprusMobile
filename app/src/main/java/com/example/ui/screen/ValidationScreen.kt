package com.example.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppViewModel
import com.example.data.Peminjaman
import com.example.data.PeminjamanStatus
import com.example.data.Role

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidationScreen(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val peminjamanList by viewModel.peminjamanList.collectAsState()
    val scrollState = rememberScrollState()
    val userRole = currentUser?.role ?: Role.GUEST

    var selectedTab by remember { mutableStateOf(0) }
    var showNoteDialog by remember { mutableStateOf<Pair<Int, PeminjamanStatus>?>(null) }
    var noteText by remember { mutableStateOf("") }

    val needsValidationList = peminjamanList.filter { p ->
        if (userRole == Role.ADMIN_RT) p.status == PeminjamanStatus.MENUNGGU_RT
        else if (userRole == Role.KEPALA_RT) p.status == PeminjamanStatus.MENUNGGU_KEPALA
        else false
    }

    val displayList = if (selectedTab == 0) needsValidationList else peminjamanList

    Scaffold(containerColor = Color.Transparent) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(scrollState).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column {
                Text("Panel Validasi", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                Text(
                    text = if (userRole == Role.ADMIN_RT) "Tingkat 1 (Rumah Tangga)" else "Tingkat 2 (Kepala Siprus)", 
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
            }

            Surface(
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ValTab(text = "Antrean", count = needsValidationList.size, isSelected = selectedTab == 0, onClick = { selectedTab = 0 }, Modifier.weight(1f))
                    ValTab(text = "Semua Riwayat", count = 0, isSelected = selectedTab == 1, onClick = { selectedTab = 1 }, Modifier.weight(1f))
                }
            }

            if (displayList.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Tidak ada data antrean validasi.", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                displayList.asReversed().forEach { item ->
                    ValidationCard(
                        item = item,
                        userRole = userRole,
                        onApprove = {
                            val nextStatus = if (userRole == Role.ADMIN_RT) PeminjamanStatus.MENUNGGU_KEPALA else PeminjamanStatus.DISETUJUI
                            viewModel.validateBooking(item.id, nextStatus)
                        },
                        onReject = { showNoteDialog = item.id to (if (userRole == Role.ADMIN_RT) PeminjamanStatus.DITOLAK_RT else PeminjamanStatus.DITOLAK_KEPALA) },
                        onRevision = { showNoteDialog = item.id to PeminjamanStatus.BUTUH_REVISI }
                    )
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    if (showNoteDialog != null) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = null; noteText = "" },
            title = { Text("Tambahkan Catatan", fontWeight = FontWeight.Black) },
            text = { 
                OutlinedTextField(
                    value = noteText, 
                    onValueChange = { noteText = it }, 
                    placeholder = { Text("Tulis alasan atau instruksi...") }, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) 
            },
            confirmButton = {
                Button(onClick = {
                    showNoteDialog?.let { (id, status) -> viewModel.validateBooking(id, status, noteText) }
                    showNoteDialog = null; noteText = ""
                }) { Text("Kirim Validasi", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = null; noteText = "" }) { Text("Batal") }
            }
        )
    }
}

@Composable
fun ValTab(text: String, count: Int, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        onClick = onClick, 
        color = if (isSelected) Color.White else Color.Transparent, 
        shape = RoundedCornerShape(10.dp), 
        shadowElevation = 0.dp,
        modifier = modifier.height(44.dp)
    ) {
        Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Black, color = if (isSelected) Color(0xFF0F172A) else Color(0xFF64748B))
            if (count > 0) {
                Spacer(Modifier.width(8.dp))
                Surface(color = Color(0xFFEF4444), shape = RoundedCornerShape(8.dp)) {
                    Text(count.toString(), Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ValidationCard(item: Peminjaman, userRole: Role, onApprove: () -> Unit, onReject: () -> Unit, onRevision: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White), 
        shape = RoundedCornerShape(16.dp), 
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("REF #${item.id}", fontWeight = FontWeight.Black, color = Color(0xFF4F46E5), fontSize = 11.sp)
                StatusTag(item.status)
            }
            Text(item.user?.name ?: "Mahasiswa", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.Black)
            
            Column {
                Text(text = item.ruang?.nama ?: "Ruangan", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(text = "${item.tanggal} • ${item.waktuMulai} - ${item.waktuSelesai}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF475569))
            }
            
            Surface(color = Color(0xFFF8FAFC), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                Text("\"${item.keperluan}\"", modifier = Modifier.padding(12.dp), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            }
            
            val isMyTurn = (userRole == Role.ADMIN_RT && item.status == PeminjamanStatus.MENUNGGU_RT) ||
                           (userRole == Role.KEPALA_RT && item.status == PeminjamanStatus.MENUNGGU_KEPALA)
            
            if (isMyTurn) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFF1F5F9))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onApprove, modifier = Modifier.weight(1.2f).height(40.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))) { 
                        Text("Setuju", fontSize = 11.sp, fontWeight = FontWeight.Black) 
                    }
                    Button(onClick = onRevision, modifier = Modifier.weight(1f).height(40.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))) { 
                        Text("Revisi", fontSize = 11.sp, fontWeight = FontWeight.Black) 
                    }
                    Button(onClick = onReject, modifier = Modifier.weight(1f).height(40.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) { 
                        Text("Tolak", fontSize = 11.sp, fontWeight = FontWeight.Black) 
                    }
                }
            }
        }
    }
}
