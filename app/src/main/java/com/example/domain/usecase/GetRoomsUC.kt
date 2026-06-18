package com.example.domain.usecase

import com.example.domain.interfaces.IRoomRepo
// import javax.inject.Inject

class GetRoomsUC /* @Inject constructor */ (
    private val repository: IRoomRepo
) {
    operator fun invoke() = repository.getAllRooms()
}
