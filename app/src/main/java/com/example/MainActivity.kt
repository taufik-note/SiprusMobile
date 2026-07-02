package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

    if (currentUser == null) {
        LoginScreen(
            viewModel = viewModel,
            onLoginSuccess = { activeTab = MainTab.BERANDA }
        )
    } else {
        val userRole = currentUser?.role ?: Role.GUEST
        val pendingCount = peminjamanList.count { p ->
            if (userRole == Role.ADMIN_RT) p.status == PeminjamanStatus.MENUNGGU_RT
            else if (userRole == Role.KEPALA_RT) p.status == PeminjamanStatus.MENUNGGU_KEPALA
            else p.status == PeminjamanStatus.MENUNGGU_RT || p.status == PeminjamanStatus.MENUNGGU_KEPALA
        }

        Scaffold(
            containerColor = Color(0xFFF8FAFC),
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = Color(0xFF4F46E5), shape = CircleShape, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Business, null, tint = Color.White, modifier = Modifier.padding(8.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Text("UNIROOM", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A), letterSpacing = 1.sp)
                        }
                    },
                    actions = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(end = 12.dp)) {
                            Surface(onClick = { activeTab = MainTab.PANDUAN }, color = if (activeTab == MainTab.PANDUAN) Color(0xFF4F46E5) else Color(0xFFF1F5F9), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(40.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.MenuBook, "Panduan", tint = if (activeTab == MainTab.PANDUAN) Color.White else Color(0xFF4F46E5), modifier = Modifier.size(20.dp))
                                }
                            }
                            Surface(onClick = { activeTab = MainTab.NOTIFIKASI }, color = if (activeTab == MainTab.NOTIFIKASI) Color(0xFF4F46E5) else Color(0xFFF1F5F9), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(40.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    val unreadCount = notifications.count { !it.dibaca }
                                    BadgedBox(badge = { if (unreadCount > 0) Badge(containerColor = Color(0xFFEF4444), modifier = Modifier.offset(x = (-2).dp, y = 2.dp)) { Text(unreadCount.coerceAtMost(9).toString(), color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black) } }) {
                                        Icon(Icons.Default.Notifications, "Notifikasi", tint = if (activeTab == MainTab.NOTIFIKASI) Color.White else Color(0xFF4F46E5), modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                            Surface(onClick = { viewModel.logout() }, color = Color(0xFFF1F5F9), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(40.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.AutoMirrored.Filled.Logout, "Logout", tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    // 1. Beranda
                    NavigationBarItem(
                        selected = activeTab == MainTab.BERANDA,
                        onClick = { activeTab = MainTab.BERANDA },
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("Beranda", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            indicatorColor = Color(0xFF4F46E5),
                            selectedTextColor = Color(0xFF4F46E5),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        )
                    )

                    // 2. Cari
                    if (userRole != Role.ADMIN_RT && userRole != Role.KEPALA_RT) {
                        NavigationBarItem(
                            selected = activeTab == MainTab.CARI,
                            onClick = { activeTab = MainTab.CARI },
                            icon = { Icon(Icons.Default.Search, null) },
                            label = { Text("Cari", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                indicatorColor = Color(0xFF4F46E5),
                                selectedTextColor = Color(0xFF4F46E5),
                                unselectedIconColor = Color(0xFF64748B),
                                unselectedTextColor = Color(0xFF64748B)
                            )
                        )
                    }

                    // 3. Riwayat
                    if (userRole == Role.MAHASISWA) {
                        NavigationBarItem(
                            selected = activeTab == MainTab.RIWAYAT,
                            onClick = { activeTab = MainTab.RIWAYAT },
                            icon = { Icon(Icons.Default.Receipt, null) },
                            label = { Text("Riwayat", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                indicatorColor = Color(0xFF4F46E5),
                                selectedTextColor = Color(0xFF4F46E5),
                                unselectedIconColor = Color(0xFF64748B),
                                unselectedTextColor = Color(0xFF64748B)
                            )
                        )
                    }

                    // 4. Validasi
                    if (userRole == Role.ADMIN_RT || userRole == Role.KEPALA_RT) {
                        NavigationBarItem(
                            selected = activeTab == MainTab.VALIDASI,
                            onClick = { activeTab = MainTab.VALIDASI },
                            icon = {
                                BadgedBox(badge = { if (pendingCount > 0) Badge { Text(pendingCount.toString()) } }) {
                                    Icon(Icons.Default.FactCheck, null)
                                }
                            },
                            label = { Text("Validasi", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                indicatorColor = Color(0xFF4F46E5),
                                selectedTextColor = Color(0xFF4F46E5),
                                unselectedIconColor = Color(0xFF64748B),
                                unselectedTextColor = Color(0xFF64748B)
                            )
                        )
                    }

                    // 5. Laporan
                    if (userRole == Role.ADMIN_RT || userRole == Role.KEPALA_RT) {
                        NavigationBarItem(
                            selected = activeTab == MainTab.LAPORAN,
                            onClick = { activeTab = MainTab.LAPORAN },
                            icon = { Icon(Icons.Default.Assessment, null) },
                            label = { Text("Laporan", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                indicatorColor = Color(0xFF4F46E5),
                                selectedTextColor = Color(0xFF4F46E5),
                                unselectedIconColor = Color(0xFF64748B),
                                unselectedTextColor = Color(0xFF64748B)
                            )
                        )
                    }

                    // 6. Master
                    if (userRole == Role.ADMIN_RT || userRole == Role.KEPALA_RT) {
                        NavigationBarItem(
                            selected = activeTab == MainTab.MASTER,
                            onClick = { activeTab = MainTab.MASTER },
                            icon = { Icon(Icons.Default.Storage, null) },
                            label = { Text("Master", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                indicatorColor = Color(0xFF4F46E5),
                                selectedTextColor = Color(0xFF4F46E5),
                                unselectedIconColor = Color(0xFF64748B),
                                unselectedTextColor = Color(0xFF64748B)
                            )
                        )
                    }

                    // 7. Profil
                    NavigationBarItem(
                        selected = activeTab == MainTab.PROFIL,
                        onClick = { activeTab = MainTab.PROFIL },
                        icon = { Icon(Icons.Default.Person, null) },
                        label = { Text("Profil", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            indicatorColor = Color(0xFF4F46E5),
                            selectedTextColor = Color(0xFF4F46E5),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                when (activeTab) {
                    MainTab.BERANDA -> DashboardScreen(viewModel = viewModel, onNavigateToBooking = { activeTab = MainTab.CARI })
                    MainTab.CARI -> BookingScreen(viewModel = viewModel, onNavigateToHistory = { activeTab = MainTab.RIWAYAT })
                    MainTab.RIWAYAT -> HistoryScreen(viewModel = viewModel)
                    MainTab.VALIDASI -> ValidationScreen(viewModel = viewModel)
                    MainTab.MASTER -> MasterDataScreen(viewModel = viewModel)
                    MainTab.LAPORAN -> ReportScreen(viewModel = viewModel)
                    MainTab.PANDUAN -> GuideScreen()
                    MainTab.PROFIL -> ProfileScreen(viewModel = viewModel, onLogoutClicked = { viewModel.logout(); activeTab = MainTab.BERANDA })
                    MainTab.NOTIFIKASI -> NotificationScreen(viewModel = viewModel, onBack = { activeTab = MainTab.BERANDA })
                }
            }
        }
    }
}
