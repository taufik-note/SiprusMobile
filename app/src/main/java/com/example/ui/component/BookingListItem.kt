package com.example.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Peminjaman

@Composable
fun BookingListItem(item: Peminjaman) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = item.ruanganKode,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                }
                StatusTag(status = item.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = item.ruanganNama,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            DetailRow(icon = Icons.Default.Person, text = "Pemohon: ${item.namaMahasiswa}")
            DetailRow(icon = Icons.Default.Label, text = "\"${item.tujuan}\"")

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(item.tanggal, fontSize = 11.sp, color = Color(0xFF64748B))

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(Icons.Default.AccessTime, null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${item.jamMulai} WIB", fontSize = 11.sp, color = Color(0xFF64748B))
                }

                Text(
                    text = "ID: #${item.id.replace("PEM", "")}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCBD5E1)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(icon, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 12.sp, color = Color(0xFF475569))
    }
}
