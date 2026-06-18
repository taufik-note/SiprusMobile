package com.example.data

import com.google.gson.annotations.SerializedName

enum class Role {
    MAHASISWA,
    ADMIN_RT,
    KEPALA_RT,
    GUEST
}

data class User(
    @SerializedName("email")
    val email: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("role")
    val role: Role,
    @SerializedName("idPengenal")
    val idPengenal: String,
    @SerializedName("kredensialSesi")
    val kredensialSesi: String = "Terverifikasi Kampus"
)

enum class PeminjamanStatus {
    MENUNGGU_VERIFIKASI_RT,
    MENUNGGU_VERIFIKASI_SIPRUS,
    DISETUJUI,
    DITOLAK
}

data class Peminjaman(
    val id: String,
    val namaMahasiswa: String,
    val emailMahasiswa: String,
    val ruanganKode: String,
    val ruanganNama: String,
    val gedungKode: String,
    val gedungNama: String,
    val lantai: Int,
    val kapasitas: Int,
    val tanggal: String,
    val jamMulai: String,
    val jamSelesai: String,
    val tujuan: String,
    val status: PeminjamanStatus,
    val diajukanPada: String
)

data class Gedung(
    val id: String,
    val kode: String,
    val nama: String,
    val lokasi: String
)

data class Ruangan(
    val id: String,
    val kode: String,
    val nama: String,
    val gedungId: String,
    val lantai: Int,
    val kapasitas: Int,
    val tipe: String,
    val deskripsi: String
)
