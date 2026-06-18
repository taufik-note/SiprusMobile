package com.example.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppViewModel
import com.example.data.Peminjaman
import com.example.data.PeminjamanStatus
import com.example.data.Role
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidationScreen(
    viewModel: AppViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val peminjamanList by viewModel.peminjamanList.collectAsState()
    val ruanganList by viewModel.ruanganList.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val userRole = currentUser?.role ?: Role.GUEST
    var selectedTab by remember { mutableStateOf(0) }
    var selectedRelocateId by remember { mutableStateOf<String?>(null) }
    var showAltRoomDropdown by remember { mutableStateOf(false) }

    val needsValidationList = peminjamanList.filter { p ->
        if (userRole == Role.ADMIN_RT) p.status == PeminjamanStatus.MENUNGGU_VERIFIKASI_RT
        else if (userRole == Role.KEPALA_RT) p.status == PeminjamanStatus.MENUNGGU_VERIFIKASI_SIPRUS
        else false
    }

    val displayList = if (selectedTab == 0) needsValidationList else peminjamanList

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column {
                Text(
                    text = "Panel Validasi",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = if (userRole == Role.ADMIN_RT) "Otorisasi Tingkat 1 (Rumah Tangga)" else "Otorisasi Tingkat 2 (Siprus)",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
            }

            // Tab Selector
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ValTabButton(
                    text = "Perlu Validasi",
                    count = needsValidationList.size,
                    isSelected = selectedTab == 0,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = 0 }
                )
                ValTabButton(
                    text = "Semua Riwayat",
                    count = peminjamanList.size,
                    isSelected = selectedTab == 1,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = 1 }
                )
            }

            // List
            if (displayList.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada antrean validasi.", color = Color.Gray)
                }
            } else {
                displayList.asReversed().forEach { item ->
                    ValidationItemCard(
                        item = item,
                        userRole = userRole,
                        onApprove = {
                            if (userRole == Role.ADMIN_RT) {
                                viewModel.adminApprove(item.id)
                                scope.launch { snackbarHostState.showSnackbar("Disetujui Admin RT. Diteruskan ke Kepala RT.") }
                            } else {
                                viewModel.kepalaApprove(item.id)
                                scope.launch { snackbarHostState.showSnackbar("Persetujuan Final Berhasil!") }
                            }
                        },
                        onReject = {
                            if (userRole == Role.ADMIN_RT) viewModel.adminReject(item.id) else viewModel.kepalaReject(item.id)
                            scope.launch { snackbarHostState.showSnackbar("Pengajuan Ditolak.") }
                        },
                        onRelocate = { selectedRelocateId = item.id; showAltRoomDropdown = true },
                        onRevision = {
                             scope.launch { snackbarHostState.showSnackbar("Fitur Minta Revisi akan segera hadir.") }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Relocation Dialog
    if (showAltRoomDropdown && selectedRelocateId != null) {
        AlertDialog(
            onDismissRequest = { showAltRoomDropdown = false },
            title = { Text("Alihkan ke Ruang Alternatif", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ruanganList.take(5).forEach { r ->
                        Card(
                            onClick = {
                                selectedRelocateId?.let { id -> 
                                    viewModel.kepalaRelocate(id, r.kode)
                                    scope.launch { snackbarHostState.showSnackbar("Ruangan berhasil dialihkan ke ${r.nama}") }
                                }
                                showAltRoomDropdown = false
                            },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MeetingRoom, null, tint = Color(0xFF4F46E5), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(r.nama, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showAltRoomDropdown = false }) { Text("Batal") } }
        )
    }
}

@Composable
fun ValTabButton(text: String, count: Int, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) Color(0xFFEEF2F6) else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFF4F46E5) else Color(0xFF64748B))
            if (count > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(color = if (isSelected) Color(0xFF4F46E5) else Color(0xFFCBD5E1), shape = RoundedCornerShape(8.dp)) {
                    Text(count.toString(), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ValidationItemCard(
    item: Peminjaman, 
    userRole: Role, 
    onApprove: () -> Unit, 
    onReject: () -> Unit, 
    onRelocate: () -> Unit,
    onRevision: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Vertical line indicator
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(4.dp)
                    .height(200.dp) // Large enough to cover height
                    .background(Color(0xFF4F46E5), RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Content
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Header ID & Badge
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(6.dp)) {
                            Text(
                                text = "ID #${item.id.replace("PEM", "")}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                        }
                        
                        val statusLabel = when(item.status) {
                            PeminjamanStatus.MENUNGGU_VERIFIKASI_RT -> "MENUNGGU TINGKAT 1"
                            PeminjamanStatus.MENUNGGU_VERIFIKASI_SIPRUS -> "MENUNGGU TINGKAT 2"
                            PeminjamanStatus.DISETUJUI -> "DISETUJUI"
                            PeminjamanStatus.DITOLAK -> "DITOLAK"
                        }
                        Surface(color = Color(0xFFFFF7ED), shape = RoundedCornerShape(20.dp)) {
                            Text(
                                text = statusLabel,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFC2410C)
                            )
                        }
                    }

                    // Peminjam Section
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                color = Color.White,
                                shape = CircleShape,
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(24.dp))
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("PEMINJAM", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                                Text(item.namaMahasiswa, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                Text(item.emailMahasiswa, fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                        }
                    }

                    // DateTime Row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DateTimeInfoBox(icon = Icons.Default.CalendarToday, label = "TANGGAL", value = item.tanggal, modifier = Modifier.weight(1f))
                        DateTimeInfoBox(icon = Icons.Default.AccessTime, label = "JAM", value = "${item.jamMulai} - ${item.jamSelesai}", modifier = Modifier.weight(1f))
                    }

                    // Tujuan Section
                    Column {
                        Text("TUJUAN ACARA:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFFEF3C7)),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Text(
                                "\"${item.tujuan}\"",
                                modifier = Modifier.padding(12.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }

                // Right Sidebar Buttons
                Column(
                    modifier = Modifier.width(140.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Aksi Validasi:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    
                    val isPendingForMe = (userRole == Role.ADMIN_RT && item.status == PeminjamanStatus.MENUNGGU_VERIFIKASI_RT) ||
                                         (userRole == Role.KEPALA_RT && item.status == PeminjamanStatus.MENUNGGU_VERIFIKASI_SIPRUS)
                    
                    if (isPendingForMe) {
                        ActionButton(text = "Setuju", icon = Icons.Default.CheckCircle, color = Color(0xFF059669), onClick = onApprove)
                        ActionButton(text = "Minta Revisi", icon = Icons.Outlined.EditNote, color = Color(0xFF3B82F6), bgColor = Color(0xFFEFF6FF), onClick = onRevision)
                        ActionButton(text = "Tolak", icon = Icons.Default.Cancel, color = Color(0xFFEF4444), bgColor = Color(0xFFFEF2F2), onClick = onReject)
                        ActionButton(text = "Alihkan", icon = Icons.Default.SwapHoriz, color = Color(0xFF6366F1), bgColor = Color(0xFFF5F3FF), onClick = onRelocate)
                    } else {
                         Text(
                            text = if (item.status == PeminjamanStatus.DISETUJUI) "SELESAI" else "DIPROSES",
                            modifier = Modifier.padding(top = 20.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = if (item.status == PeminjamanStatus.DISETUJUI) Color(0xFF10B981) else Color(0xFF94A3B8)
                         )
                    }
                }
            }
        }
    }
}

@Composable
fun DateTimeInfoBox(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, modifier: Modifier) {
    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Color(0xFF4F46E5), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            }
        }
    }
}

@Composable
fun ActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, bgColor: Color = color, onClick: () -> Unit) {
    val isPrimary = color == bgColor
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(42.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) color else bgColor,
            contentColor = if (isPrimary) Color.White else color
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
