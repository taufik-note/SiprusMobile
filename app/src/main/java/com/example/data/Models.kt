package com.example.data

enum class Role {
    MAHASISWA,
    ADMIN_RT,
    KEPALA_RT,
    GUEST
}

data class User(
    val email: String,
    val name: String,
    val role: Role,
    val idPengenal: String,
    val kredensialSesi: String = "Terverifikasi Kampus"
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
    val tipe: String, // e.g. "Kantor", "Kelas", "KM/WC", "Dapur", "Laboratorium"
    val deskripsi: String
)

enum class PeminjamanStatus {
    MENUNGGU_VERIFIKASI_RT,       // Yellow: Status Kuning
    MENUNGGU_VERIFIKASI_SIPRUS,   // Blue: Status Biru
    DISETUJUI,                    // Green: Status Hijau
    DITOLAK                       // Red
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
    val diajukanPada: String = "01/06/2026, 08:00"
)

object MockData {
    val initialGedungList = listOf(
        Gedung("G1", "REK", "Gedung Rektorat UNIMUS", "Kampus 1, Jl. Kedungmundu"),
        Gedung("G2", "KAS", "Gedung Kasipah UNIMUS", "Kampus 3, Jl. Kasipah"),
        Gedung("G3", "KM2", "Gedung Kedungmundu II (GKB 2)", "Kampus 4, Jl. Kedungmundu"),
        Gedung("G4", "RUS110", "Rusunawa Kampus 110", "Kampus 1, Samping Rektorat"),
        Gedung("G5", "WON", "Gedung Fakultas Kedokteran (Wonodri)", "Kampus Wonodri, Jl. Wonodri Sendang No. 6"),
        Gedung("G6", "PDM", "Gedung PDM / Muhammadiyah", "Kampus 2, Jl. Kedungmundu"),
        Gedung("G7", "NRC", "Nursing Research Center (NRC)", "Kampus 1, Jl. Kedungmundu"),
        Gedung("G8", "RUS", "Asrama Mahasiswa (Rusunawa UNIMUS)", "Kampus 1, Samping Masjid Al-Azhar")
    )

    val initialRuanganList = listOf(
        Ruangan("R1", "1A101", "1A101 - Kantor (Ka. Prodi Kep / Profesi)", "REK", 1, 15, "Kantor", "Luas: 9 m², Digunakan untuk: Ka. Prodi Kep / Profesi"),
        Ruangan("R2", "1A102", "1A102 - Kantor (Lemlit / LPM)", "REK", 1, 10, "Kantor", "Luas: 15 m², Digunakan untuk: Lemlit / LPM"),
        Ruangan("R3", "1A103", "1A103 - Kantor (BPM dan LSIK)", "REK", 1, 15, "Kantor", "Luas: 15 m², Digunakan untuk: BPM dan LSIK"),
        Ruangan("R4", "1A104", "1A104 - KM/WC (Umum)", "REK", 1, 5, "KM/WC", "Luas: 6 m², Digunakan untuk: Umum"),
        Ruangan("R5", "1A106", "1A106 - Kantor (Koordinator Driver)", "REK", 1, 12, "Kantor", "Luas: 6 m², Digunakan untuk: Koordinator Driver"),
        Ruangan("R6", "1A107", "1A107 - Kantor (DOSEN S1 KEP)", "REK", 1, 20, "Kantor", "Luas: 15 m², Digunakan untuk: DOSEN S1 KEP"),
        Ruangan("R7", "1A108", "1A108 - Kantor (Admin S1 dan Profesi Kep)", "REK", 1, 18, "Kantor", "Luas: 15 m², Digunakan untuk: Admin S1 dan Profesi Kep"),
        Ruangan("R8", "1A109", "1A109 - KM/WC (Umum)", "REK", 1, 3, "KM/WC", "Luas: 28 m², Digunakan untuk: Umum"),
        Ruangan("R9", "1A110", "1A110 - Kantor (Ka. Prodi S1 Kep)", "REK", 1, 10, "Kantor", "Luas: 6 m², Digunakan untuk: Ka. Prodi S1 Kep"),
        Ruangan("R10", "1A204", "1A204 - Kantor (BAUK)", "REK", 2, 7, "Kantor", "Gedung Rektorat UNIMUS Lantai 2, Kapasitas: 7 Sektor Kursi")
    )

    val initialPeminjamanList = listOf(
        Peminjaman(
            id = "PEM1",
            namaMahasiswa = "Taufik Hidayat (Mahasiswa)",
            emailMahasiswa = "taufik@unimus.ac.id",
            ruanganKode = "1A101",
            ruanganNama = "1A101 - Kantor (Ka. Prodi Kep / Profesi)",
            gedungKode = "REK",
            gedungNama = "Gedung Rektorat UNIMUS",
            lantai = 1,
            kapasitas = 2,
            tanggal = "2026-05-20",
            jamMulai = "08:00",
            jamSelesai = "10:00",
            tujuan = "Kuliah Tamu S1 Keperawatan UNIMUS",
            status = PeminjamanStatus.DISETUJUI,
            diajukanPada = "20/5/2026, 21.08.13"
        ),
        Peminjaman(
            id = "PEM2",
            namaMahasiswa = "Taufik Hidayat (Mahasiswa)",
            emailMahasiswa = "taufik@unimus.ac.id",
            ruanganKode = "1A107",
            ruanganNama = "1A107 - Kantor (DOSEN S1 KEP)",
            gedungKode = "REK",
            gedungNama = "Gedung Rektorat UNIMUS",
            lantai = 1,
            kapasitas = 6,
            tanggal = "2026-05-23",
            jamMulai = "11:00",
            jamSelesai = "13:00",
            tujuan = "Rapat Organisasi Mahasiswa BEM FIKKES",
            status = PeminjamanStatus.MENUNGGU_VERIFIKASI_RT,
            diajukanPada = "23/5/2026, 21.08.13"
        ),
        Peminjaman(
            id = "PEM3",
            namaMahasiswa = "Taufik Hidayat (Mahasiswa)",
            emailMahasiswa = "taufik@unimus.ac.id",
            ruanganKode = "1A204",
            ruanganNama = "1A204 - Kantor (BAUK)",
            gedungKode = "REK",
            gedungNama = "Gedung Rektorat UNIMUS",
            lantai = 2,
            kapasitas = 7,
            tanggal = "2026-05-24",
            jamMulai = "14:00",
            jamSelesai = "16:00",
            tujuan = "Ujian Praktikum Klinik Lab Keperawatan",
            status = PeminjamanStatus.MENUNGGU_VERIFIKASI_SIPRUS,
            diajukanPada = "23/5/2026, 09.08.13"
        )
    )
}
