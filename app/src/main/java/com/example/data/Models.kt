package com.example.data

import com.google.gson.annotations.SerializedName

// ── Role ──────────────────────────────────────────────────────────────────────

enum class Role {
    @SerializedName("MAHASISWA", alternate = ["mahasiswa"])
    MAHASISWA,

    @SerializedName("ADMIN_RT", alternate = ["admin_rt"])
    ADMIN_RT,

    @SerializedName("KEPALA_RT", alternate = ["kepala_rt"])
    KEPALA_RT,

    @SerializedName("GUEST", alternate = ["guest"])
    GUEST
}

// ── User ──────────────────────────────────────────────────────────────────────

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

// ── PeminjamanStatus ──────────────────────────────────────────────────────────

enum class PeminjamanStatus {
    @SerializedName("MENUNGGU_RT", alternate = ["menunggu_rt"])
    MENUNGGU_RT,

    @SerializedName("MENUNGGU_KEPALA", alternate = ["menunggu_kepala"])
    MENUNGGU_KEPALA,

    @SerializedName("DISETUJUI", alternate = ["disetujui"])
    DISETUJUI,

    @SerializedName("DITOLAK_RT", alternate = ["ditolak_rt"])
    DITOLAK_RT,

    @SerializedName("DITOLAK_KEPALA", alternate = ["ditolak_kepala"])
    DITOLAK_KEPALA,

    @SerializedName("BUTUH_REVISI", alternate = ["butuh_revisi"])
    BUTUH_REVISI
}

// ── Peminjaman ────────────────────────────────────────────────────────────────

data class Peminjaman(
    @SerializedName("id")
    val id: Int,

    @SerializedName("userId")
    val userId: Int,

    @SerializedName("ruangId")
    val ruangId: Int,

    @SerializedName("tanggal")
    val tanggal: String,

    @SerializedName("waktuMulai")
    val waktuMulai: String,

    @SerializedName("waktuSelesai")
    val waktuSelesai: String,

    @SerializedName("keperluan")
    val keperluan: String,

    @SerializedName("status")
    val status: PeminjamanStatus,

    // Catatan dari Admin RT saat menolak
    @SerializedName("catatanPenolakan")
    val catatanRt: String? = null,

    // Catatan dari Kepala saat mengalihkan / menolak
    @SerializedName("catatanPeralihan")
    val catatanKepala: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    // Relasi — dikirim backend jika query include user & ruang
    @SerializedName("user")
    val user: User? = null,

    @SerializedName("ruang")
    val ruang: Ruangan? = null
)

// ── Gedung ────────────────────────────────────────────────────────────────────

data class Gedung(
    @SerializedName("id")
    val id: Int,

    @SerializedName("kode")
    val kode: String,

    @SerializedName("nama")
    val nama: String,

    @SerializedName("lokasi")
    val lokasi: String? = null
)

// ── Ruangan ───────────────────────────────────────────────────────────────────

data class Ruangan(
    @SerializedName("id")
    val id: Int,

    @SerializedName("kode")
    val kode: String,

    @SerializedName("nama")
    val nama: String,

    // camelCase "gedungId" sesuai kolom SQL
    @SerializedName("gedungId")
    val gedungId: Int,

    @SerializedName("lantai")
    val lantai: Int,

    @SerializedName("kapasitas")
    val kapasitas: Int,

    @SerializedName("jenis")
    val jenis: String,

    @SerializedName("fasilitas")
    val fasilitas: String? = null,

    // Relasi — dikirim backend jika query include gedung
    @SerializedName("gedung")
    val gedung: Gedung? = null
)

// ── ResponseModel (Generic API Response) ─────────────────────────────────────

data class ResponseModel(
    @SerializedName("message")
    val message: String? = null,

    @SerializedName("error")
    val error: String? = null,

    @SerializedName("success")
    val success: Boolean? = null
)