package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GuideScreen() {
    val scrollState = rememberScrollState()

    var activeInfoTab by remember { mutableStateOf(0) } // 0 = Alur Pinjam, 1 = Aturan Umum, 2 = FAQ

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Guide Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Panduan & Regulasi Ruang", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                }
                Text(
                    text = "Persyaratan & Aturan Resmi Universitas Muhammadiyah Semarang (UNIMUS)",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Toggle Buttons segment
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = { activeInfoTab = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeInfoTab == 0) Color(0xFF4F46E5) else Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text(
                    "❶ Alur Pinjam",
                    color = if (activeInfoTab == 0) Color.White else Color(0xFF475569),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { activeInfoTab = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeInfoTab == 1) Color(0xFF4F46E5) else Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text(
                    "❷ Aturan Umum",
                    color = if (activeInfoTab == 1) Color.White else Color(0xFF475569),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { activeInfoTab = 2 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeInfoTab == 2) Color(0xFF4F46E5) else Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text(
                    "❸ Kontak RT",
                    color = if (activeInfoTab == 2) Color.White else Color(0xFF475569),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Selected Content Box
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                when (activeInfoTab) {
                    0 -> {
                        Text(
                            text = "Skema Pelayanan Otorisasi 2-Tingkat Digital:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Step cards replicating popup sidebar in reference
                        StepProgressRow(
                            number = "1",
                            role = "Ajukan Pengajuan (Mahasiswa)",
                            desc = "Pilih ruangan kosong di tab 'Cari & Pinjam', isi detail agenda kegiatan resmi, serta kirim permohonan.",
                            numBgColor = Color(0xFFEFF6FF),
                            numColor = Color(0xFF3B82F6)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        StepProgressRow(
                            number = "2",
                            role = "Review & Verifikasi (Admin RT)",
                            desc = "Biro Admin Rumah Tangga memvalidasi kesesuaian berkas serta menyetujui kuintansi / pengalihan jadwal bebas konflik akademis.",
                            numBgColor = Color(0xFFFEF3C7),
                            numColor = Color(0xFFD97706)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        StepProgressRow(
                            number = "3",
                            role = "Persetujuan Kepala RT (Siprus)",
                            desc = "Kepala Bagian RT menerbitkan persetujuan resmi penuh, membuat ruangan status terbooking, serta mengirim notifikasi ke pemohon.",
                            numBgColor = Color(0xFFECFDF5),
                            numColor = Color(0xFF10B981)
                        )
                    }

                    1 -> {
                        Text(
                            text = "Peraturan & Ketentuan Penggunaan:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        listOf(
                            "Ketepatan Waktu: Penggunaan ruangan wajib sesuai dengan jadwal jam mulai dan selesai yang telah disetujui Kepala RT.",
                            "Kebersihan: Pemohon bertanggung jawab penuh atas kebersihan dan kondisi fisik fasilitas setelah acara selesai.",
                            "Prioritas Kuliah: Kegiatan perkuliahan resmi akademis memiliki prioritas mutlak di atas rapat organisasi mahasiswa.",
                            "Konflik Jadwal: Apabila terjadi bentrok kepentingan struktural kampus, Biro RT berhak melakukan relokasi / pengalihan ruang secara instan."
                        ).forEachIndexed { i, rule ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("${i + 1}. ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFef4444))
                                Text(rule, fontSize = 11.sp, color = Color(0xFF334155), lineHeight = 16.sp)
                            }
                        }
                    }

                    2 -> {
                        Text(
                            text = "Kontak Bantuan & Admin Rumah Tangga Kampus:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Text(
                            text = "Jika Anda mengalami kendala fisik seperti kunci pintu ruang terkunci, mati lampu, AC tidak menyala, atau projector rusak, silakan hubungi:",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        listOf(
                            "📞 Telepon BAUK / RT: Ext (204) Sektor GKB Rektorat",
                            "✉ Email Layanan: bauk@unimus.ac.id",
                            "💬 WhatsApp Bantuan RT: +62 812-3456-7890 (Iqbal Ramadhan)"
                        ).forEach { contact ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = contact,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(35.dp))
    }
}

@Composable
fun StepProgressRow(
    number: String,
    role: String,
    desc: String,
    numBgColor: Color,
    numColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .background(numBgColor, RoundedCornerShape(14.dp))
            ) {
                Text(number, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = numColor)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(role, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(2.dp))
                Text(desc, fontSize = 11.sp, color = Color(0xFF475569), lineHeight = 15.sp)
            }
        }
    }
}
private const val maxTabs = 3
