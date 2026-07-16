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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppViewModel
import com.example.data.Peminjaman
import com.example.data.PeminjamanStatus
import com.example.data.Role
import com.example.data.Ruangan
import com.example.ui.component.StatusTag
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidationScreen(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val peminjamanList by viewModel.peminjamanList.collectAsState()
    val ruanganList by viewModel.ruanganList.collectAsState()
    val scrollState = rememberScrollState()
    val userRole = currentUser?.role ?: Role.GUEST
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableStateOf(0) }
    var showNoteDialog by remember { mutableStateOf<Pair<Int, PeminjamanStatus>?>(null) }
    var noteText by remember { mutableStateOf("") }
    
    // State for switching room
    var switchingBookingId by remember { mutableStateOf<Int?>(null) }

    val needsValidationList = peminjamanList.filter { p ->
        if (userRole == Role.ADMIN_RT) p.status == PeminjamanStatus.MENUNGGU_RT
        else if (userRole == Role.KEPALA_RT) p.status == PeminjamanStatus.MENUNGGU_KEPALA
        else false
    }

    val displayList = if (selectedTab == 0) needsValidationList else peminjamanList

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
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
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(14.dp))
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
                displayList.forEach { item ->
                    ValidationCard(
                        item = item,
                        userRole = userRole,
                        isSwitching = switchingBookingId == item.id,
                        ruanganOptions = ruanganList,
                        onApprove = {
                            val nextStatus = if (userRole == Role.ADMIN_RT) PeminjamanStatus.MENUNGGU_KEPALA else PeminjamanStatus.DISETUJUI
                            viewModel.validateBooking(item.id, nextStatus)
                        },
                        onReject = { showNoteDialog = item.id to (if (userRole == Role.ADMIN_RT) PeminjamanStatus.DITOLAK_RT else PeminjamanStatus.DITOLAK_KEPALA) },
                        onRevision = { showNoteDialog = item.id to PeminjamanStatus.BUTUH_REVISI },
                        onSwitchClick = { switchingBookingId = if (switchingBookingId == item.id) null else item.id },
                        onExecuteSwitch = { newRuangId, alasan ->
                            viewModel.switchRoom(item.id, newRuangId, alasan) { success, msg ->
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                                if (success) switchingBookingId = null
                            }
                        }
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
        shadowElevation = if (isSelected) 1.dp else 0.dp,
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
fun ValidationCard(
    item: Peminjaman, 
    userRole: Role, 
    isSwitching: Boolean,
    ruanganOptions: List<Ruangan>,
    onApprove: () -> Unit, 
    onReject: () -> Unit, 
    onRevision: () -> Unit,
    onSwitchClick: () -> Unit,
    onExecuteSwitch: (Int, String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White), 
        shape = RoundedCornerShape(16.dp), 
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("REF #${item.id}", fontWeight = FontWeight.Black, color = Color(0xFF4F46E5), fontSize = 11.sp)
                StatusTag(item.status)
            }
            
            // Peminjam Info
            Surface(color = Color(0xFFF8FAFC), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Color.White, shape = RoundedCornerShape(100.dp), modifier = Modifier.size(40.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = Color(0xFF64748B))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(text = item.user?.name ?: "Mahasiswa", fontSize = 14.sp, fontWeight = FontWeight.Black)
                        Text(text = item.user?.email ?: "", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                }
            }
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = Color(0xFFF8FAFC), shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f)) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, tint = Color(0xFF4F46E5), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(text = item.tanggal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Surface(color = Color(0xFFF8FAFC), shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f)) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, null, tint = Color(0xFF4F46E5), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(text = "${item.waktuMulai} - ${item.waktuSelesai}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Column {
                Text("TUJUAN ACARA:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B), letterSpacing = 0.5.sp)
                Text("\"${item.keperluan}\"", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            }
            
            val isMyTurn = (userRole == Role.ADMIN_RT && item.status == PeminjamanStatus.MENUNGGU_RT) ||
                           (userRole == Role.KEPALA_RT && item.status == PeminjamanStatus.MENUNGGU_KEPALA)
            
            if (isMyTurn) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFF1F5F9))
                
                if (!isSwitching) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // SETUJU - Hijau Full
                            Button(
                                onClick = onApprove,
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                            ) {
                                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Setuju", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // REVISI - Biru Muda Outlined
                            Button(
                                onClick = onRevision,
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEFF6FF),
                                    contentColor = Color(0xFF2563EB)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2563EB).copy(alpha = 0.3f))
                            ) {
                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Revisi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // ALIHKAN - Ungu Muda Outlined
                            Button(
                                onClick = onSwitchClick,
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF5F3FF),
                                    contentColor = Color(0xFF7C3AED)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.3f))
                            ) {
                                Icon(Icons.Default.SwapHoriz, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Alihkan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // TOLAK - Merah Muda Outlined
                            Button(
                                onClick = onReject,
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFEF2F2),
                                    contentColor = Color(0xFFDC2626)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.3f))
                            ) {
                                Icon(Icons.Default.Cancel, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Tolak", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    SwitchRoomForm(
                        currentRoomName = item.ruang?.nama ?: "",
                        ruanganOptions = ruanganOptions,
                        onCancel = onSwitchClick,
                        onExecute = onExecuteSwitch
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwitchRoomForm(
    currentRoomName: String,
    ruanganOptions: List<Ruangan>,
    onCancel: () -> Unit,
    onExecute: (Int, String) -> Unit
) {
    var selectedRuanganId by remember { mutableStateOf<Int?>(null) }
    var alasan by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    val selectedRuanganName = ruanganOptions.find { it.id == selectedRuanganId }?.nama ?: "-- Cari Ruang Alternatif --"

    Surface(
        color = Color(0xFFF8FAFC), 
        shape = RoundedCornerShape(12.dp), 
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Form Peralihan Ruang Otomatis", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            
            Box {
                OutlinedTextField(
                    value = selectedRuanganName,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                Box(Modifier.matchParentSize().clickable { expanded = true })
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.fillMaxWidth(0.8f)) {
                    ruanganOptions.filter { it.nama != currentRoomName }.forEach { room ->
                        DropdownMenuItem(
                            text = { Text("${room.nama} (Lt ${room.lantai})") },
                            onClick = { 
                                selectedRuanganId = room.id
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            OutlinedTextField(
                value = alasan,
                onValueChange = { alasan = it },
                placeholder = { Text("Alasan Peralihan...", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onCancel) {
                    Text("Batal", color = Color(0xFF64748B))
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { selectedRuanganId?.let { onExecute(it, alasan) } },
                    enabled = selectedRuanganId != null && alasan.isNotBlank(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    Text("Eksekusi", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
