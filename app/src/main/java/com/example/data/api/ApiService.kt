package com.example.data.api

import com.example.data.Ruangan
import retrofit2.http.GET

interface ApiService {
    @GET("rooms")
    suspend fun getRooms(): List<Ruangan>
}
