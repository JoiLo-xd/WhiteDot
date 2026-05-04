package com.joilol.whitedot.model.persistance

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface WhiteDotAPI {
    @POST("login")
    suspend fun login(@Body credentials: LoginRequest): Response<UserResponse>

    @POST("register")
    suspend fun register(@Body credentials: LoginRequest): Response<RegisterResponse>

    @POST("sync")
    suspend fun syncData(@Body data: UserSyncRequest): Response<SyncResponse>

    @POST("users/{username}/last-logout")
    suspend fun updateLastLogout(
        @retrofit2.http.Path("username") username: String,
        @Body payload: LastLogoutRequest
    ): Response<SyncResponse>

    @retrofit2.http.GET("users/{username}/last-logout")
    suspend fun getLastLogout(
        @retrofit2.http.Path("username") username: String
    ): Response<LastLogoutResponse>
}

// Clases de apoyo para el JSON
data class LoginRequest(val username: String, val password: String)

data class UserResponse(
    val username: String,
    val points: Int,
    val multiplier: Float,
    val last_logout: String?
)

data class RegisterResponse(val message: String)

data class UserSyncRequest(
    val username: String,
    val points: Int,
    val multiplier: Float,
    val last_logout: String
)

data class LastLogoutRequest(val last_logout: String)
data class LastLogoutResponse(val username: String, val last_logout: String)

data class SyncResponse(val status: String)

object RetrofitClient {
    // Para conectar desde el emulador de Android a la máquina local (localhost)
    // se debe usar la IP 10.0.2.2 en lugar de 127.0.0.1
    private const val BASE_URL = "http://10.0.2.2:8000/"

    val api: WhiteDotAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WhiteDotAPI::class.java)
    }
}
