package com.fabio.eagleeyes.vinculo

import com.fabio.eagleeyes.global.FirebaseConfig
import com.fabio.eagleeyes.usuario.Usuario
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class PerfilVinculo(val uid: String, val responsavel: Boolean, val idResponsavel: String?)

class VinculoRepository(private val db: DatabaseReference = FirebaseConfig.getDatabase()) {
    suspend fun perfil(): PerfilVinculo {
        val uid = FirebaseConfig.getAuth().currentUser?.uid ?: error("Entre na sua conta novamente.")
        if (db.child("usuarios/pais/$uid").get().await().exists()) return PerfilVinculo(uid, true, null)
        val filho = db.child("usuarios/filhos/$uid").get().await()
        check(filho.exists()) { "Perfil não encontrado. Entre novamente na conta." }
        return PerfilVinculo(uid, false, filho.child("idResponsavel").getValue(String::class.java))
    }

    suspend fun vincular(idInformado: String) {
        val id = VinculoPolicy.normalizarId(idInformado)
        val perfil = perfil()
        require(id != perfil.uid) { "Use o ID da outra pessoa, não o seu próprio ID." }
        val pai = if (perfil.responsavel) perfil.uid else id
        val filho = if (perfil.responsavel) id else perfil.uid
        check(db.child("usuarios/pais/$pai/nome").get().await().exists()) { "Responsável não encontrado. Confira o ID copiado na conta do responsável." }
        check(db.child("usuarios/filhos/$filho/nome").get().await().exists()) { "Jovem não encontrado. Confira o ID copiado na conta do jovem." }
        check(FirebaseConfig.getAuth().currentUser?.uid == perfil.uid) { "A conta mudou. Entre novamente." }
        // A transação impede que dois responsáveis sobrescrevam o mesmo vínculo.
        suspendCancellableCoroutine<Unit> { continuation ->
            db.child("usuarios/filhos/$filho/idResponsavel").runTransaction(object : Transaction.Handler {
                override fun doTransaction(data: MutableData): Transaction.Result {
                    if (!VinculoPolicy.podeVincular(data.getValue(String::class.java), pai)) return Transaction.abort()
                    data.value = pai
                    return Transaction.success(data)
                }
                override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                    if (!continuation.isActive) return
                    when {
                        error != null -> continuation.resumeWithException(error.toException())
                        !committed -> continuation.resumeWithException(IllegalStateException("Este jovem já está vinculado a outro responsável."))
                        else -> continuation.resume(Unit)
                    }
                }
            }, false)
        }
    }

    fun observarFilhos(uidPai: String) = callbackFlow {
        val id = VinculoPolicy.normalizarId(uidPai)
        val query = db.child("usuarios/filhos").orderByChild("idResponsavel").equalTo(id)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    trySend(snapshot.children.mapNotNull { child ->
                        child.getValue(Usuario::class.java)?.apply { uid = child.key.orEmpty() }
                    }.sortedBy { it.nome.lowercase() })
                } catch (error: Exception) { close(error) }
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }
}
