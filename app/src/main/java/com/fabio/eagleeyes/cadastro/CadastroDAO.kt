package com.fabio.eagleeyes.cadastro

import android.util.Log
import com.fabio.eagleeyes.usuario.UsuarioFilho
import com.fabio.eagleeyes.usuario.UsuarioPai
import com.fabio.eagleeyes.global.FirebaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt

class CadastroDAO {
    private val auth = FirebaseConfig.getAuth()
    private val db = FirebaseConfig.getDatabase()

    /**
     * Salva um usuário do tipo Pai no Auth e no Database com verificação de segurança.
     */
    suspend fun salvarUsuarioPai(usuario: UsuarioPai, noDestino: String): Boolean {
        val emailFormatado = usuario.email.lowercase().trim()
        val senhaPura = usuario.senha

        // 1. Verifica duplicidade antes de criar no Auth para evitar lixo
        verificarEmailDuplicado(emailFormatado)
        verificarTelefoneDuplicado(usuario.telefone)
        verificarCPFDuplicado(usuario.cpf)

        // 2. Cria o usuário no Firebase Authentication
        val resultado = auth.createUserWithEmailAndPassword(emailFormatado, senhaPura).await()
        val uid = resultado.user?.uid ?: throw Exception("Não foi possível criar a autenticação.")

        try {
            // 3. Criptografia e formatação
            val senhaCriptografada = withContext(Dispatchers.Default) {
                BCrypt.hashpw(senhaPura, BCrypt.gensalt())
            }

            usuario.uid = uid
            usuario.email = emailFormatado
            usuario.senha = senhaCriptografada

            // 4. Salva os dados no Database usando o UID como chave
            db.child(noDestino).child(uid).setValue(usuario).await()

            Log.d("CADASTRO_DEBUG", "Usuário Pai $uid salvo com sucesso")
            return true

        } catch (e: Exception) {
            // Se houver qualquer erro, removemos o usuário do Auth
            auth.currentUser?.delete()?.await()
            throw e
        }
    }

    /**
     * Salva um usuário do tipo Filho no Auth e no Database com verificação de segurança.
     */
    suspend fun salvarUsuarioFilho(usuario: UsuarioFilho, noDestino: String): Boolean {
        val emailFormatado = usuario.email.lowercase().trim()
        val senhaPura = usuario.senha

        // 1. Verifica duplicidade antes de criar no Auth para evitar lixo
        verificarEmailDuplicado(emailFormatado)
        verificarTelefoneDuplicado(usuario.telefone)
        verificarCPFDuplicado(usuario.cpf)

        // Verifica a quantidade de filhos do responsável usando o ID do Responsável (UID)
        if (noDestino == "usuarios/filhos") {
            verificarQuantidadeFilhos(usuario.idResponsavel ?: "")
        }

        // 2. Cria o usuário no Firebase Authentication
        val resultado = auth.createUserWithEmailAndPassword(emailFormatado, senhaPura).await()
        val uid = resultado.user?.uid ?: throw Exception("Não foi possível criar a autenticação.")

        try {
            // 3. Criptografia e formatação
            val senhaCriptografada = withContext(Dispatchers.Default) {
                BCrypt.hashpw(senhaPura, BCrypt.gensalt())
            }

            usuario.uid = uid
            usuario.email = emailFormatado
            usuario.senha = senhaCriptografada
            usuario.idResponsavel = usuario.idResponsavel?.trim()

            // 4. Salva os dados no Database usando o UID como chave
            db.child(noDestino).child(uid).setValue(usuario).await()

            Log.d("CADASTRO_DEBUG", "Usuário Filho $uid salvo com sucesso")
            return true

        } catch (e: Exception) {
            // Se houver qualquer erro, removemos o usuário do Auth
            auth.currentUser?.delete()?.await()
            throw e
        }
    }

    private suspend fun verificarTelefoneDuplicado(telefone: String) {
        val nos = listOf("usuarios/pais", "usuarios/filhos")
        for (no in nos) {
            val snapshot = db.child(no)
                .orderByChild("telefone")
                .equalTo(telefone)
                .get()
                .await()

            if (snapshot.exists()) {
                throw Exception("Este número de telefone já está cadastrado.")
            }
        }
    }

    private suspend fun verificarEmailDuplicado(email: String) {
        val nos = listOf("usuarios/pais", "usuarios/filhos")
        for (no in nos) {
            val snapshot = db.child(no)
                .orderByChild("email")
                .equalTo(email)
                .get()
                .await()

            if (snapshot.exists()) {
                throw Exception("Este e-mail já está cadastrado.")
            }
        }
    }

    private suspend fun verificarCPFDuplicado(cpf: String) {
        if (cpf.isEmpty()) return
        val nos = listOf("usuarios/pais", "usuarios/filhos")
        for (no in nos) {
            val snapshot = db.child(no)
                .orderByChild("cpf")
                .equalTo(cpf)
                .get()
                .await()

            if (snapshot.exists()) {
                throw Exception("Este CPF já está cadastrado.")
            }
        }
    }

    private suspend fun verificarQuantidadeFilhos(idResponsavel: String) {
        val idFormatado = idResponsavel.trim()

        if (idFormatado.isEmpty()) {
            throw Exception("O ID do responsável é obrigatório para cadastrar um filho.")
        }

        // 1. Busca o responsável pelo UID para verificar o limite cadastrado
        val snapshotPai = db.child("usuarios/pais").child(idFormatado).get().await()

        if (!snapshotPai.exists()) {
            throw Exception("O ID do responsável informado não foi encontrado.")
        }

        // Obtém o limite de filhos do cadastro do pai
        // Usando "numeroFilhos" que é o nome da propriedade na classe UsuarioPai
        val limite = snapshotPai.child("numeroFilhos").value?.toString()?.toIntOrNull() ?: 0

        // 2. Conta quantos filhos já estão vinculados a este responsável via ID
        val snapshotFilhos = db.child("usuarios/filhos")
            .orderByChild("idResponsavel")
            .equalTo(idFormatado)
            .get()
            .await()

        val totalFilhosJaCadastrados = snapshotFilhos.childrenCount

        if (totalFilhosJaCadastrados >= limite) {
            throw Exception("O limite de filhos ($limite) para este responsável já foi alcançado.")
        }
    }
}
