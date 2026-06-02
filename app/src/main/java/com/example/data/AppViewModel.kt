package com.example.data

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppViewModel : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _gedungList = MutableStateFlow<List<Gedung>>(MockData.initialGedungList)
    val gedungList: StateFlow<List<Gedung>> = _gedungList.asStateFlow()

    private val _ruanganList = MutableStateFlow<List<Ruangan>>(MockData.initialRuanganList)
    val ruanganList: StateFlow<List<Ruangan>> = _ruanganList.asStateFlow()

    private val _peminjamanList = MutableStateFlow<List<Peminjaman>>(MockData.initialPeminjamanList)
    val peminjamanList: StateFlow<List<Peminjaman>> = _peminjamanList.asStateFlow()

    // Simulation trace message
    private val _systemLogs = MutableStateFlow<List<String>>(listOf("Sistem Uniroom Unimus Diinisialisasi."))
    val systemLogs: StateFlow<List<String>> = _systemLogs.asStateFlow()

    fun logMessage(msg: String) {
        _systemLogs.value = listOf(msg) + _systemLogs.value
    }

    fun login(email: String, role: Role, name: String = "") {
        val userName = if (name.isNotEmpty()) name else {
            when (role) {
                Role.MAHASISWA -> "Taufik Hidayat (Mahasiswa)"
                Role.ADMIN_RT -> "Iqbal Ramadhan (Admin RT)"
                Role.KEPALA_RT -> "Avril Lavigne (Kepala RT)"
                Role.GUEST -> "Sesi Guest"
            }
        }
        val idPengenal = when (role) {
            Role.MAHASISWA -> "#1"
            Role.ADMIN_RT -> "#42"
            Role.KEPALA_RT -> "#107"
            Role.GUEST -> "#GUEST"
        }
        _currentUser.value = User(
            email = email,
            name = userName,
            role = role,
            idPengenal = idPengenal
        )
        logMessage("User ${userName} masuk sebagai ${role.name}")
    }

    fun loginWithDemo(role: Role) {
        when (role) {
            Role.MAHASISWA -> login("taufik@unimus.ac.id", Role.MAHASISWA, "Taufik Hidayat (Mahasiswa)")
            Role.ADMIN_RT -> login("iqbal@unimus.ac.id", Role.ADMIN_RT, "Iqbal Ramadhan (Admin RT)")
            Role.KEPALA_RT -> login("avril@unimus.ac.id", Role.KEPALA_RT, "Avril Lavigne (Kepala RT)")
            Role.GUEST -> login("guest@unimus.ac.id", Role.GUEST, "Sesi Guest")
        }
    }

    fun logout() {
        val oldUser = _currentUser.value?.name ?: "Seseorang"
        _currentUser.value = null
        logMessage("${oldUser} keluar dari sesi.")
    }

    // Creating a booking request (Mahasiswa)
    fun createBooking(
        ruanganCode: String,
        tanggal: String,
        jamMulai: String,
        jamSelesai: String,
        tujuan: String
    ): Boolean {
        val activeUser = _currentUser.value ?: return false
        val room = _ruanganList.value.find { it.kode == ruanganCode } ?: return false
        val gedung = _gedungList.value.find { it.id == room.gedungId } ?: return false

        val sdf = SimpleDateFormat("dd/M/yyyy, HH.mm.ss", Locale.getDefault())
        val dateString = sdf.format(Date())

        val nextId = "PEM" + (_peminjamanList.value.size + 1)
        val newBooking = Peminjaman(
            id = nextId,
            namaMahasiswa = activeUser.name,
            emailMahasiswa = activeUser.email,
            ruanganKode = room.kode,
            ruanganNama = room.nama,
            gedungKode = gedung.kode,
            gedungNama = gedung.nama,
            lantai = room.lantai,
            kapasitas = room.kapasitas,
            tanggal = tanggal,
            jamMulai = jamMulai,
            jamSelesai = jamSelesai,
            tujuan = tujuan,
            status = PeminjamanStatus.MENUNGGU_VERIFIKASI_RT,
            diajukanPada = dateString
        )

        _peminjamanList.value = _peminjamanList.value + newBooking
        logMessage("Booking baru diajukan oleh ${activeUser.name}: Kode ${room.kode}")
        return true
    }

    // Admin RT Approval (Level 1)
    fun adminApprove(id: String) {
        _peminjamanList.value = _peminjamanList.value.map {
            if (it.id == id && it.status == PeminjamanStatus.MENUNGGU_VERIFIKASI_RT) {
                logMessage("Admin RT menyetujui Peminjaman ${it.id}. Diteruskan ke Kepala RT.")
                it.copy(status = PeminjamanStatus.MENUNGGU_VERIFIKASI_SIPRUS)
            } else {
                it
            }
        }
    }

    // Admin RT Reject (Level 1)
    fun adminReject(id: String) {
        _peminjamanList.value = _peminjamanList.value.map {
            if (it.id == id && it.status == PeminjamanStatus.MENUNGGU_VERIFIKASI_RT) {
                logMessage("Admin RT menolak Peminjaman ${it.id}.")
                it.copy(status = PeminjamanStatus.DITOLAK)
            } else {
                it
            }
        }
    }

    // Kepala RT Approval (Level 2 Final)
    fun kepalaApprove(id: String) {
        _peminjamanList.value = _peminjamanList.value.map {
            if (it.id == id && it.status == PeminjamanStatus.MENUNGGU_VERIFIKASI_SIPRUS) {
                logMessage("Kepala RT memberikan persetujuan final (Disetujui) untuk ${it.id}.")
                it.copy(status = PeminjamanStatus.DISETUJUI)
            } else {
                it
            }
        }
    }

    // Kepala RT Reject (Level 2 Final)
    fun kepalaReject(id: String) {
        _peminjamanList.value = _peminjamanList.value.map {
            if (it.id == id && it.status == PeminjamanStatus.MENUNGGU_VERIFIKASI_SIPRUS) {
                logMessage("Kepala RT menolak Peminjaman ${it.id}.")
                it.copy(status = PeminjamanStatus.DITOLAK)
            } else {
                it
            }
        }
    }

    // Kepala RT Relocate booking to alternative room (Alihkan Ruangan - Interactive Sandbox)
    fun kepalaRelocate(id: String, alternativeRoomKode: String) {
        val alternativeRoom = _ruanganList.value.find { it.kode == alternativeRoomKode } ?: return
        val altGedung = _gedungList.value.find { it.id == alternativeRoom.gedungId } ?: return

        _peminjamanList.value = _peminjamanList.value.map {
            if (it.id == id) {
                logMessage("Kepala RT mengalihkan ruangan Peminjaman ${it.id} dari ${it.ruanganKode} ke ${alternativeRoom.kode} karena urusan mendesak / bentrok.")
                it.copy(
                    ruanganKode = alternativeRoom.kode,
                    ruanganNama = alternativeRoom.nama,
                    gedungKode = altGedung.kode,
                    gedungNama = altGedung.nama,
                    lantai = alternativeRoom.lantai,
                    kapasitas = alternativeRoom.kapasitas,
                    status = PeminjamanStatus.DISETUJUI // Auto approved on successful relocation
                )
            } else {
                it
            }
        }
    }

    // Manage Gedung (Master Data)
    fun addGedung(kode: String, nama: String, lokasi: String) {
        val newId = "G" + (_gedungList.value.size + 1)
        val newGedung = Gedung(newId, kode.uppercase(), nama, lokasi)
        _gedungList.value = _gedungList.value + newGedung
        logMessage("Gedung Baru Ditambahkan: ${nama} (${kode})")
    }

    fun deleteGedung(id: String) {
        val gedung = _gedungList.value.find { it.id == id }
        if (gedung != null) {
            _gedungList.value = _gedungList.value.filter { it.id != id }
            logMessage("Gedung Dihapus: ${gedung.nama}")
        }
    }

    // Manage Ruangan (Master Data)
    fun addRuangan(kode: String, nama: String, gedungId: String, lantai: Int, kapasitas: Int, tipe: String, deskripsi: String) {
        val newId = "R" + (_ruanganList.value.size + 1)
        val newRuangan = Ruangan(
            id = newId,
            kode = kode.uppercase(),
            nama = "${kode.uppercase()} - $nama",
            gedungId = gedungId,
            lantai = lantai,
            kapasitas = kapasitas,
            tipe = tipe,
            deskripsi = deskripsi
        )
        _ruanganList.value = _ruanganList.value + newRuangan
        logMessage("Ruangan Baru Ditambahkan: ${newRuangan.nama}")
    }

    fun deleteRuangan(id: String) {
        val ruangan = _ruanganList.value.find { it.id == id }
        if (ruangan != null) {
            _ruanganList.value = _ruanganList.value.filter { it.id != id }
            logMessage("Ruangan Dihapus: ${ruangan.nama}")
        }
    }

    // Trigger interactive sandbox conflict simulation
    fun triggerSimulasiBentrok() {
        // This will insert a conflicting booking into the list for demonstration
        val nextId = "PEM_SIM"
        val simulasiPeminjaman = Peminjaman(
            id = nextId,
            namaMahasiswa = "Ahmad Dahlan (Teknik)",
            emailMahasiswa = "ahmad@unimus.ac.id",
            ruanganKode = "1A204", // Same room as PEM3 (1A204 - Kantor BAUK) on same date
            ruanganNama = "1A204 - Kantor (BAUK)",
            gedungKode = "REK",
            gedungNama = "Gedung Rektorat UNIMUS",
            lantai = 2,
            kapasitas = 7,
            tanggal = "2026-05-24",
            jamMulai = "14:00",
            jamSelesai = "16:00",
            tujuan = "Rapat Senat Fakultas Teknik",
            status = PeminjamanStatus.MENUNGGU_VERIFIKASI_SIPRUS,
            diajukanPada = "24/5/2026, 08.15.00"
        )

        // Make sure it doesn't already exist
        if (_peminjamanList.value.none { it.id == nextId }) {
            _peminjamanList.value = _peminjamanList.value + simulasiPeminjaman
            logMessage("SIMULASI BENTROK: Menambahkan Rapat Senat pada jam & ruangan sama (1A204). Gunakan opsi Alihkan Ruangan!")
        }
    }
}
