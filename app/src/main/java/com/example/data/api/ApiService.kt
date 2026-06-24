package com.example.data.api

import com.example.data.Gedung
import com.example.data.Peminjaman
import com.example.data.ResponseModel
import com.example.data.Ruangan
import com.example.data.User
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────

    @POST("auth/login")
    suspend fun login(
        @Body credentials: Map<String, String>
    ): Response<User>

    // ── Gedung ────────────────────────────────────────────────────────────────

    @GET("gedung")
    suspend fun getGedung(
        @Header("Authorization") token: String   // endpoint butuh auth
    ): List<Gedung>

    @POST("gedung")
    suspend fun addGedung(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<ResponseModel>

    // ── Ruangan ───────────────────────────────────────────────────────────────

    @GET("ruang")
    suspend fun getRooms(
        @Header("Authorization") token: String   // endpoint butuh auth
    ): List<Ruangan>

    @POST("ruang")
    suspend fun addRuangan(
        @Header("Authorization") token: String,
        @Body body: Map<String, Any>
    ): Response<ResponseModel>

    // ── Booking ───────────────────────────────────────────────────────────────

    @GET("booking/my")
    suspend fun getMyHistory(
        @Header("Authorization") token: String
    ): List<Peminjaman>

    @GET("booking")
    suspend fun getAllBookings(
        @Header("Authorization") token: String
    ): List<Peminjaman>

    @POST("booking")
    suspend fun createBooking(
        @Header("Authorization") token: String,
        @Body body: Map<String, Any>
    ): Response<ResponseModel>

    @PATCH("booking/{id}/validate")
    suspend fun validateBooking(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<ResponseModel>

    // ── Profil ────────────────────────────────────────────────────────────────

    @PUT("profile/update")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<ResponseModel>

    @PUT("profile/password")
    suspend fun updatePassword(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<ResponseModel>
}