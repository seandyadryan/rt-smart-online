package com.deploydulupulangnanti.rtsmartpro

import android.app.Application
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

fun JSONObject.s(key: String) = if (isNull(key)) "" else optString(key, "")
fun JSONArray.objects() = (0 until length()).map { getJSONObject(it) }

class TokenVault(context: Context) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return (store.getKey("rt_session", null) as? SecretKey) ?: KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder("rt_session", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }
    fun read(): String = runCatching {
        val raw = Base64.decode(prefs.getString("token", null) ?: return "", Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, raw.copyOfRange(0, 12))) }
        String(cipher.doFinal(raw.copyOfRange(12, raw.size)))
    }.getOrDefault("")
    fun save(token: String) {
        if (token.isEmpty()) { prefs.edit().clear().apply(); return }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
        prefs.edit().putString("token", Base64.encodeToString(cipher.iv + cipher.doFinal(token.toByteArray()), Base64.NO_WRAP)).apply()
    }
}

class ApiError(val status: Int, override val message: String): Exception(message)

class AppModel(application: Application): AndroidViewModel(application) {
    private val vault = TokenVault(application)
    private var token = vault.read()
    var me by mutableStateOf<JSONObject?>(null); private set
    var dashboard by mutableStateOf(JSONObject()); private set
    var invoices by mutableStateOf(emptyList<JSONObject>()); private set
    var news by mutableStateOf(emptyList<JSONObject>()); private set
    var residents by mutableStateOf(emptyList<JSONObject>()); private set
    var reports by mutableStateOf(emptyList<JSONObject>()); private set
    var finance by mutableStateOf(JSONObject()); private set
    var busy by mutableStateOf(false); private set
    var loading by mutableStateOf(token.isNotEmpty()); private set
    var message by mutableStateOf<String?>(null)
    var notice by mutableStateOf<String?>(null)
    var otpEnabled by mutableStateOf(false); private set
    val isAdmin get() = me?.s("role") == "admin"
    init {
        viewModelScope.launch {
            runCatching { JSONObject(api("config")).optBoolean("otp_enabled") }.onSuccess { otpEnabled = it }
            if (token.isNotEmpty()) reload()
            loading = false
        }
    }
    private suspend fun api(path: String, method: String = "GET", body: JSONObject? = null): String = withContext(Dispatchers.IO) {
        val connection = URL(BuildConfig.API_BASE_URL + path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 15000; connection.readTimeout = 25000
            connection.setRequestProperty("Accept", "application/json")
            if (token.isNotEmpty()) connection.setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                connection.doOutput = true; connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
            val status = connection.responseCode
            val raw = (if (status in 200..299) connection.inputStream else connection.errorStream)?.bufferedReader()?.use { it.readText() } ?: "{}"
            if (status !in 200..299) {
                val detail = runCatching { JSONObject(raw).opt("detail") }.getOrNull()
                throw ApiError(status, if (detail is String) detail else "Data belum sesuai. Periksa isian Anda.")
            }
            raw
        } finally { connection.disconnect() }
    }
    private suspend fun reload() {
        try {
            me = JSONObject(api("me"))
            dashboard = JSONObject(api("dashboard"))
            invoices = JSONArray(api("invoices")).objects()
            news = JSONArray(api("news")).objects()
            finance = JSONObject(api("finance"))
            reports = JSONArray(api("reports")).objects()
            if (isAdmin) residents = JSONArray(api("residents")).objects()
        } catch (e: Exception) {
            if (e is ApiError && e.status == 401) { clear() }
            message = if (e is ApiError) e.message else "Koneksi belum tersedia. Periksa internet lalu coba lagi."
        }
    }
    fun refresh() { if (busy) return; viewModelScope.launch { busy = true; reload(); busy = false } }
    fun login(phone: String, code: String, otp: Boolean) {
        if (busy) return
        viewModelScope.launch {
            busy = true
            try {
                val response = JSONObject(api(if (otp) "auth/otp/verify" else "auth/pin", "POST", JSONObject().put("phone", phone).put("code", code)))
                token = response.getString("token"); vault.save(token); reload()
            } catch (e: Exception) { message = if (e is ApiError) e.message else "Tidak dapat menghubungi server. Coba lagi." }
            busy = false
        }
    }
    fun requestOtp(phone: String, onSent: () -> Unit) {
        if (busy) return
        viewModelScope.launch {
            busy = true
            try { api("auth/otp/request", "POST", JSONObject().put("phone", phone)); notice = "Jika nomor terdaftar, OTP akan dikirim."; onSent() }
            catch (e: Exception) { message = if (e is ApiError) e.message else "OTP belum dapat dikirim." }
            busy = false
        }
    }
    fun mutate(path: String, data: JSONObject = JSONObject(), method: String = "POST", after: (() -> Unit)? = null) {
        if (busy) return
        viewModelScope.launch {
            busy = true
            try {
                val result = JSONObject(api(path, method, data))
                if (result.has("initial_pin")) notice = "PIN warga: ${result.s("initial_pin")}\nSampaikan secara pribadi. PIN hanya ditampilkan sekali."
                else notice = result.optString("message", "Perubahan berhasil disimpan.")
                if (path == "me/pin") clear() else reload()
                after?.invoke()
            } catch (e: Exception) { message = if (e is ApiError) e.message else "Perubahan belum tersimpan. Periksa koneksi Anda." }
            busy = false
        }
    }
    private fun clear() { token = ""; vault.save(""); me = null; invoices = emptyList(); news = emptyList(); residents = emptyList(); reports = emptyList(); finance = JSONObject(); dashboard = JSONObject() }
    fun logout() {
        viewModelScope.launch { busy = true; try { api("auth/logout", "POST", JSONObject()); clear() } catch (_:Exception) { message = "Gagal keluar dari server. Coba lagi saat terhubung." }; busy = false }
    }
}
