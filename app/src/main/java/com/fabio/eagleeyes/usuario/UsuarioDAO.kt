package com.fabio.eagleeyes.usuario

import com.fabio.eagleeyes.alerta.Alerta
import com.fabio.eagleeyes.global.FirebaseConfig
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await

class UsuarioDAO {
    private val auth = FirebaseConfig.getAuth()
    private val rootDb = FirebaseConfig.getDatabase()

    /**
     * Realiza o login via Firebase Auth.
     */
    suspend fun login(email: String, senhaPura: String): String? {
        return try {
            val resultado = auth.signInWithEmailAndPassword(email, senhaPura).await()
            val uid = resultado.user?.uid ?: return null

            val snapshotPai = rootDb.child("usuarios/pais").child(uid).get().await()
            if (snapshotPai.exists()) return "pai"

            val snapshotFilho = rootDb.child("usuarios/filhos").child(uid).get().await()
            if (snapshotFilho.exists()) return "filho"

            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Cria o usuário no Firebase Auth e salva os dados adicionais no Realtime Database usando o UID.
     * Lança exceção em caso de erro para que o ViewModel possa capturar a mensagem real.
     */
    suspend fun cadastrar(usuario: Usuario, local: String): String? {
        if (emailExiste(usuario.email))
            throw Exception("Este e-mail já está sendo usado.")

        if (telefoneExiste(usuario.telefone))
            throw Exception("Este telefone já está sendo usado.")

        val resultado = auth.createUserWithEmailAndPassword(usuario.email, usuario.senha).await()
        val uid = resultado.user?.uid ?: throw Exception("Falha ao obter ID do usuário.")

        usuario.uid = uid
        // Salva os dados no banco usando o UID como chave
        rootDb.child(local).child(uid).setValue(usuario).await()
        return uid
    }

    suspend fun emailExiste(email: String): Boolean {
        val paiExiste = rootDb.child("usuarios/pais")
            .orderByChild("email")
            .equalTo(email)
            .get().await().exists()
            
        val filhoExiste = rootDb.child("usuarios/filhos")
            .orderByChild("email")
            .equalTo(email)
            .get().await().exists()
            
        return paiExiste || filhoExiste
    }

    suspend fun telefoneExiste(telefone: String): Boolean {
        val snapshotPais = rootDb.child("usuarios/pais")
            .orderByChild("telefone")
            .equalTo(telefone)
            .get().await()

        if (snapshotPais.exists()) return true

        val snapshotFilhos = rootDb.child("usuarios/filhos")
            .orderByChild("telefone")
            .equalTo(telefone)
            .get().await()

        return snapshotFilhos.exists()
    }

    suspend fun listarFilhosDoPai(uidPai: String): List<Usuario> {
        return try {
            val snapshot = rootDb.child("usuarios/filhos")
                .orderByChild("idResponsavel")
                .equalTo(uidPai)
                .get().await()

            snapshot.children.mapNotNull { it.getValue(Usuario::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun escutarAlertasDoPai(onAlertasAtualizados: (List<Alerta>) -> Unit, onErro: (String) -> Unit) {
        val user = auth.currentUser ?: return
        val uidPai = user.uid

        rootDb.child("alertas").child(uidPai).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaAlertas = mutableListOf<Alerta>()
                for (alertaSnapshot in snapshot.children) {
                    val alerta = alertaSnapshot.getValue(Alerta::class.java)
                    if (alerta != null) {
                        listaAlertas.add(alerta.copy(id = alertaSnapshot.key ?: ""))
                    }
                }
                onAlertasAtualizados(listaAlertas)
            }

            override fun onCancelled(error: DatabaseError) {
                onErro(error.message)
            }
        })
    }
}
