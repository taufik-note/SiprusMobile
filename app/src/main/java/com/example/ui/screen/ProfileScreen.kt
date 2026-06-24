package com.example.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppViewModel
import com.example.data.Role
import com.example.ui.component.StyledLabel
import com.example.ui.component.StyledTextField
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: AppViewModel,
    onLogoutClicked: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var isSettingsMode by remember { mutableStateOf(false) }

    // Profile Edit States
    var editName by remember { mutableStateOf(currentUser?.name ?: "") }
    var editEmail by remember { mutableStateOf(currentUser?.email ?: "") }

    // Password Edit States
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAFC))
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header
            currentUser?.let { user ->
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF3B82F6))),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = if (!user.name.isNullOrEmpty()) user.name.take(1).uppercase() else "?"
                        Text(
                            text = initial,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(user.name ?: "Guest", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
                    Text(user.email ?: "-", fontSize = 13.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(color = Color(0xFFEEF2F6), shape = RoundedCornerShape(20.dp)) {
                        Text(
                            user.role?.name ?: "GUEST",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F46E5)
                        )
                    }
                }

                if (user.role == Role.GUEST) {
                    GuestBanner()
                } else {
                    if (!isSettingsMode) {
                        AccountInfoCard(user)
                    } else {
                        // Personal Info Section
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PersonOutline, null, tint = Color(0xFF4F46E5), modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("INFORMASI PRIBADI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                }
                                
                                HorizontalDivider(color = Color(0xFFF1F5F9))

                                Column {
                                    StyledLabel("Nama Lengkap", icon = Icons.Default.Person)
                                    StyledTextField(value = editName, onValueChange = { editName = it }, placeholder = "Nama Lengkap")
                                }
                                
                                Column {
                                    StyledLabel("Email Pengguna", icon = Icons.Default.Email)
                                    StyledTextField(value = editEmail, onValueChange = { editEmail = it }, placeholder = "Email")
                                }

                                Button(
                                    onClick = {
                                        viewModel.updateProfile(editName, editEmail) { success, msg ->
                                            scope.launch { snackbarHostState.showSnackbar(msg) }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                                ) {
                                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Simpan Profil", fontSize = 13.sp)
                                }
                            }
                        }

                        // Password Security Section
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Shield, null, tint = Color(0xFF4F46E5), modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("KEAMANAN SANDI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                }

                                HorizontalDivider(color = Color(0xFFF1F5F9))

                                Column {
                                    StyledLabel("Password Saat Ini", icon = Icons.Default.Lock)
                                    StyledTextField(
                                        value = currentPassword,
                                        onValueChange = { currentPassword = it },
                                        placeholder = "Masukkan sandi saat ini",
                                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        trailingIcon = {
                                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                                Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    )
                                }

                                Column {
                                    StyledLabel("Password Baru")
                                    StyledTextField(
                                        value = newPassword,
                                        onValueChange = { newPassword = it },
                                        placeholder = "Minimal 6 karakter",
                                        visualTransformation = PasswordVisualTransformation()
                                    )
                                }

                                Column {
                                    StyledLabel("Konfirmasi Password")
                                    StyledTextField(
                                        value = confirmPassword,
                                        onValueChange = { confirmPassword = it },
                                        placeholder = "Ulangi sandi baru",
                                        visualTransformation = PasswordVisualTransformation()
                                    )
                                }

                                Button(
                                    onClick = {
                                        viewModel.updatePassword(currentPassword, newPassword, confirmPassword) { success, msg ->
                                            scope.launch { snackbarHostState.showSnackbar(msg) }
                                            if (success) {
                                                currentPassword = ""; newPassword = ""; confirmPassword = ""
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                                ) {
                                    Icon(Icons.Default.Key, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Perbarui Password", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (currentUser?.role != Role.GUEST) {
                    OutlinedButton(
                        onClick = { isSettingsMode = !isSettingsMode },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = if (isSettingsMode) ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFEEF2F6)) else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Icon(if (isSettingsMode) Icons.Default.ArrowBack else Icons.Default.Settings, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(if (isSettingsMode) "Kembali" else "Pengaturan Profil", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Button(
                    onClick = {
                        viewModel.logout()
                        onLogoutClicked()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentUser?.role == Role.GUEST) Color(0xFF4F46E5) else Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(if (currentUser?.role == Role.GUEST) Icons.Default.Login else Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (currentUser?.role == Role.GUEST) "Login Akun Mahasiswa" else "Keluar Aplikasi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun GuestBanner() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Anda masuk sebagai Guest",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF92400E),
                fontSize = 13.sp
            )
            Text(
                text = "Sesi Guest hanya diizinkan untuk melihat ketersediaan ruangan. Untuk melakukan peminjaman, silakan login menggunakan akun Mahasiswa.",
                fontSize = 11.sp,
                color = Color(0xFFB45309),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun AccountInfoCard(user: com.example.data.User) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("INFORMASI AKUN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
            
            ProfileInfoRow(icon = Icons.Default.Badge, label = "ID Pengenal", value = (user.id ?: 0).toString())
            ProfileInfoRow(icon = Icons.Default.Verified, label = "Status Sesi", value = if (user.token != null) "Aktif" else "Terbatas")
            ProfileInfoRow(icon = Icons.Default.Security, label = "Keamanan", value = "SSO Terintegrasi")
        }
    }
}

@Composable
fun ProfileInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, null, tint = Color(0xFF4F46E5), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 10.sp, color = Color(0xFF94A3B8))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        }
    }
}
