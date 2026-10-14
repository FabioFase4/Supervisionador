package com.fabio.eagleeyes

import com.fabio.eagleeyes.chat.ChatException
import com.fabio.eagleeyes.chat.ChatRepository
import com.fabio.eagleeyes.chat.Message
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class ChatRepositoryTest {
    private fun repository(body: String, code: Int = 200, inspect: (Request) -> Unit = {}) =
        ChatRepository(
            client = OkHttpClient.Builder().addInterceptor { chain ->
                inspect(chain.request())
                Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                    .code(code).message("Test").body(body.toResponseBody()).build()
            }.build(),
            backendUrl = "https://backend.example/chat"
        )

    @Test fun sendsContextUsingTheExistingGeminiBackendContract() = runBlocking {
        var sent: Request? = null
        val repository = repository("""{"resposta":"Podemos conversar sobre isso."}""") { sent = it }
        val answer = repository.reply(listOf(Message("assistant", "Como foi seu dia?"), Message("user", "Foi bom")))
        val request = requireNotNull(sent)
        assertEquals("https://backend.example/chat", request.url.toString())
        assertEquals("POST", request.method)
        assertNull(request.header("Authorization"))
        val body = JSONObject(Buffer().also { request.body!!.writeTo(it) }.readUtf8())
        assertEquals(setOf("mensagem"), body.keys().asSequence().toSet())
        val prompt = body.getString("mensagem")
        assertTrue(prompt.contains("Você é a IA do Chatbox"))
        assertTrue(prompt.contains("Como foi seu dia?"))
        assertTrue(prompt.contains("Foi bom"))
        assertEquals("Podemos conversar sobre isso.", answer)
    }

    @Test fun startsNewConversationAndLimitsOnlyTheAiContext() = runBlocking {
        var prompt = ""
        val repository = repository("Olá") { request ->
            prompt = JSONObject(Buffer().also { request.body!!.writeTo(it) }.readUtf8()).getString("mensagem")
        }
        repository.reply(emptyList())
        assertTrue(prompt.contains("Inicie a conversa agora"))
        repository.reply((0..40).map { Message("user", "fala-$it-fim") })
        assertFalse(prompt.contains("fala-0-fim"))
        assertTrue(prompt.contains("fala-1-fim"))
        assertTrue(prompt.contains("fala-40-fim"))
    }

    @Test fun acceptsTheBackendResponseFormats() = runBlocking {
        for (body in listOf("""{"resposta":"Olá"}""", """{"response":"Olá"}""", """{"message":"Olá"}""", "Olá")) {
            assertEquals("Olá", repository(body).reply(emptyList()))
        }
    }

    @Test fun rejectsServerErrorsInsteadOfSavingThemAsAssistantMessages() = runBlocking {
        for ((body, code) in listOf(
            """{"error":"Falha"}""" to 200,
            """{"status":"ok"}""" to 200,
            """{"resposta":null}""" to 200,
            "<html>Erro</html>" to 200,
            "" to 200,
            "Limite excedido" to 429,
            "Indisponível" to 503
        )) {
            try {
                repository(body, code).reply(emptyList())
                fail("Deveria recusar HTTP $code: $body")
            } catch (_: ChatException) { /* A tela poderá oferecer uma nova tentativa. */ }
        }
    }
}
