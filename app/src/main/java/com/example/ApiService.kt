package com.example

import com.example.data.Gedung
import com.example.data.Peminjaman
import com.example.data.Ruangan
import com.example.data.User
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @GET("/")
    fun ambilDataSiprus(): Call<ResponseModel>

    // --- AUTHENTICATION ---
    @POST("login")
    suspend fun login(@Body credentials: Map<String, String>): User

    // --- CRUD GEDUNG ---
    @GET("gedung")
    suspend fun getGedung(): List<Gedung>

    @POST("gedung")
    suspend fun addGedung(@Body gedung: Gedung): ResponseModel

    @DELETE("gedung/{id}")
    suspend fun deleteGedung(@Path("id") id: String): ResponseModel

    // --- CRUD RUANGAN ---
    @GET("ruangan")
    suspend fun getRuangan(): List<Ruangan>

    @POST("ruangan")
    suspend fun addRuangan(@Body ruangan: Ruangan): ResponseModel

    @DELETE("ruangan/{id}")
    suspend fun deleteRuangan(@Path("id") id: String): ResponseModel

    // --- CRUD PEMINJAMAN (BOOKING) ---
    @GET("peminjaman")
    suspend fun getPeminjaman(): List<Peminjaman>

    @POST("peminjaman")
    suspend fun createPeminjaman(@Body peminjaman: Peminjaman): ResponseModel

    @PATCH("peminjaman/{id}/approve")
    suspend fun approvePeminjaman(
        @Path("id") id: String,
        @Query("role") role: String
    ): ResponseModel

    @PATCH("peminjaman/{id}/reject")
    suspend fun rejectPeminjaman(
        @Path("id") id: String,
        @Query("role") role: String
    ): ResponseModel

    @PATCH("peminjaman/{id}/relocate")
    suspend fun relocatePeminjaman(
        @Path("id") id: String,
        @Query("alternativeRoomKode") altRoomKode: String
    ): ResponseModel
}
