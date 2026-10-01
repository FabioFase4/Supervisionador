package com.fabio.eagleeyes.global

import com.fabio.eagleeyes.BuildConfig

object GeminiConfig {
    // A chave agora será lida do BuildConfig para maior segurança
    private val geminiApiKey = BuildConfig.GEMINI_API_KEY
    private val geminiBaseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"
    private val backendUrl = "https://gemini-render-backend.onrender.com/chat"

    fun getGeminiApiKey(): String = geminiApiKey
    fun getGeminiBaseUrl(): String = geminiBaseUrl
    fun getBackendUrl(): String = backendUrl
}