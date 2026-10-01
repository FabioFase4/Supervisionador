package com.fabio.eagleeyes.formatadores

import android.util.Log

import org.json.JSONArray
import org.json.JSONObject

class MontarJSON {
    fun JsonGemini (prompt: String): String
    {
        Log.d("MontarJSON", "prompt: $prompt")
        return JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
        }.toString()
    }
}