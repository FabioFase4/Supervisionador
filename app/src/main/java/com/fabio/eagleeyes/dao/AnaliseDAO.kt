package com.fabio.eagleeyes.dao

import android.util.Log
import com.fabio.eagleeyes.formatadores.MontarJSON
import com.fabio.eagleeyes.global.GeminiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AnaliseDAO {
    private val TAG = "AnaliseDAO"
    private val montarJSON: MontarJSON = MontarJSON()
    private val client = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS) // Aumentado para 90s (Render demora a acordar)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()

    private val geminiApiKey = GeminiConfig.getGeminiApiKey()
    private val geminiBaseUrl = GeminiConfig.getGeminiBaseUrl()
    private val backendUrl = GeminiConfig.getBackendUrl()

    suspend fun analiseTempoTelaFilhoParaPai(prompt: String, usarBackend: Boolean = true): String {
        val url = if (usarBackend) backendUrl else "$geminiBaseUrl?key=$geminiApiKey"

        val bodyJson = if (usarBackend) {
            JSONObject().put("mensagem", prompt).toString()
        } else {
            montarJSON.JsonGemini(prompt)
        }

        return novaRequisicao(url, bodyJson, usarBackend)
    }

    suspend fun analiseTempoTelaParaFilho(prompt: String, usarBackend: Boolean = true): String {
        val url = if (usarBackend) backendUrl else "$geminiBaseUrl?key=$geminiApiKey"

        val bodyJson = if (usarBackend) {
            JSONObject().put("mensagem", prompt).toString()
        } else {
            montarJSON.JsonGemini(prompt)
        }

        return novaRequisicao(url, bodyJson, usarBackend)
    }

    private suspend fun novaRequisicao(url: String, bodyJson: String, usarBackend: Boolean): String {
        val request = Request.Builder()
            .url(url)
            .post(bodyJson.toRequestBody("application/json".toMediaType()))
            .build()

        return chamarApi(request) { body ->
            if (body.isBlank()) return@chamarApi "O servidor retornou uma resposta vazia."

            try {
                // Tenta tratar como JSON
                if (body.trim().startsWith("{")) {
                    val json = JSONObject(body)
                    if (usarBackend) {
                        when {
                            json.has("resposta") -> json.getString("resposta")
                            json.has("response") -> json.getString("response")
                            json.has("message") -> json.getString("message")
                            json.has("error") -> "Erro do Servidor: ${json.optString("error")}"
                            else -> body
                        }
                    } else {
                        extrairTextoGemini(body)
                    }
                } else {
                    // Se não for JSON, mas for texto (e não HTML de erro), retorna o texto
                    if (!body.contains("<!DOCTYPE html>", ignoreCase = true)) {
                        body.trim()
                    } else {
                        "O servidor de IA está com dificuldades técnicas (Erro de Formato)."
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao parsear: ${e.message}")
                "Erro ao processar resposta: ${e.localizedMessage}"
            }
        }
    }

    private fun extrairTextoGemini(jsonBruto: String): String {
        return try {
            val json = JSONObject(jsonBruto)
            if (json.has("candidates")) {
                val candidate = json.getJSONArray("candidates").getJSONObject(0)
                val content = candidate.getJSONObject("content")
                content.getJSONArray("parts").getJSONObject(0).getString("text")
            } else if (json.has("error")) {
                "Erro Gemini: " + json.getJSONObject("error").optString("message", "Erro desconhecido")
            } else {
                "IA não gerou resposta válida."
            }
        } catch (e: Exception) {
            "Erro no processamento da IA: ${e.message}"
        }
    }

    private suspend fun chamarApi(request: Request, parser: (String) -> String): String {
        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string() ?: ""

                    if (!response.isSuccessful) {
                        Log.e("AnaliseDAO", "HTTP ${response.code}: $body")
                        return@use when (response.code) {
                            500 -> "Erro interno no servidor de IA. Tente novamente em instantes."
                            503 -> "O serviço de IA está sobrecarregado. Aguarde um momento."
                            404 -> "Caminho da IA não localizado."
                            else -> "Falha na IA (${response.code})."
                        }
                    }
                    parser(body)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Falha: ${e.message}")
                "Sem conexão com o serviço de IA. Verifique sua rede."
            }
        }
    }
}