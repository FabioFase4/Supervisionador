package com.fabio.eagleeyes.login

import android.util.Log
import com.fabio.eagleeyes.global.FirebaseConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class LoginDAO(
    private val auth: FirebaseAuth = FirebaseConfig.getAuth(),
    private val db: DatabaseReference = FirebaseConfig.getDatabase()
) {
    init {
        db.child("usuarios").keepSynced(true)
    }

    suspend fun buscarTipoPorUid(uid: String): String? = coroutineScope {
        try {
            val buscaPai = async { db.child("usuarios/pais").child(uid).get().await() }
            val buscaFilho = async { db.child("usuarios/filhos").child(uid).get().await() }

            if (buscaPai.await().exists()) return@coroutineScope "pai"
            if (buscaFilho.await().exists()) return@coroutineScope "filho"

            null
        } catch (e: Exception) {
            Log.e("LOGIN_DEBUG", "Erro ao buscar tipo por UID: ${e.message}")
            null
        }
    }

    suspend fun buscarHashPorEmail(caminho: String, email: String): String? {
        val snapshot = db.child(caminho).orderByChild("email").equalTo(email).get().await()
        return snapshot.children.firstOrNull()?.child("senha")?.value as? String
    }
}