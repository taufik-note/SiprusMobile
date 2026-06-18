package com.example.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PeminjamanStatus

@Composable
fun StatusTag(status: PeminjamanStatus) {
    val (bgColor, textColor, label) = when (status) {
        PeminjamanStatus.MENUNGGU_VERIFIKASI_RT -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "VERIFIKASI RT")
        PeminjamanStatus.MENUNGGU_VERIFIKASI_SIPRUS -> Triple(Color(0xFFDBEAFE), Color(0xFF2563EB), "VERIFIKASI SIPRUS")
        PeminjamanStatus.DISETUJUI -> Triple(Color(0xFFD1FAE5), Color(0xFF059669), "DISETUJUI")
        PeminjamanStatus.DITOLAK -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), "DITOLAK")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
