package com.example.data.repo

import com.example.data.Ruangan
import com.example.domain.interfaces.IRoomRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RoomRepo : IRoomRepo {
    override fun getAllRooms(): Flow<List<Ruangan>> = flow {
        // Implementation
        emit(emptyList())
    }
}
