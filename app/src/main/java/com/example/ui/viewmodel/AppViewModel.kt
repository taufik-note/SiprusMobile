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

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://siprus-api.onrender.com/api/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _peminjamanList = MutableStateFlow<List<Peminjaman>>(emptyList())
    val peminjamanList: StateFlow<List<Peminjaman>> = _peminjamanList.asStateFlow()

    private val _gedungList = MutableStateFlow<List<Gedung>>(emptyList())
    val gedungList: StateFlow<List<Gedung>> = _gedungList.asStateFlow()

    private val _ruanganList = MutableStateFlow<List<Ruangan>>(emptyList())
    val ruanganList: StateFlow<List<Ruangan>> = _ruanganList.asStateFlow()

    private val _apiStatusMessage = MutableStateFlow("Terhubung ke Server")
    val apiStatusMessage: StateFlow<String> = _apiStatusMessage.asStateFlow()

    init {
        loadSession()
    }

    private fun loadSession() {
        val userJson = sessionManager.getToken()
        if (userJson.isNotEmpty()) {
            try {
                val user = gson.fromJson(userJson, User::class.java)
                _currentUser.value = user
                refreshDataFromServer()
            } catch (e: Exception) {
                sessionManager.clearSession()
            }
        }
    }

    private fun saveSession(user: User) {
        val userJson = gson.toJson(user)
        sessionManager.saveToken(userJson)
    }

    fun logMessage(msg: String) {
        _apiStatusMessage.value = msg
    }

    fun logout() {
        _currentUser.value = null
        _peminjamanList.value = emptyList()
        sessionManager.clearSession()
        logMessage("Berhasil Logout")
    }

    fun refreshDataFromServer() {
        viewModelScope.launch {
            try {
                _gedungList.value = apiService.getGedung()
                _ruanganList.value = apiService.getRooms()

                val token = _currentUser.value?.token ?: ""
                if (token.isNotEmpty()) {
                    val authHeader = "Bearer $token"
                    if (_currentUser.value?.role == Role.MAHASISWA) {
                        _peminjamanList.value = apiService.getMyHistory(authHeader)
                    } else {
                        _peminjamanList.value = apiService.getAllBookings(authHeader)
                    }
                }
            } catch (e: Exception) {
                logMessage("Error refresh: ${e.message}")
            }
        }
    }

    fun login(email: String, password: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val credentials = mapOf("email" to email, "password" to password)
                val response = apiService.login(credentials)

                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        _currentUser.value = user
                        saveSession(user)
                        logMessage("Login Berhasil: ${user.name}")
                        refreshDataFromServer()
                        onComplete(true, "Selamat datang, ${user.name}!")
                    } else {
                        onComplete(false, "Data user kosong")
                    }
                } else {
                    onComplete(false, "Login gagal: ${response.message()}")
                }
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("Unable to resolve host") == true -> "Tidak ada koneksi internet."
                    e.message?.contains("401") == true -> "Email atau password salah."
                    else -> "Error: ${e.localizedMessage ?: e.message}"
                }
                logMessage("Error Login: $errorMsg")
                onComplete(false, errorMsg)
            }
        }
    }

    fun loginWithDemo(role: Role, onComplete: () -> Unit) {
        val demoUser = User(
            id = 1,
            email = "demo@example.com",
            name = "Demo ${role.name.lowercase().replaceFirstChar { it.uppercase() }}",
            role = role,
            token = "demo-token"
        )
        _currentUser.value = demoUser
        logMessage("Login Demo: ${demoUser.name}")
        refreshDataFromServer()
        onComplete()
    }

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
                val token = _currentUser.value?.token ?: ""
                val body = mapOf(
                    "ruang_id" to ruangId.toString(),
                    "tanggal" to tanggal,
                    "waktu_mulai" to jamMulai,
                    "waktu_selesai" to jamSelesai,
                    "keperluan" to keperluan
                )
                val response = apiService.createBooking("Bearer $token", body)
                if (response.isSuccessful) {
                    refreshDataFromServer()
                    onComplete(true, "Peminjaman berhasil diajukan")
                } else {
                    onComplete(false, "Gagal: ${response.message()}")
                }
            } catch (e: Exception) {
                onComplete(false, "Error: ${e.message}")
            }
        }
    }

    fun validateBooking(
        id: Int,
        status: PeminjamanStatus,
        catatan: String = ""
    ) {
        viewModelScope.launch {
            try {
                val token = _currentUser.value?.token ?: ""
                val body = mapOf(
                    "status" to status.name,
                    "catatan" to catatan
                )
                val response = apiService.validateBooking("Bearer $token", id, body)
                if (response.isSuccessful) {
                    refreshDataFromServer()
                } else {
                    logMessage("Gagal validasi: ${response.message()}")
                }
            } catch (e: Exception) {
                logMessage("Error validasi: ${e.message}")
            }
        }
    }

    fun addGedung(kode: String, nama: String, lokasi: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val token = _currentUser.value?.token ?: ""
                val body = mapOf("kode" to kode, "nama" to nama, "lokasi" to lokasi)
                val response = apiService.addGedung("Bearer $token", body)
                if (response.isSuccessful) {
                    refreshDataFromServer()
                    onResult(true, "Gedung berhasil ditambahkan")
                } else {
                    onResult(false, "Gagal menambahkan gedung")
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    fun addRuangan(kode: String, nama: String, gedungId: Int, lantai: Int, kapasitas: Int, jenis: String, fasilitas: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val token = _currentUser.value?.token ?: ""
                val body = mapOf(
                    "kode" to kode,
                    "nama" to nama,
                    "gedung_id" to gedungId.toString(),
                    "lantai" to lantai.toString(),
                    "kapasitas" to kapasitas.toString(),
                    "jenis" to jenis,
                    "fasilitas" to fasilitas
                )
                val response = apiService.addRuangan("Bearer $token", body)
                if (response.isSuccessful) {
                    refreshDataFromServer()
                    onResult(true, "Ruangan berhasil ditambahkan")
                } else {
                    onResult(false, "Gagal menambahkan ruangan")
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    fun updateProfile(name: String, email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val token = _currentUser.value?.token ?: ""
                val body = mapOf("name" to name, "email" to email)
                val response = apiService.updateProfile("Bearer $token", body)
                if (response.isSuccessful) {
                    // Update local user state
                    val current = _currentUser.value
                    if (current != null) {
                        val updated = current.copy(name = name, email = email)
                        _currentUser.value = updated
                        saveSession(updated)
                    }
                    onResult(true, "Profil berhasil diperbarui")
                } else {
                    onResult(false, "Gagal memperbarui profil")
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    fun updatePassword(current: String, new: String, confirm: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val token = _currentUser.value?.token ?: ""
                val body = mapOf(
                    "current_password" to current,
                    "new_password" to new,
                    "new_password_confirmation" to confirm
                )
                val response = apiService.updatePassword("Bearer $token", body)
                if (response.isSuccessful) {
                    onResult(true, "Password berhasil diperbarui")
                } else {
                    onResult(false, "Gagal: ${response.message()}")
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }
}