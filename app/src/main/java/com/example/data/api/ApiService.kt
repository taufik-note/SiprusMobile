package com.example.data.api

import com.example.data.Gedung
import com.example.data.Peminjaman
import com.example.data.Ruangan
import com.example.data.User
import com.example.ResponseModel
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body credentials: Map<String, String>): Response<User>

    @GET("gedung")
    suspend fun getGedung(): List<Gedung>

    @GET("ruang")
    suspend fun getRooms(): List<Ruangan>

    @GET("booking/my")
    suspend fun getMyHistory(@Header("Authorization") token: String): List<Peminjaman>

    @GET("booking")
    suspend fun getAllBookings(@Header("Authorization") token: String): List<Peminjaman>

    @POST("booking")
    suspend fun createBooking(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<ResponseModel>

    @PATCH("booking/{id}/validate")
    suspend fun validateBooking(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<ResponseModel>

    @POST("gedung")
    suspend fun addGedung(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<ResponseModel>

    @POST("ruang")
    suspend fun addRuangan(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<ResponseModel>

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