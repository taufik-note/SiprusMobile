package com.example.ui.screens

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppViewModel
import com.example.data.Role

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AppViewModel,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("taufik@unimus.ac.id") }
    var password by remember { mutableStateOf("123") }
    val scrollState = rememberScrollState()

    // Royal Blue Gradients
    val blueGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A), // Deep Slate
            Color(0xFF1E3A8A), // Royal Dark Blue
            Color(0xFF3B82F6)  // Bright Accent Blue
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(blueGradient)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Logo Branding matching header in reference
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.MeetingRoom,
                    contentDescription = "Uniroom Logo",
                    tint = Color(0xFFFBBF24), // Gold
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "UNIROOM UNIMUS",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Main Slogan Block from ref ("Sistem Digitalisasi Peminjaman Ruangan Kampus")
            Text(
                text = "Sistem Digitalisasi\nPeminjaman Ruangan Kampus",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Memudahkan civitas akademika Universitas Muhammadiyah Semarang melakukan pelacakan ketersediaan, reservasi, dan validasi ruangan kelas terintegrasi secara dinamis.",
                fontSize = 12.sp,
                color = Color(0xFFD1D5DB),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Form Box (Light Container)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Selamat Datang",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "Silakan login dengan akun UNIMUS Anda",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("EMAIL UNIMUS") },
                        placeholder = { Text("Masukkan email UNIMUS") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF3B82F6)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("PASSWORD") },
                        placeholder = { Text("Masukkan sandi") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF3B82F6)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Login Button matching blue action in web format
                    Button(
                        onClick = {
                            if (email.isNotEmpty()) {
                                // Match password demo role
                                when {
                                    email.contains("admin") || email.contains("iqbal") -> viewModel.login(email, Role.ADMIN_RT)
                                    email.contains("avril") || email.contains("siprus") -> viewModel.login(email, Role.KEPALA_RT)
                                    else -> viewModel.login(email, Role.MAHASISWA)
                                }
                                onLoginSuccess()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Masuk ke Aplikasi", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Guest Session Pathway
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.loginWithDemo(Role.GUEST)
                                onLoginSuccess()
                            }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Masuk Sesi Guest (Lihat Ketersediaan)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4F46E5)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Demo Accounts Section corresponding to the 3 roles screenshot helpers
            Text(
                text = "Klik Akun Demo untuk Uji Coba Cepat:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )

            // Mahasiswa Quick Accent Card
            DemoRoleCard(
                title = "Mahasiswa (Taufik / Zulham)",
                email = "taufik@unimus.ac.id",
                iconColor = Color(0xFF3B82F6),
                bgColor = Color(0xFFEFF6FF).copy(alpha = 0.9f)
            ) {
                viewModel.loginWithDemo(Role.MAHASISWA)
                onLoginSuccess()
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Admin RT Quick Accent Card
            DemoRoleCard(
                title = "Admin Rumah Tangga (Iqbal)",
                email = "iqbal@unimus.ac.id",
                iconColor = Color(0xFFF59E0B),
                bgColor = Color(0xFFFEF3C7).copy(alpha = 0.9f)
            ) {
                viewModel.loginWithDemo(Role.ADMIN_RT)
                onLoginSuccess()
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Siprus Quick Accent Card
            DemoRoleCard(
                title = "Siprus / Kepala RT (Avril)",
                email = "avril@unimus.ac.id",
                iconColor = Color(0xFF8B5CF6),
                bgColor = Color(0xFFF5F3FF).copy(alpha = 0.9f)
            ) {
                viewModel.loginWithDemo(Role.KEPALA_RT)
                onLoginSuccess()
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Siprus Version v2.6 • Universitas Muhammadiyah Semarang",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun DemoRoleCard(
    title: String,
    email: String,
    iconColor: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = email,
                    fontSize = 11.sp,
                    color = Color(0xFF475569)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF64748B)
            )
        }
    }
}
