package com.fabio.eagleeyes.alerta

import android.util.Log
import com.fabio.eagleeyes.global.FirebaseConfig
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositório responsável pela gestão de alertas entre Pais e Filhos.
 */
class AlertaRepositorio(
    private val alertaDao: AlertaDAO = AlertaDAO()
) {
    private val auth = FirebaseConfig.getAuth()
    private val rootRef = FirebaseConfig.getDatabase()

    private fun String.toFirebaseKey(): String = this.lowercase().trim().replace(".", ",")

    /**
     * Transporta os dados de identidade identificados no banco.
     */
    private data class UserIdentity(
        val emailUser: String?,
        val emailPai: String?,
        val isPai: Boolean
    )

    /**
     * Escuta alertas em tempo real.
     */
    fun escutarAlertas(emailFilhoParam: String? = null): Flow<List<Alerta>> = callbackFlow {
        val user = auth.currentUser ?: run {
            close(Exception("Usuário não autenticado"))
            return@callbackFlow
        }

        try {
            val identity = buscarIdentidade(user.uid, user.email)

            // Determina quem é o responsável (pai) para acessar o nó de alertas
            val emailPai = if (identity.isPai) identity.emailUser else identity.emailPai
            
            // Determina se vamos filtrar por um filho específico (sempre verdadeiro se o logado for filho)
            val filtroFilho = if (identity.isPai) emailFilhoParam else identity.emailUser

            if (emailPai == null) {
                Log.e("AlertaRepositorio", "Responsável não localizado para o usuário: ${user.email}")
                close(Exception("Responsável não identificado no sistema."))
                return@callbackFlow
            }

            val ref = alertaDao.getAlertasRef(emailPai, filtroFilho)
            Log.d("AlertaRepositorio", "Escutando alertas em: ${ref.path}")

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val alertas = mapearSnapshot(snapshot, !filtroFilho.isNullOrEmpty())
                    Log.d("AlertaRepositorio", "Alertas detectados: ${alertas.size} no caminho ${ref.path}")
                    trySend(alertas)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("AlertaRepositorio", "Erro Firebase: ${error.message}")
                }
            }

            ref.addValueEventListener(listener)
            awaitClose { ref.removeEventListener(listener) }

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("AlertaRepositorio", "Erro ao configurar fluxo de alertas: ${e.message}")
            close(e)
        }
    }

    suspend fun salvarAlerta(alerta: Alerta): Boolean {
        return try {
            val user = auth.currentUser ?: return false
            val identity = buscarIdentidade(user.uid, user.email)
            
            val emailPai = if (identity.isPai) identity.emailUser else identity.emailPai
            val emailFilho = if (identity.isPai) alerta.emailFilho else identity.emailUser

            if (emailPai.isNullOrEmpty() || emailFilho.isNullOrEmpty()) {
                Log.e("AlertaRepositorio", "Dados insuficientes para salvar alerta. Pai: $emailPai, Filho: $emailFilho")
                return false
            }

            alertaDao.salvarAlerta(emailPai, emailFilho, alerta)
            true
        } catch (e: Exception) {
            Log.e("AlertaRepositorio", "Erro ao salvar alerta: ${e.message}")
            false
        }
    }

    /**
     * Busca a identidade real do usuário (Pai ou Filho) no banco de dados.
     */
    private suspend fun buscarIdentidade(uid: String, emailAuth: String?): UserIdentity {
        val emailKey = emailAuth?.toFirebaseKey()

        // 1. Tenta encontrar como PAI
        var snap = rootRef.child("usuarios/pais").child(uid).get().await()
        if (snap.exists()) {
            val emailDb = snap.child("email").getValue(String::class.java)
            return UserIdentity(emailDb ?: emailAuth, null, true)
        }
        
        if (emailKey != null) {
            snap = rootRef.child("usuarios/pais").child(emailKey).get().await()
            if (snap.exists()) {
                val emailDb = snap.child("email").getValue(String::class.java)
                return UserIdentity(emailDb ?: emailAuth, null, true)
            }
        }

        // 2. Tenta encontrar como FILHO
        snap = rootRef.child("usuarios/filhos").child(uid).get().await()
        if (!snap.exists() && emailKey != null) {
            snap = rootRef.child("usuarios/filhos").child(emailKey).get().await()
        }

        if (snap.exists()) {
            val emailFilho = snap.child("email").getValue(String::class.java)
            // Correção crucial: No cadastro do filho, o vínculo é guardado em 'idResponsavel' contendo o UID do pai
            val idResponsavel = snap.child("idResponsavel").getValue(String::class.java)
            
            val emailPai = if (!idResponsavel.isNullOrEmpty()) {
                rootRef.child("usuarios/pais").child(idResponsavel).child("email").get().await().getValue(String::class.java)
            } else {
                null
            }

            return UserIdentity(emailFilho ?: emailAuth, emailPai, false)
        }

        return UserIdentity(emailAuth, null, false)
    }

    private fun mapearSnapshot(snapshot: DataSnapshot, isUnicoFilho: Boolean): List<Alerta> {
        val lista = mutableListOf<Alerta>()
        if (!snapshot.exists()) return lista

        if (isUnicoFilho) {
            // Caminho direto: /alertas/pai/filho/{alertas}
            snapshot.children.forEach { child ->
                child.getValue(Alerta::class.java)?.let { lista.add(it.copy(id = child.key ?: "")) }
            }
        } else {
            // Caminho geral: /alertas/pai/{filho}/{alertas}
            snapshot.children.forEach { filhoSnap ->
                filhoSnap.children.forEach { alertSnap ->
                    alertSnap.getValue(Alerta::class.java)?.let { lista.add(it.copy(id = alertSnap.key ?: "")) }
                }
            }
        }
        return lista.sortedByDescending { it.timestamp }
    }
}
