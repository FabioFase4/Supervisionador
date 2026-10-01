package com.fabio.eagleeyes

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("LOGIN_PREFS", Context.MODE_PRIVATE)

    fun salvarDadosLogin(email: String, tipo: String) {
        sharedPref.edit().apply {
            putString("LAST_EMAIL", email)
            putString("USER_TYPE", tipo)
            apply()
        }
    }

    fun recuperarEmail(): String {
        return sharedPref.getString("LAST_EMAIL", "") ?: ""
    }

    fun recuperarTipoUsuario(): String {
        return sharedPref.getString("USER_TYPE", "") ?: ""
    }

    fun limparSessao() {
        sharedPref.edit().clear().apply()
    }
}