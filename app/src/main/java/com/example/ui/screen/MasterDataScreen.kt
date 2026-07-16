package com.example.ui.screen

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
import androidx.compose.ui.window.Dialog
import com.example.ui.viewmodel.AppViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterDataScreen(
    viewModel: AppViewModel
) {
    val gedungList by viewModel.gedungList.collectAsState()
    val ruanganList by viewModel.ruanganList.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(0) }
    var showAddGedungDialog by remember { mutableStateOf(false) }
    var showAddRuanganDialog by remember { mutableStateOf(false) }

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
                Text("Kelola Data", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
                Text("Administrasi prasarana UNIMUS.", fontSize = 12.sp, color = Color(0xFF64748B))
            }

            // Tab Switcher
            Surface(color = Color(0xFFEEF2F6), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp))) {
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
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4F46E5),
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.Add, null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Tambah ${if (activeTab == 0) "Gedung" else "Ruangan"}", fontWeight = FontWeight.Bold, color = Color.White)
        }

            // List Header
            Text(
                text = "DAFTAR ${if (activeTab == 0) "GEDUNG" else "RUANGAN"} (${if (activeTab == 0) gedungList.size else ruanganList.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                letterSpacing = 1.sp
            )

            // List content
            if (activeTab == 0) {
                gedungList.forEach { g ->
                    MasterItemCard(title = g.nama, subtitle = g.lokasi ?: "-", code = g.kode) { }
                }
            } else {
                ruanganList.forEach { r ->
                    val gName = gedungList.find { it.id == r.gedungId }?.nama ?: "Gedung"
                    MasterItemCard(title = r.nama, subtitle = "$gName • Lt ${r.lantai} • Kap: ${r.kapasitas}", code = r.kode) { }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        if (showAddGedungDialog) {
            AddGedungDialog(
                onDismiss = { showAddGedungDialog = false },
                onConfirm = { kode, nama, lokasi ->
                    viewModel.addGedung(kode, nama, lokasi) { success, msg ->
                        if (success) {
                            showAddGedungDialog = false
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    }
                }
            )
        }

        if (showAddRuanganDialog) {
            AddRuanganDialog(
                gedungOptions = gedungList.map { it.id to it.nama },
                onDismiss = { showAddRuanganDialog = false },
                onConfirm = { kode, nama, gId, lantai, kap, jenis, fas ->
                    viewModel.addRuangan(kode, nama, gId, lantai, kap, jenis, fas) { success, msg ->
                        if (success) {
                            showAddRuanganDialog = false
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGedungDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var kode by remember { mutableStateOf("") }
    var nama by remember { mutableStateOf("") }
    var lokasi by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFEEF2F6),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Business,
                                null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("Tambah Gedung Baru", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, null, tint = Color.LightGray)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Form Fields
                StyledLabel("KODE GEDUNG")
                StyledTextField(
                    value = kode,
                    onValueChange = { kode = it },
                    placeholder = "Contoh: GKB1, LABMED, REK"
                )

                Spacer(Modifier.height(16.dp))

                StyledLabel("NAMA LENGKAP GEDUNG")
                StyledTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    placeholder = "Contoh: Gedung Kuliah Bersama I (GKB I)"
                )

                Spacer(Modifier.height(16.dp))

                StyledLabel("LOKASI KAMPUS / ALAMAT")
                StyledTextField(
                    value = lokasi,
                    onValueChange = { lokasi = it },
                    placeholder = "Contoh: Kampus Barat, Jl. Kedungmundu Raya No.125",
                    singleLine = false,
                    minLines = 3
                )

                Spacer(Modifier.height(24.dp))

                // Actions
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Text("Batal", color = Color(0xFF64748B))
                    }
                    Button(
                        onClick = { onConfirm(kode, nama, lokasi) },
                        modifier = Modifier.weight(1.5f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                    ) {
                        Text("Daftarkan Gedung", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRuanganDialog(
    gedungOptions: List<Pair<Int, String>>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Int, Int, String, String) -> Unit
) {
    var kode by remember { mutableStateOf("") }
    var nama by remember { mutableStateOf("") }
    var selectedGedungId by remember { mutableStateOf(gedungOptions.firstOrNull()?.first ?: 0) }
    var lantai by remember { mutableStateOf("1") }
    var kapasitas by remember { mutableStateOf("30") }
    var tipe by remember { mutableStateOf("Kelas") }
    var fasilitas by remember { mutableStateOf("") }
    
    var expandedGedung by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Color(0xFFEEF2F6), shape = RoundedCornerShape(8.dp), modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.MeetingRoom, null, tint = Color(0xFF10B981), modifier = Modifier.padding(8.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("Tambah Ruangan Baru", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Close, null, tint = Color.LightGray) }
                }

                Spacer(Modifier.height(24.dp))

                StyledLabel("GEDUNG")
                Box {
                    OutlinedTextField(
                        value = gedungOptions.find { it.first == selectedGedungId }?.second ?: "Pilih Gedung",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )
                    Box(Modifier.matchParentSize().clickable { expandedGedung = true })
                    DropdownMenu(expanded = expandedGedung, onDismissRequest = { expandedGedung = false }) {
                        gedungOptions.forEach { (id, name) ->
                            DropdownMenuItem(text = { Text(name) }, onClick = { selectedGedungId = id; expandedGedung = false })
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        StyledLabel("KODE RUANG")
                        StyledTextField(value = kode, onValueChange = { kode = it }, placeholder = "R.101")
                    }
                    Column(Modifier.weight(1f)) {
                        StyledLabel("LANTAI")
                        StyledTextField(value = lantai, onValueChange = { lantai = it }, placeholder = "1")
                    }
                }

                Spacer(Modifier.height(16.dp))

                StyledLabel("NAMA RUANGAN")
                StyledTextField(value = nama, onValueChange = { nama = it }, placeholder = "Ruang Kelas Teori")

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        StyledLabel("KAPASITAS")
                        StyledTextField(value = kapasitas, onValueChange = { kapasitas = it }, placeholder = "40")
                    }
                    Column(Modifier.weight(1f)) {
                        StyledLabel("TIPE")
                        StyledTextField(value = tipe, onValueChange = { tipe = it }, placeholder = "Kelas")
                    }
                }

                Spacer(Modifier.height(16.dp))

                StyledLabel("FASILITAS")
                StyledTextField(value = fasilitas, onValueChange = { fasilitas = it }, placeholder = "AC, Proyektor, Papan Tulis", singleLine = false, minLines = 2)

                Spacer(Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp)) { Text("Batal", color = Color(0xFF64748B)) }
                    Button(onClick = { onConfirm(kode, nama, selectedGedungId, lantai.toIntOrNull() ?: 1, kapasitas.toIntOrNull() ?: 30, tipe, fasilitas) }, modifier = Modifier.weight(1.5f).height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))) {
                        Text("Daftarkan Ruangan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StyledLabel(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF64748B),
        modifier = Modifier.padding(bottom = 8.dp),
        letterSpacing = 0.5.sp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, fontSize = 13.sp, color = Color(0xFF94A3B8)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = singleLine,
        minLines = minLines,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF8FAFC),
            unfocusedContainerColor = Color(0xFFF8FAFC),
            unfocusedBorderColor = Color(0xFFE2E8F0),
            focusedBorderColor = Color(0xFF4F46E5)
        )
    )
}

@Composable
fun MasterTabItem(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) Color.White else Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier,
        shadowElevation = if (isSelected) 1.dp else 0.dp
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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
