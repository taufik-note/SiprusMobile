package com.example.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.Ruangan

@Composable
fun RoomCard(ruangan: Ruangan, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = ruangan.nama, style = MaterialTheme.typography.titleMedium)
            Text(text = ruangan.kode, style = MaterialTheme.typography.bodySmall)
        }
    }
}
