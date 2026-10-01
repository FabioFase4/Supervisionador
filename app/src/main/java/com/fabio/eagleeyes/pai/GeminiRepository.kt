package com.fabio.eagleeyes.pai

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.fabio.eagleeyes.global.GeminiConfig
import android.util.Log

class GeminiRepository {

    private val apiKey = GeminiConfig.getGeminiApiKey()
    
    // Tentativa de usar o modelo flash. 
    // Se a sua chave for antiga, talvez precise de "gemini-pro"
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    suspend fun analisarComportamento(pergunta: String, dadosUso: String): String = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Você é um especialista em mediação familiar e segurança digital do aplicativo EagleEyes.
                Dados de uso do(s) filho(s): $dadosUso
                Pergunta do pai: $pergunta
                
                Responda de forma empática, técnica e curta (máximo 4 parágrafos). 
                Dê conselhos práticos de como o pai pode conversar com o filho sobre isso.
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            response.text ?: "O Gemini não conseguiu gerar uma resposta no momento."
        } catch (e: Exception) {
            Log.e("GeminiRepository", "Erro na SDK: ${e.message}")
            // Se falhar a SDK (como o erro 404 do screenshot), sugerimos verificar a API Key 
            // ou usar o backend que você já tem configurado no AnaliseDAO.
            "A IA está temporariamente indisponível (Erro 404). Verifique se a API Key em GeminiConfig é válida e se o modelo 'gemini-1.5-flash' está liberado para sua conta."
        }
    }
}
