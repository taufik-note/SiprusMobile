package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.AppViewModel
import com.example.data.PeminjamanStatus
import com.example.data.Role
import com.example.ui.screen.*
import com.example.ui.theme.MyApplicationTheme

enum class MainTab {
    BERANDA,
    CARI,
    RIWAYAT,
    VALIDASI,
    MASTER,
    LAPORAN,
    PANDUAN,
    PROFIL
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
                p.status == PeminjamanStatus.MENUNGGU_VERIFIKASI_RT
            } else if (userRole == Role.KEPALA_RT) {
                p.status == PeminjamanStatus.MENUNGGU_VERIFIKASI_SIPRUS
            } else {
                p.status == PeminjamanStatus.MENUNGGU_VERIFIKASI_RT || p.status == PeminjamanStatus.MENUNGGU_VERIFIKASI_SIPRUS
            }
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MeetingRoom,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Uniroom Unimus",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    },
                    actions = {
                        // Small role icon badge
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .background(
                                    when (userRole) {
                                        Role.MAHASISWA -> Color(0xFF3B82F6)
                                        Role.ADMIN_RT -> Color(0xFFF59E0B)
                                        Role.KEPALA_RT -> Color(0xFF8B5CF6)
                                        Role.GUEST -> Color(0xFF64748B)
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = userRole.name,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF1E3A8A) // Deep Royal Blue Header
                    )
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

                    // 6. Panduan (Only for students/guests)
                    if (userRole != Role.ADMIN_RT && userRole != Role.KEPALA_RT) {
                        NavigationBarItem(
                            selected = activeTab == MainTab.PANDUAN,
                            onClick = { activeTab = MainTab.PANDUAN },
                            icon = { Icon(Icons.Default.Book, contentDescription = "Panduan") },
                            label = { Text("Panduan", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
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
                        onLogoutClicked = {}
                    )
                }
            }
        }
    }
}
