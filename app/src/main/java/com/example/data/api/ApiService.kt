package com.example.data.api

import com.example.data.BookingRequest
import com.example.data.Gedung
import com.example.data.LoginResponse
import com.example.data.Notification
import com.example.data.Peminjaman
import com.example.data.ProfileResponse
import com.example.data.ResponseModel
import com.example.data.Ruangan
import com.example.data.User
import com.example.data.ValidateRequest
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────

    @POST("auth/login")
    suspend fun login(
        @Body credentials: Map<String, String>
    ): Response<LoginResponse>

    // ── Gedung ────────────────────────────────────────────────────────────────

    @GET("gedung")
    suspend fun getGedung(
        @Header("Authorization") token: String? = null
    ): List<Gedung>

    @POST("gedung")
    suspend fun addGedung(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<ResponseModel>

    // ── Ruangan ───────────────────────────────────────────────────────────────

    @GET("ruang")
    suspend fun getRooms(
        @Header("Authorization") token: String? = null
    ): List<Ruangan>

    @GET("ruang/available")
    suspend fun getAvailableRooms(
        @Header("Authorization") token: String? = null,
        @Query("tanggal") tanggal: String,
        @Query("waktuMulai") waktuMulai: String,
        @Query("waktuSelesai") waktuSelesai: String,
        @Query("kapasitas") kapasitas: String?,
        @Query("gedungId") gedungId: String?
    ): List<Ruangan>

    @POST("ruang")
    suspend fun addRuangan(
        @Header("Authorization") token: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<ResponseModel>

    // ── Booking ───────────────────────────────────────────────────────────────

    @GET("booking/history")
    suspend fun getMyHistory(
        @Header("Authorization") token: String? = null
    ): List<Peminjaman>

    @GET("booking/all")
    suspend fun getAllBookings(
        @Header("Authorization") token: String? = null
    ): List<Peminjaman>

    @POST("booking")
    suspend fun createBooking(
        @Header("Authorization") token: String,
        @Body body: BookingRequest
    ): Response<ResponseModel>

    @PUT("booking/{id}/validate")
    suspend fun validateBooking(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: ValidateRequest
    ): Response<ResponseModel>

    @PUT("booking/{id}/transfer")
    suspend fun switchRoom(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: com.example.data.SwitchRoomRequest
    ): Response<ResponseModel>

    // ── Profil ────────────────────────────────────────────────────────────────

    @PUT("auth/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<ProfileResponse>

    @PUT("auth/password")
    suspend fun updatePassword(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<ResponseModel>

    // ── Notifications ─────────────────────────────────────────────────────────

    @GET("notifications")
    suspend fun getNotifications(
        @Header("Authorization") token: String? = null
    ): List<Notification>

    @PUT("notifications/read-all")
    suspend fun markAllNotificationsAsRead(
        @Header("Authorization") token: String
    ): Response<ResponseModel>

    @PUT("notifications/{id}/read")
    suspend fun markNotificationAsRead(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ResponseModel>
}