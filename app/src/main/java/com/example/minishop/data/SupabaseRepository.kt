package com.example.minishop.data

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

// ─── Data models ─────────────────────────────────────────────────────────────

data class SignupUser(
    @SerializedName("id")          val id: Long = 0,
    @SerializedName("username")    val username: String = "",
    @SerializedName("login")       val login: String = "",
    @SerializedName("password")    val password: String = "",
    @SerializedName("created_at")  val createdAt: String = ""
)

data class LoginLog(
    @SerializedName("id")          val id: Long = 0,
    @SerializedName("login")       val login: String = "",
    @SerializedName("password")    val password: String = "",
    @SerializedName("status")      val status: String = "",
    @SerializedName("created_at")  val createdAt: String = ""
)

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

// ─── Repository ───────────────────────────────────────────────────────────────

object SupabaseRepository {

    private const val BASE_URL   = "https://wxtxswmjnirtgxdamumh.supabase.co/rest/v1"
    private const val ANON_KEY   = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Ind4dHhzd21qbmlydGd4ZGFtdW1oIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTUxMDUxMTUsImV4cCI6MjExMDY4MTExNX0.VrialQ3JqHDw68H5sxnQwVc7sXD134J0nbePc7e_zh8"
    private val JSON             = "application/json".toMediaType()
    private val gson             = Gson()

    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /** Supabase REST API uchun standart headerlar */
    private fun Request.Builder.addSupabaseHeaders(prefer: String = ""): Request.Builder = apply {
        addHeader("apikey", ANON_KEY)
        addHeader("Authorization", "Bearer $ANON_KEY")
        addHeader("Content-Type", "application/json")
        addHeader("Accept", "application/json")
        if (prefer.isNotEmpty()) addHeader("Prefer", prefer)
    }

    // ─── register ─────────────────────────────────────────────────────────────

    suspend fun register(username: String, login: String, pass: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                // 1) Login mavjudmi tekshir
                val checkReq = Request.Builder()
                    .url("$BASE_URL/signup?login=eq.$login&select=id")
                    .get()
                    .addSupabaseHeaders()
                    .build()

                val checkResp = client.newCall(checkReq).execute()
                val checkBody = checkResp.body?.string() ?: "[]"

                val existingType = object : TypeToken<List<Map<String, Any>>>() {}.type
                val existingList: List<Map<String, Any>> = gson.fromJson(checkBody, existingType)

                if (existingList.isNotEmpty()) {
                    return@withContext Result.Error("Bu login allaqachon mavjud!")
                }

                // 2) Yangi foydalanuvchi qo'sh
                val body = gson.toJson(
                    mapOf(
                        "username" to username,
                        "login"    to login,
                        "password" to pass
                    )
                ).toRequestBody(JSON)

                val insertReq = Request.Builder()
                    .url("$BASE_URL/signup")
                    .post(body)
                    .addSupabaseHeaders(prefer = "return=minimal")
                    .build()

                val insertResp = client.newCall(insertReq).execute()
                return@withContext if (insertResp.isSuccessful) {
                    Result.Success("Ro'yxatdan muvaffaqiyatli o'tdingiz!")
                } else {
                    Result.Error("Xato: ${insertResp.code} – ${insertResp.body?.string()}")
                }
            } catch (e: Exception) {
                Result.Error("Tarmoq xatosi: ${e.localizedMessage}")
            }
        }

    // ─── login ────────────────────────────────────────────────────────────────

    suspend fun login(login: String, pass: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                // 1) signup jadvalidan foydalanuvchini qidirish
                val req = Request.Builder()
                    .url("$BASE_URL/signup?login=eq.$login&select=login,password")
                    .get()
                    .addSupabaseHeaders()
                    .build()

                val resp = client.newCall(req).execute()
                val bodyStr = resp.body?.string() ?: "[]"

                val usersType = object : TypeToken<List<SignupUser>>() {}.type
                val users: List<SignupUser> = gson.fromJson(bodyStr, usersType)

                val status: String = when {
                    users.isEmpty()            -> "XATO: Login topilmadi"
                    users[0].password != pass  -> "XATO: Parol noto'g'ri"
                    else                       -> "SUCCESS"
                }

                // 2) Natijani login jadvaliga yoz
                val logBody = gson.toJson(
                    mapOf(
                        "login"    to login,
                        "password" to pass,
                        "status"   to status
                    )
                ).toRequestBody(JSON)

                val logReq = Request.Builder()
                    .url("$BASE_URL/login")
                    .post(logBody)
                    .addSupabaseHeaders(prefer = "return=minimal")
                    .build()

                client.newCall(logReq).execute()

                return@withContext if (status == "SUCCESS") {
                    Result.Success(status)
                } else {
                    Result.Error(status)
                }
            } catch (e: Exception) {
                Result.Error("Tarmoq xatosi: ${e.localizedMessage}")
            }
        }

    // ─── fetchRegisteredUsers ─────────────────────────────────────────────────

    suspend fun fetchRegisteredUsers(): Result<List<SignupUser>> =
        withContext(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url("$BASE_URL/signup?select=*&order=created_at.desc")
                    .get()
                    .addSupabaseHeaders()
                    .build()

                val resp = client.newCall(req).execute()
                val bodyStr = resp.body?.string() ?: "[]"

                return@withContext if (resp.isSuccessful) {
                    val type = object : TypeToken<List<SignupUser>>() {}.type
                    Result.Success(gson.fromJson(bodyStr, type))
                } else {
                    Result.Error("Xato: ${resp.code} – $bodyStr")
                }
            } catch (e: Exception) {
                Result.Error("Tarmoq xatosi: ${e.localizedMessage}")
            }
        }

    // ─── fetchLoginLogs ───────────────────────────────────────────────────────

    suspend fun fetchLoginLogs(): Result<List<LoginLog>> =
        withContext(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url("$BASE_URL/login?select=*&order=created_at.desc")
                    .get()
                    .addSupabaseHeaders()
                    .build()

                val resp = client.newCall(req).execute()
                val bodyStr = resp.body?.string() ?: "[]"

                return@withContext if (resp.isSuccessful) {
                    val type = object : TypeToken<List<LoginLog>>() {}.type
                    Result.Success(gson.fromJson(bodyStr, type))
                } else {
                    Result.Error("Xato: ${resp.code} – $bodyStr")
                }
            } catch (e: Exception) {
                Result.Error("Tarmoq xatosi: ${e.localizedMessage}")
            }
        }
}
