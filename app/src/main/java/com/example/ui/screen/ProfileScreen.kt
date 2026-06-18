package com.example.ui.screen

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppViewModel
import com.example.data.Role

@Composable
fun ProfileScreen(
    viewModel: AppViewModel,
    onLogoutClicked: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Profile Header
        currentUser?.let { user ->
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF3B82F6))),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.take(1).uppercase(),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(user.name, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
                Text(user.email, fontSize = 14.sp, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(12.dp))
                Surface(color = Color(0xFFEEF2F6), shape = RoundedCornerShape(20.dp)) {
                    Text(
                        user.role.name,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4F46E5)
                    )
                }
            }

            if (user.role == Role.GUEST) {
                // Special Banner for Guest
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFD97706))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Anda masuk sebagai Guest",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                        Text(
                            text = "Sesi Guest hanya diizinkan untuk melihat ketersediaan ruangan. Untuk melakukan peminjaman, silakan keluar dan login menggunakan akun Mahasiswa.",
                            fontSize = 12.sp,
                            color = Color(0xFFB45309),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                // Info Section for regular users
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("INFORMASI AKUN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
                        
                        ProfileInfoRow(icon = Icons.Default.Badge, label = "ID Pengenal", value = user.idPengenal)
                        ProfileInfoRow(icon = Icons.Default.Verified, label = "Status Sesi", value = user.kredensialSesi)
                        ProfileInfoRow(icon = Icons.Default.Security, label = "Keamanan", value = "SSO Terintegrasi")
                    }
                }
            }
        }

        // Action Buttons
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (currentUser?.role != Role.GUEST) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Settings, null)
                    Spacer(Modifier.width(12.dp))
                    Text("Pengaturan Lanjutan", fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = {
                    viewModel.logout()
                    onLogoutClicked()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentUser?.role == Role.GUEST) Color(0xFF4F46E5) else Color(0xFFEF4444)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(if (currentUser?.role == Role.GUEST) Icons.Default.Login else Icons.Default.Logout, null)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (currentUser?.role == Role.GUEST) "Login Akun Mahasiswa" else "Keluar Aplikasi",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun ProfileInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, null, tint = Color(0xFF4F46E5), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color(0xFF94A3B8))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        }
    }
}
