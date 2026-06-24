package com.example.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

    Scaffold(containerColor = Color(0xFFF8FAFC)) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(scrollState).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Panel Validasi", fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text(if (userRole == Role.ADMIN_RT) "Tingkat 1 (RT)" else "Tingkat 2 (Kepala)", color = Color.Gray)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ValTab(text = "Antrean", count = needsValidationList.size, isSelected = selectedTab == 0, onClick = { selectedTab = 0 }, Modifier.weight(1f))
                ValTab(text = "Semua", count = peminjamanList.size, isSelected = selectedTab == 1, onClick = { selectedTab = 1 }, Modifier.weight(1f))
            }

            if (displayList.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("Kosong.") }
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
            title = { Text("Tambahkan Catatan") },
            text = { OutlinedTextField(value = noteText, onValueChange = { noteText = it }, placeholder = { Text("Alasan...") }, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                Button(onClick = {
                    showNoteDialog?.let { (id, status) -> viewModel.validateBooking(id, status, noteText) }
                    showNoteDialog = null; noteText = ""
                }) { Text("Kirim") }
            }
        )
    }
}

@Composable
fun ValTab(text: String, count: Int, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Surface(onClick = onClick, color = if (isSelected) Color(0xFFEEF2F6) else Color.Transparent, shape = RoundedCornerShape(12.dp), modifier = modifier) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.Center) {
            Text(text, fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFF4F46E5) else Color.Gray)
            if (count > 0) {
                Spacer(Modifier.width(6.dp))
                Surface(color = Color(0xFF4F46E5), shape = RoundedCornerShape(8.dp)) {
                    Text(count.toString(), Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ValidationCard(item: Peminjaman, userRole: Role, onApprove: () -> Unit, onReject: () -> Unit, onRevision: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ID #${item.id}", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 10.sp)
                StatusTag(item.status)
            }
            Text("${item.user?.name}", fontWeight = FontWeight.Bold)
            Text("${item.ruang?.nama} (${item.tanggal})", fontSize = 12.sp)
            Text("\"${item.keperluan}\"", fontSize = 11.sp, color = Color.Gray)
            
            val isMyTurn = (userRole == Role.ADMIN_RT && item.status == PeminjamanStatus.MENUNGGU_RT) ||
                           (userRole == Role.KEPALA_RT && item.status == PeminjamanStatus.MENUNGGU_KEPALA)
            
            if (isMyTurn) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onApprove, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))) { Text("Setuju", fontSize = 11.sp) }
                    Button(onClick = onRevision, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))) { Text("Revisi", fontSize = 11.sp) }
                    Button(onClick = onReject, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) { Text("Tolak", fontSize = 11.sp) }
                }
            }
        }
    }
}
