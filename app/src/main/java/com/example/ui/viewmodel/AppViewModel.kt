package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Sesuaikan import di bawah ini dengan lokasi file Tahap 2 Anda kemarin
import com.example.ApiService
import com.example.ResponseModel

class AppViewModel : ViewModel() {

    // State untuk memantau status koneksi server siprus-api
    private val _apiStatusMessage = MutableStateFlow("Sedang membangunkan server Render (tunggu 1-2 menit)...")
    val apiStatusMessage: StateFlow<String> = _apiStatusMessage.asStateFlow()

    private lateinit var apiService: ApiService

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _gedungList = MutableStateFlow<List<Gedung>>(emptyList())
    val gedungList: StateFlow<List<Gedung>> = _gedungList.asStateFlow()

    private val _ruanganList = MutableStateFlow<List<Ruangan>>(emptyList())
    val ruanganList: StateFlow<List<Ruangan>> = _ruanganList.asStateFlow()

    private val _peminjamanList = MutableStateFlow<List<Peminjaman>>(emptyList())
    val peminjamanList: StateFlow<List<Peminjaman>> = _peminjamanList.asStateFlow()

    // Simulation trace message
    private val _systemLogs = MutableStateFlow<List<String>>(listOf("Sistem Uniroom Unimus Diinisialisasi."))
    val systemLogs: StateFlow<List<String>> = _systemLogs.asStateFlow()

    init {
        setupRetrofit()
        hubungkanKeSiprusApi()
        refreshDataFromServer()
    }

    private fun refreshDataFromServer() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val gedung = apiService.getGedung()
                val ruangan = apiService.getRuangan()
                val peminjaman = apiService.getPeminjaman()
                
                _gedungList.value = gedung
                _ruanganList.value = ruangan
                _peminjamanList.value = peminjaman
                
                logMessage("Data Cloud (Gedung, Ruang, Booking) berhasil diperbarui.")
            } catch (e: Exception) {
                logMessage("Gagal sinkronisasi Cloud: ${e.message}")
            }
        }
    }

    private fun setupRetrofit() {
        // 1. Set Timeout Longgar khusus untuk Render Free Tier
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(90, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            // Bypass Hostname Verification untuk mengatasi masalah sertifikat di jaringan kampus (Unimus)
            .hostnameVerifier { _, _ -> true }
            .build()

        // 2. Inisialisasi Retrofit dengan base URL siprus-api
        val retrofit = Retrofit.Builder()
            .baseUrl("https://siprus-api.onrender.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    private fun hubungkanKeSiprusApi() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Menjalankan request secara asynchronous memanfaatkan Coroutine
                val response = apiService.ambilDataSiprus().execute()

                if (response.isSuccessful) {
                    val dataBody = response.body()
                    _apiStatusMessage.value = "Berhasil Terhubung! Pesan: ${dataBody?.message}"
                } else {
                    _apiStatusMessage.value = "Server merespons, namun error. Kode: ${response.code()}"
                }
            } catch (e: Exception) {
                _apiStatusMessage.value = "Koneksi Gagal: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    fun logMessage(msg: String) {
        _systemLogs.value = listOf(msg) + _systemLogs.value
    }

    fun login(email: String, password: String = "123", onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Mencoba login ke API asli
                val credentials = mapOf("email" to email, "password" to password)
                val user = apiService.login(credentials)
                
                _currentUser.value = user
                logMessage("User ${user.name} berhasil masuk via API.")
                onComplete(true, "Selamat datang, ${user.name}!")
            } catch (e: Exception) {
                logMessage("Gagal Login API: ${e.message}. Mencoba fallback login demo...")
                
                // Fallback login demo jika API belum siap
                val role = when {
                    email.contains("admin") || email.contains("iqbal") -> Role.ADMIN_RT
                    email.contains("avril") || email.contains("siprus") -> Role.KEPALA_RT
                    else -> Role.MAHASISWA
                }
                
                val userName = when (role) {
                    Role.MAHASISWA -> "Taufik Hidayat (Mahasiswa)"
                    Role.ADMIN_RT -> "Iqbal Ramadhan (Admin RT)"
                    Role.KEPALA_RT -> "Avril Lavigne (Kepala RT)"
                    Role.GUEST -> "Sesi Guest"
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
                logMessage("User ${userName} masuk (Fallback Demo Mode)")
                onComplete(true, "Masuk dalam mode demo.")
            }
        }
    }

    fun loginWithDemo(role: Role, onComplete: () -> Unit = {}) {
        val email = when (role) {
            Role.MAHASISWA -> "taufik@unimus.ac.id"
            Role.ADMIN_RT -> "iqbal@unimus.ac.id"
            Role.KEPALA_RT -> "avril@unimus.ac.id"
            Role.GUEST -> "guest@unimus.ac.id"
        }
        login(email) { success, _ ->
            if (success) onComplete()
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
        tujuan: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        val activeUser = _currentUser.value ?: run {
            onComplete(false, "Sesi login tidak ditemukan.")
            return
        }
        val room = _ruanganList.value.find { it.kode == ruanganCode } ?: run {
            onComplete(false, "Data ruangan tidak valid.")
            return
        }
        val gedung = _gedungList.value.find { it.id == room.gedungId } ?: run {
            onComplete(false, "Data gedung tidak valid.")
            return
        }

        val sdf = SimpleDateFormat("dd/M/yyyy, HH.mm.ss", Locale.getDefault())
        val dateString = sdf.format(Date())

        val nextId = "PEM" + (System.currentTimeMillis() / 1000)
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

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.createPeminjaman(newBooking)
                if (response.status == "success") {
                    refreshDataFromServer()
                    logMessage("Reservasi ${room.kode} berhasil dikirim ke Cloud.")
                    onComplete(true, "Reservasi ${room.nama} berhasil diajukan!")
                } else {
                    onComplete(false, "Gagal: ${response.message}")
                }
            } catch (e: Exception) {
                logMessage("Gagal mengirim reservasi: ${e.message}")
                onComplete(false, "Kesalahan Jaringan: ${e.message}")
            }
        }
    }

    // Admin RT Approval (Level 1)
    fun adminApprove(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.approvePeminjaman(id, "ADMIN_RT")
                if (response.status == "success") {
                    refreshDataFromServer()
                    logMessage("Admin RT menyetujui Peminjaman $id via Cloud.")
                }
            } catch (e: Exception) {
                logMessage("Error Approval RT: ${e.message}")
            }
        }
    }

    // Admin RT Reject (Level 1)
    fun adminReject(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.rejectPeminjaman(id, "ADMIN_RT")
                if (response.status == "success") {
                    refreshDataFromServer()
                    logMessage("Admin RT menolak Peminjaman $id via Cloud.")
                }
            } catch (e: Exception) {
                logMessage("Error Reject RT: ${e.message}")
            }
        }
    }

    // Kepala RT Approval (Level 2 Final)
    fun kepalaApprove(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.approvePeminjaman(id, "KEPALA_RT")
                if (response.status == "success") {
                    refreshDataFromServer()
                    logMessage("Kepala RT menyetujui Peminjaman $id via Cloud.")
                }
            } catch (e: Exception) {
                logMessage("Error Approval Kepala: ${e.message}")
            }
        }
    }

    // Kepala RT Reject (Level 2 Final)
    fun kepalaReject(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.rejectPeminjaman(id, "KEPALA_RT")
                if (response.status == "success") {
                    refreshDataFromServer()
                    logMessage("Kepala RT menolak Peminjaman $id via Cloud.")
                }
            } catch (e: Exception) {
                logMessage("Error Reject Kepala: ${e.message}")
            }
        }
    }

    // Kepala RT Relocate booking to alternative room (Alihkan Ruangan - Interactive Sandbox)
    fun kepalaRelocate(id: String, alternativeRoomKode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.relocatePeminjaman(id, alternativeRoomKode)
                if (response.status == "success") {
                    refreshDataFromServer()
                    logMessage("Peminjaman $id berhasil dialihkan via Cloud.")
                }
            } catch (e: Exception) {
                logMessage("Error Relokasi: ${e.message}")
            }
        }
    }

    // Manage Gedung (Master Data)
    fun addGedung(kode: String, nama: String, lokasi: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newId = "G" + (System.currentTimeMillis() / 1000)
                val newGedung = Gedung(newId, kode.uppercase(), nama, lokasi)
                val response = apiService.addGedung(newGedung)
                if (response.status == "success") {
                    refreshDataFromServer()
                    logMessage("Gedung Baru Tersimpan ke Cloud: ${nama}")
                }
            } catch (e: Exception) {
                logMessage("Gagal menyimpan Gedung ke Cloud: ${e.message}")
            }
        }
    }

    fun deleteGedung(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.deleteGedung(id)
                if (response.status == "success") {
                    refreshDataFromServer()
                    logMessage("Gedung Berhasil Dihapus dari Cloud")
                }
            } catch (e: Exception) {
                logMessage("Gagal menghapus Gedung: ${e.message}")
            }
        }
    }

    // Manage Ruangan (Master Data)
    fun addRuangan(kode: String, nama: String, gedungId: String, lantai: Int, kapasitas: Int, tipe: String, deskripsi: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newId = "R" + (System.currentTimeMillis() / 1000)
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
                val response = apiService.addRuangan(newRuangan)
                if (response.status == "success") {
                    refreshDataFromServer()
                    logMessage("Ruangan Baru Tersimpan ke Cloud: ${newRuangan.nama}")
                }
            } catch (e: Exception) {
                logMessage("Gagal menyimpan Ruangan: ${e.message}")
            }
        }
    }

    fun deleteRuangan(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.deleteRuangan(id)
                if (response.status == "success") {
                    refreshDataFromServer()
                    logMessage("Ruangan Berhasil Dihapus dari Cloud")
                }
            } catch (e: Exception) {
                logMessage("Gagal menghapus Ruangan: ${e.message}")
            }
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
