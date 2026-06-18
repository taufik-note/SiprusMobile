package com.example.domain.interfaces

import com.example.data.Ruangan
import kotlinx.coroutines.flow.Flow

interface IRoomRepo {
    fun getAllRooms(): Flow<List<Ruangan>>
}
