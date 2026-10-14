package com.fabio.eagleeyes.chat

import com.fabio.eagleeyes.BuildConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun interface ChatResponder {
    suspend fun reply(messages: List<Message>): String
}

class ChatException(override val message: String) : Exception(message)

class ChatRepository : ChatResponder {
    private val client = OkHttpClient.Builder().callTimeout(60, TimeUnit.SECONDS).build()

    override suspend fun reply(messages: List<Message>): String {
        if (BuildConfig.OPENROUTER_API_KEY.isBlank()) throw ChatException("A conexão com a IA ainda não foi configurada neste app.")
        val request = ConversationPolicy.request(messages.takeLast(40))
        val body = JSONObject().put("model", request.model).put("messages", JSONArray().apply {
            request.messages.forEach { put(JSONObject().put("role", it.role).put("content", it.content)) }
        })
        val call = client.newCall(Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .header("Authorization", "Bearer ${BuildConfig.OPENROUTER_API_KEY}")
            .header("X-OpenRouter-Title", "Supervisionador Chatbox")
            .post(body.toString().toRequestBody("application/json".toMediaType())).build())
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { call.cancel() }
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isActive) continuation.resumeWithException(ChatException("Não foi possível conectar. Confira sua internet e tente novamente."))
                }
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val content = response.use {
                            if (!it.isSuccessful) throw ChatException(when (it.code) {
                                401, 403 -> "Não foi possível autorizar a conexão com a IA. Confira a configuração do app."
                                429 -> "A IA está recebendo muitas mensagens. Espere um pouco e tente novamente."
                                else -> "A IA não conseguiu responder agora. Tente novamente em instantes."
                            })
                            JSONObject(it.body?.string().orEmpty()).optJSONArray("choices")
                                ?.optJSONObject(0)?.optJSONObject("message")?.optString("content")
                                ?.trim()?.takeIf { text -> text.isNotEmpty() && text != "null" }
                                ?: throw ChatException("A resposta veio vazia. Tente novamente.")
                        }
                        if (continuation.isActive) continuation.resume(content)
                    } catch (error: Exception) {
                        if (continuation.isActive) continuation.resumeWithException(
                            error as? ChatException ?: ChatException("Não foi possível ler a resposta da IA. Tente novamente."))
                    }
                }
            })
        }
    }
}
