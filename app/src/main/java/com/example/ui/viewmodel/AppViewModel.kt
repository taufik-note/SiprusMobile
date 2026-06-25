package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.api.ApiService
import com.example.utils.SessionManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val gson = Gson()

    // ── Network ──────────────────────────────────────────────────────────────

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val apiService: ApiService = Retrofit.Builder()
        .baseUrl("https://siprus-api.onrender.com/api/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    // ── State ─────────────────────────────────────────────────────────────────

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _peminjamanList = MutableStateFlow<List<Peminjaman>>(emptyList())
    val peminjamanList: StateFlow<List<Peminjaman>> = _peminjamanList.asStateFlow()

    private val _gedungList = MutableStateFlow<List<Gedung>>(emptyList())
    val gedungList: StateFlow<List<Gedung>> = _gedungList.asStateFlow()

    private val _ruanganList = MutableStateFlow<List<Ruangan>>(emptyList())
    val ruanganList: StateFlow<List<Ruangan>> = _ruanganList.asStateFlow()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _weeklyAvailability = MutableStateFlow<Map<String, List<Ruangan>>>(emptyMap())
    val weeklyAvailability: StateFlow<Map<String, List<Ruangan>>> = _weeklyAvailability.asStateFlow()

    private val _isLoadingWeekly = MutableStateFlow(false)
    val isLoadingWeekly: StateFlow<Boolean> = _isLoadingWeekly.asStateFlow()

    /**
     * Pesan status terakhir untuk ditampilkan di UI (snackbar, banner, dsb).
     * Kosong berarti tidak ada pesan aktif.
     */
    private val _apiStatusMessage = MutableStateFlow("")
    val apiStatusMessage: StateFlow<String> = _apiStatusMessage.asStateFlow()

    /** True selama refreshDataFromServer() berjalan — gunakan untuk spinner. */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadSession()
    }

    // ── Session ───────────────────────────────────────────────────────────────

    /**
     * Muat sesi tersimpan. Token yang tersimpan adalah JSON dari objek [User].
     * Jika parsing gagal (misal struktur User berubah), sesi dihapus agar tidak
     * menyebabkan crash berulang.
     */
    private fun loadSession() {
        // getToken() bisa mengembalikan null — tangani dengan aman
        val userJson = sessionManager.getToken()?.takeIf { it.isNotBlank() } ?: return
        try {
            val user = gson.fromJson(userJson, User::class.java)
            if (user?.token.isNullOrBlank()) {
                // Data tersimpan tidak valid / tidak lengkap
                sessionManager.clearSession()
                return
            }
            _currentUser.value = user
            refreshDataFromServer()
        } catch (e: Exception) {
            // JSON korup atau struktur User berubah → bersihkan sesi
            sessionManager.clearSession()
            logMessage("Sesi lama tidak valid, silakan login kembali.")
        }
    }

    private fun saveSession(user: User) {
        sessionManager.saveToken(gson.toJson(user))
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    fun logMessage(msg: String) {
        _apiStatusMessage.value = msg
    }

    /** Panggil dari UI setelah pesan ditampilkan supaya tidak muncul lagi. */
    fun clearStatusMessage() {
        _apiStatusMessage.value = ""
    }

    private fun bearerToken(): String {
        val token = _currentUser.value?.token ?: ""
        return "Bearer $token"
    }

    // ── Auth ──────────────────────────────────────────────────────────────────

    fun logout() {
        _currentUser.value = null
        _peminjamanList.value = emptyList()
        _gedungList.value = emptyList()
        _ruanganList.value = emptyList()
        sessionManager.clearSession()
        logMessage("Berhasil logout.")
    }

    fun login(email: String, password: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.login(mapOf("email" to email, "password" to password))

                if (response.isSuccessful) {
                    val loginData = response.body()
                    if (loginData != null) {
                        val user = loginData.user
                        user.token = loginData.token // Simpan token ke dalam objek User

                        _currentUser.value = user
                        saveSession(user)
                        logMessage("Terhubung: ${user.name}")
                        refreshDataFromServer()
                        onComplete(true, "Selamat datang, ${user.name}!")
                    } else {
                        onComplete(false, "Respons server tidak berisi data user yang valid.")
                    }
                } else {
                    val errorMsg = parseErrorMessage(response)
                    val finalMsg = when (response.code()) {
                        401  -> "Email atau password salah."
                        422  -> "Format input tidak valid: $errorMsg"
                        500  -> "Server Error — database mungkin tidak terhubung."
                        503  -> "Server sedang maintenance / DB down."
                        else -> errorMsg
                    }
                    onComplete(false, finalMsg)
                }
            } catch (e: java.net.SocketTimeoutException) {
                // Render free-tier cold start bisa melebihi 60 detik
                val msg = "Waktu koneksi habis (timeout). " +
                        "Server mungkin sedang cold-start, coba lagi dalam beberapa detik."
                logMessage(msg)
                onComplete(false, msg)
            } catch (e: java.net.UnknownHostException) {
                val msg = "Tidak dapat menemukan server. Periksa koneksi internet atau URL API."
                logMessage(msg)
                onComplete(false, msg)
            } catch (e: java.io.IOException) {
                val msg = "Koneksi ke server gagal: ${e.localizedMessage}"
                logMessage(msg)
                onComplete(false, msg)
            } catch (e: Exception) {
                val msg = "Error tidak terduga: ${e.localizedMessage ?: e.message}"
                logMessage(msg)
                onComplete(false, msg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loginWithDemo(role: Role, onComplete: () -> Unit) {
        val demoUser = User(
            id    = 0,
            email = "demo@example.com",
            name  = "Demo ${role.name.lowercase().replaceFirstChar { it.uppercase() }}",
            role  = role,
            token = "demo-token"
        )
        _currentUser.value = demoUser
        logMessage("Login demo: ${demoUser.name} (tidak ada sesi tersimpan).")
        refreshDataFromServer()
        onComplete()
    }

    // ── Data refresh ──────────────────────────────────────────────────────────

    /**
     * Muat ulang data dari server.
     *
     * Setiap endpoint ditangani secara terpisah sehingga kegagalan satu endpoint
     * tidak menghentikan endpoint lainnya.
     */
    fun refreshDataFromServer() {
        val token = _currentUser.value?.token
        if (token.isNullOrBlank() || token == "demo-token") {
            // Belum login atau mode demo — tidak ada yang bisa dimuat
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val auth = "Bearer $token"

            // Semua endpoint butuh Authorization — kirim token ke setiap call
            try {
                _gedungList.value = apiService.getGedung(auth)
            } catch (e: Exception) {
                logMessage("Gagal memuat data gedung: ${friendlyError(e)}")
            }

            try {
                val rooms = apiService.getRooms(auth)
                _ruanganList.value = rooms
            } catch (e: Exception) {
                logMessage("Gagal memuat data ruangan: ${friendlyError(e)}")
            }

            try {
                _peminjamanList.value = when (_currentUser.value?.role) {
                    Role.MAHASISWA -> apiService.getMyHistory(auth)
                    else           -> apiService.getAllBookings(auth)
                }
            } catch (e: Exception) {
                logMessage("Gagal memuat data peminjaman: ${friendlyError(e)}")
            }

            // 4. Notifikasi
            try {
                _notifications.value = apiService.getNotifications(auth)
            } catch (e: Exception) {
                // Notifikasi gagal tidak kritikal
            }

            _isLoading.value = false
        }
    }

    // ── Booking ───────────────────────────────────────────────────────────────

    fun createBooking(
        ruangId: Int,
        tanggal: String,
        jamMulai: String,
        jamSelesai: String,
        keperluan: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val request = BookingRequest(
                    ruangId = ruangId,
                    tanggal = tanggal,
                    waktuMulai = jamMulai,
                    waktuSelesai = jamSelesai,
                    keperluan = keperluan
                )
                val response = apiService.createBooking(bearerToken(), request)
                if (response.isSuccessful) {
                    refreshDataFromServer()
                    onComplete(true, "Peminjaman berhasil diajukan.")
                } else {
                    onComplete(false, "Gagal mengajukan peminjaman: ${parseErrorMessage(response)}")
                }
            } catch (e: Exception) {
                onComplete(false, "Error: ${friendlyError(e)}")
            }
        }
    }

    fun validateBooking(id: Int, status: PeminjamanStatus, catatan: String = "") {
        viewModelScope.launch {
            try {
                val actionValue = when (status) {
                    PeminjamanStatus.DISETUJUI -> "setuju"
                    PeminjamanStatus.DITOLAK_RT, PeminjamanStatus.DITOLAK_KEPALA -> "tolak"
                    PeminjamanStatus.BUTUH_REVISI -> "revisi"
                    else -> "setuju"
                }

                val request = ValidateRequest(
                    action = actionValue,
                    alasan = catatan
                )
                
                val response = apiService.validateBooking(bearerToken(), id, request)
                if (response.isSuccessful) {
                    refreshDataFromServer()
                } else {
                    logMessage("Gagal validasi: ${parseErrorMessage(response)}")
                }
            } catch (e: Exception) {
                logMessage("Error validasi: ${friendlyError(e)}")
            }
        }
    }

    // ── Admin: Gedung / Ruangan ───────────────────────────────────────────────

    fun addGedung(kode: String, nama: String, lokasi: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val body = mapOf("kode" to kode, "nama" to nama, "lokasi" to lokasi)
                val response = apiService.addGedung(bearerToken(), body)
                if (response.isSuccessful) {
                    refreshDataFromServer()
                    onResult(true, "Gedung berhasil ditambahkan.")
                } else {
                    onResult(false, "Gagal menambahkan gedung: ${parseErrorMessage(response)}")
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${friendlyError(e)}")
            }
        }
    }

    fun addRuangan(
        kode: String, nama: String, gedungId: Int,
        lantai: Int, kapasitas: Int, jenis: String, fasilitas: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Kirim field numerik sebagai Int, bukan String
                val body = mapOf<String, Any>(
                    "kode"      to kode,
                    "nama"      to nama,
                    "gedungId"  to gedungId,       // Int
                    "lantai"    to lantai,          // Int
                    "kapasitas" to kapasitas,       // Int
                    "jenis"     to jenis,
                    "fasilitas" to fasilitas
                )
                val response = apiService.addRuangan(bearerToken(), body)
                if (response.isSuccessful) {
                    refreshDataFromServer()
                    onResult(true, "Ruangan berhasil ditambahkan.")
                } else {
                    onResult(false, "Gagal menambahkan ruangan: ${parseErrorMessage(response)}")
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${friendlyError(e)}")
            }
        }
    }

    // ── Profil ────────────────────────────────────────────────────────────────

    fun updateProfile(name: String, email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.updateProfile(bearerToken(), mapOf("name" to name, "email" to email))
                if (response.isSuccessful) {
                    val updatedUser = response.body()?.user
                    if (updatedUser != null) {
                        // Pertahankan token lama agar sesi tidak putus
                        val userWithToken = updatedUser.copy(token = _currentUser.value?.token)
                        _currentUser.value = userWithToken
                        saveSession(userWithToken)
                        onResult(true, "Profil berhasil diperbarui.")
                    } else {
                        onResult(false, "Profil diperbarui tapi data gagal dimuat.")
                    }
                } else {
                    onResult(false, "Gagal memperbarui profil: ${parseErrorMessage(response)}")
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${friendlyError(e)}")
            }
        }
    }

    fun updatePassword(current: String, new: String, confirm: String, onResult: (Boolean, String) -> Unit) {
        if (new != confirm) {
            onResult(false, "Konfirmasi password tidak cocok.")
            return
        }
        viewModelScope.launch {
            try {
                val body = mapOf(
                    "oldPassword" to current, // Sesuai Backend: oldPassword
                    "newPassword" to new     // Sesuai Backend: newPassword
                )
                val response = apiService.updatePassword(bearerToken(), body)
                if (response.isSuccessful) {
                    onResult(true, "Password berhasil diperbarui.")
                } else {
                    onResult(false, "Gagal: ${parseErrorMessage(response)}")
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${friendlyError(e)}")
            }
        }
    }

    // ── Notifications ─────────────────────────────────────────────────────────

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            try {
                val response = apiService.markAllNotificationsAsRead(bearerToken())
                if (response.isSuccessful) {
                    refreshDataFromServer()
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun fetchWeeklyAvailability(
        startDate: String,
        waktuMulai: String,
        waktuSelesai: String,
        gedungId: String?,
        kapasitas: String?
    ) {
        viewModelScope.launch {
            _isLoadingWeekly.value = true
            val results = mutableMapOf<String, List<Ruangan>>()
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val calendar = java.util.Calendar.getInstance()
            
            try {
                val start = sdf.parse(startDate) ?: java.util.Date()
                val token = bearerToken()

                // Ambil data untuk 7 hari ke depan
                for (i in 0 until 7) {
                    calendar.time = start
                    calendar.add(java.util.Calendar.DAY_OF_YEAR, i)
                    val currentDateStr = sdf.format(calendar.time)
                    
                    try {
                        val available = apiService.getAvailableRooms(
                            token, currentDateStr, waktuMulai, waktuSelesai, kapasitas, gedungId
                        )
                        results[currentDateStr] = available
                    } catch (e: Exception) {
                        results[currentDateStr] = emptyList()
                    }
                }
                _weeklyAvailability.value = results
            } catch (e: Exception) {
                logMessage("Gagal memuat ketersediaan sepekan")
            } finally {
                _isLoadingWeekly.value = false
            }
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Parse pesan error dari body respons HTTP gagal. */
    private fun parseErrorMessage(response: retrofit2.Response<*>): String {
        return try {
            val errorJson = response.errorBody()?.string()
            // Parse manual — tidak bergantung pada class ResponseModel tertentu
            val jsonObj = gson.fromJson(errorJson, com.google.gson.JsonObject::class.java)
            jsonObj?.get("message")?.asString
                ?: "Error ${response.code()}: ${response.message()}"
        } catch (e: Exception) {
            "Error ${response.code()}: ${response.message()}"
        }
    }

    /** Konversi exception ke pesan yang ramah pengguna. */
    private fun friendlyError(e: Exception): String = when (e) {
        is java.net.SocketTimeoutException -> "Timeout — server mungkin sedang cold-start, coba lagi."
        is java.net.UnknownHostException   -> "Tidak dapat menjangkau server. Periksa koneksi internet."
        is java.io.IOException             -> "Masalah jaringan: ${e.localizedMessage}"
        else                               -> e.localizedMessage ?: e.message ?: "Error tidak diketahui"
    }
}