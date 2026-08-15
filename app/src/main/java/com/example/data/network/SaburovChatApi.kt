package com.example.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class ApiStatusResponse(
    @Json(name = "status") val status: String? = "ok",
    @Json(name = "version") val version: String? = "1.0",
    @Json(name = "timestamp") val timestamp: Long? = System.currentTimeMillis(),
    @Json(name = "server") val server: String? = "api.saburov.uz",
    @Json(name = "features") val features: List<String>? = listOf("realtime", "websocket", "e2e_encryption", "channels")
)

@JsonClass(generateAdapter = true)
data class ApiSendMessageRequest(
    @Json(name = "chat_id") val chatId: String,
    @Json(name = "sender_id") val senderId: String,
    @Json(name = "sender_name") val senderName: String,
    @Json(name = "text") val text: String,
    @Json(name = "media_type") val mediaType: String? = "TEXT",
    @Json(name = "media_url") val mediaUrl: String? = null,
    @Json(name = "reply_to_id") val replyToId: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiMessageResponse(
    @Json(name = "id") val id: String,
    @Json(name = "chat_id") val chatId: String,
    @Json(name = "sender_id") val senderId: String,
    @Json(name = "sender_name") val senderName: String,
    @Json(name = "text") val text: String,
    @Json(name = "timestamp") val timestamp: Long,
    @Json(name = "media_type") val mediaType: String? = "TEXT",
    @Json(name = "media_url") val mediaUrl: String? = null,
    @Json(name = "status") val status: String? = "SENT"
)

@JsonClass(generateAdapter = true)
data class ApiReactionRequest(
    @Json(name = "message_id") val messageId: String,
    @Json(name = "reaction") val reaction: String,
    @Json(name = "user_id") val userId: String
)

interface SaburovChatApiService {
    @GET("api/v1/status")
    suspend fun getStatus(): ApiStatusResponse

    @GET("api/v1/rooms/{roomId}/messages")
    suspend fun getMessages(
        @Path("roomId") roomId: String,
        @Query("limit") limit: Int = 50,
        @Header("Authorization") token: String? = null
    ): List<ApiMessageResponse>

    @POST("api/v1/messages/send")
    suspend fun sendMessage(
        @Body request: ApiSendMessageRequest,
        @Header("Authorization") token: String? = null
    ): ApiMessageResponse

    @POST("api/v1/messages/react")
    suspend fun sendReaction(
        @Body request: ApiReactionRequest,
        @Header("Authorization") token: String? = null
    ): Map<String, Any>
}

object NetworkClient {
    private var currentBaseUrl = "https://api.saburov.uz/"
    private var currentToken: String = ""

    val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private var retrofitInstance: Retrofit? = null
    private var apiServiceInstance: SaburovChatApiService? = null

    fun getApiService(baseUrl: String = currentBaseUrl): SaburovChatApiService {
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        if (apiServiceInstance == null || currentBaseUrl != normalizedUrl) {
            currentBaseUrl = normalizedUrl
            retrofitInstance = Retrofit.Builder()
                .baseUrl(normalizedUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
            apiServiceInstance = retrofitInstance?.create(SaburovChatApiService::class.java)
        }
        return apiServiceInstance!!
    }
}
