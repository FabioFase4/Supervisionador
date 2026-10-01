package com.fabio.eagleeyes.alerta

import com.fabio.eagleeyes.global.FirebaseConfig
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await

class AlertaDAO {
    private val alertasRef = FirebaseConfig.getAlertasRef()

    // Formata o email: minúsculo, sem espaços e troca ponto por vírgula
    private fun String.toFirebaseKey(): String = this.lowercase().trim().replace(".", ",")

    fun getAlertasRef(emailPai: String, emailFilho: String? = null): DatabaseReference {
        val refPai = alertasRef.child(emailPai.toFirebaseKey())
        return if (emailFilho != null) {
            refPai.child(emailFilho.toFirebaseKey())
        } else {
            refPai
        }
    }

    suspend fun salvarAlerta(emailPai: String, emailFilho: String, alerta: Alerta): Boolean {
        return try {
            val ref = getAlertasRef(emailPai, emailFilho)
            val novaChave = ref.push().key ?: return false
            ref.child(novaChave).setValue(alerta).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
