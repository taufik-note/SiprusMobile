package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.PeminjamanStatus
import com.example.data.Role
import com.example.ui.screen.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppViewModel

enum class MainTab {
    BERANDA,
    CARI,
    RIWAYAT,
    VALIDASI,
    MASTER,
    LAPORAN,
    PANDUAN,
    PROFIL,
    NOTIFIKASI
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val appViewModel: AppViewModel = viewModel()
                UniroomAppContent(appViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniroomAppContent(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val peminjamanList by viewModel.peminjamanList.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    var activeTab by remember { mutableStateOf(MainTab.BERANDA) }

    // If session is empty, present Login directly
    if (currentUser == null) {
        LoginScreen(
            viewModel = viewModel,
            onLoginSuccess = {
                activeTab = MainTab.BERANDA
            }
        )
    } else {
        val userRole = currentUser?.role ?: Role.GUEST

        // Count pending validations dynamically to display badge
        val pendingCount = peminjamanList.count { p ->
            if (userRole == Role.ADMIN_RT) {
                p.status == PeminjamanStatus.MENUNGGU_RT
            } else if (userRole == Role.KEPALA_RT) {
                p.status == PeminjamanStatus.MENUNGGU_KEPALA
            } else {
                p.status == PeminjamanStatus.MENUNGGU_RT || p.status == PeminjamanStatus.MENUNGGU_KEPALA
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color(0xFF4F46E5),
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "UNIROOM",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1E293B),
                                letterSpacing = 0.5.sp
                            )
                        }
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            // Panduan Button
                            Surface(
                                onClick = { activeTab = MainTab.PANDUAN },
                                color = Color(0xFFEEF2F6),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = "Panduan",
                                        tint = Color(0xFF4F46E5),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Notification Button
                            Box {
                                IconButton(
                                    onClick = { activeTab = MainTab.NOTIFIKASI },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    val unreadCount = notifications.count { !it.dibaca }
                                    BadgedBox(
                                        badge = {
                                            if (unreadCount > 0) {
                                                Badge(
                                                    containerColor = Color(0xFFEF4444),
                                                    modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                                                ) {
                                                    Text(
                                                        text = unreadCount.coerceAtMost(9).toString(),
                                                        color = Color.White,
                                                        fontSize = 8.sp
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsNone,
                                            contentDescription = "Notifications",
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            // Logout Button
                            Surface(
                                onClick = { viewModel.logout() },
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = "Logout",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    ),
                    modifier = Modifier.shadow(4.dp)
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 6.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    // 1. Beranda
                    NavigationBarItem(
                        selected = activeTab == MainTab.BERANDA,
                        onClick = { activeTab = MainTab.BERANDA },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                        label = { Text("Beranda", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1E3A8A),
                            indicatorColor = Color(0xFFEFF6FF)
                        )
                    )

                    // 2. Cari (Hide for Admin/Kepala RT)
                    if (userRole != Role.ADMIN_RT && userRole != Role.KEPALA_RT) {
                        NavigationBarItem(
                            selected = activeTab == MainTab.CARI,
                            onClick = { activeTab = MainTab.CARI },
                            icon = { Icon(Icons.Default.Search, contentDescription = "Cari") },
                            label = { Text("Cari", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF1E3A8A),
                                indicatorColor = Color(0xFFEFF6FF)
                            )
                        )
                    }

                    // 3. Riwayat (Only for student)
                    if (userRole == Role.MAHASISWA) {
                        NavigationBarItem(
                            selected = activeTab == MainTab.RIWAYAT,
                            onClick = { activeTab = MainTab.RIWAYAT },
                            icon = { Icon(Icons.Default.Receipt, contentDescription = "Riwayat") },
                            label = { Text("Riwayat", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF1E3A8A),
                                indicatorColor = Color(0xFFEFF6FF)
                            )
                        )
                    }

                    // 4. Validasi (Only for Admin/Kepala RT)
                    if (userRole == Role.ADMIN_RT || userRole == Role.KEPALA_RT) {
                        NavigationBarItem(
                            selected = activeTab == MainTab.VALIDASI,
                            onClick = { activeTab = MainTab.VALIDASI },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (pendingCount > 0) {
                                            Badge { Text(pendingCount.toString()) }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.FactCheck, contentDescription = "Validasi")
                                }
                            },
                            label = { Text("Validasi", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF1E3A8A),
                                indicatorColor = Color(0xFFEFF6FF)
                            )
                        )
                    }

                    // 4b. Laporan (Only for Admin/Kepala RT)
                    if (userRole == Role.ADMIN_RT || userRole == Role.KEPALA_RT) {
                        NavigationBarItem(
                            selected = activeTab == MainTab.LAPORAN,
                            onClick = { activeTab = MainTab.LAPORAN },
                            icon = { Icon(Icons.Default.Assessment, contentDescription = "Laporan") },
                            label = { Text("Laporan", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF1E3A8A),
                                indicatorColor = Color(0xFFEFF6FF)
                            )
                        )
                    }

                    // 5. Master (Only visible or accessible for Admin/Kepala RT structure)
                    if (userRole == Role.ADMIN_RT || userRole == Role.KEPALA_RT) {
                        NavigationBarItem(
                            selected = activeTab == MainTab.MASTER,
                            onClick = { activeTab = MainTab.MASTER },
                            icon = { Icon(Icons.Default.Storage, contentDescription = "Master") },
                            label = { Text("Master", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF1E3A8A),
                                indicatorColor = Color(0xFFEFF6FF)
                            )
                        )
                    }

                    // 7. Profil
                    NavigationBarItem(
                        selected = activeTab == MainTab.PROFIL,
                        onClick = { activeTab = MainTab.PROFIL },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                        label = { Text("Profil", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1E3A8A),
                            indicatorColor = Color(0xFFEFF6FF)
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (activeTab) {
                    MainTab.BERANDA -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToBooking = {
                            activeTab = MainTab.CARI
                        }
                    )
                    MainTab.CARI -> BookingScreen(
                        viewModel = viewModel,
                        onNavigateToHistory = {
                            activeTab = MainTab.RIWAYAT
                        }
                    )
                    MainTab.RIWAYAT -> HistoryScreen(
                        viewModel = viewModel
                    )
                    MainTab.VALIDASI -> ValidationScreen(
                        viewModel = viewModel
                    )
                    MainTab.MASTER -> MasterDataScreen(
                        viewModel = viewModel
                    )
                    MainTab.LAPORAN -> ReportScreen(
                        viewModel = viewModel
                    )
                    MainTab.PANDUAN -> GuideScreen()
                    MainTab.PROFIL -> ProfileScreen(
                        viewModel = viewModel,
                        onLogoutClicked = {
                            viewModel.logout()
                            activeTab = MainTab.BERANDA
                        }
                    )
                    MainTab.NOTIFIKASI -> NotificationScreen(
                        viewModel = viewModel,
                        onBack = { activeTab = MainTab.BERANDA }
                    )
                }
            }
        }
    }
}
