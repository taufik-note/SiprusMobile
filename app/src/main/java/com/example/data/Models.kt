package com.example.data

import com.google.gson.annotations.SerializedName

enum class Role {
    @SerializedName("MAHASISWA")
    MAHASISWA,
    @SerializedName("ADMIN_RT")
    ADMIN_RT,
    @SerializedName("KEPALA_RT")
    KEPALA_RT,
    @SerializedName("GUEST")
    GUEST
}

data class User(
    @SerializedName("id")
    val id: Int,
    @SerializedName("email")
    val email: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("role")
    val role: Role,
    @SerializedName("token")
    val token: String? = null
)

enum class PeminjamanStatus {
    @SerializedName("MENUNGGU_RT")
    MENUNGGU_RT,
    @SerializedName("MENUNGGU_KEPALA")
    MENUNGGU_KEPALA,
    @SerializedName("DISETUJUI")
    DISETUJUI,
    @SerializedName("DITOLAK_RT")
    DITOLAK_RT,
    @SerializedName("DITOLAK_KEPALA")
    DITOLAK_KEPALA,
    @SerializedName("BUTUH_REVISI")
    BUTUH_REVISI
}

data class Peminjaman(
    val id: Int,
    val userId: Int,
    val ruangId: Int,
    val tanggal: String,
    val waktuMulai: String,
    val waktuSelesai: String,
    val keperluan: String,
    val status: PeminjamanStatus,
    val catatanRt: String? = null,
    val catatanKepala: String? = null,
    val createdAt: String? = null,

    // UI Helpers (if backend sends populated data)
    @SerializedName("user")
    val user: User? = null,
    @SerializedName("ruang")
    val ruang: Ruangan? = null
)

data class Gedung(
    val id: Int,
    val kode: String,
    val nama: String,
    val lokasi: String? = null
)

data class Ruangan(
    val id: Int,
    val kode: String,
    val nama: String,
    val gedungId: Int,
    val lantai: Int,
    val kapasitas: Int,
    val jenis: String,
    val fasilitas: String?,
    @SerializedName("gedung")
    val gedung: Gedung? = null
)