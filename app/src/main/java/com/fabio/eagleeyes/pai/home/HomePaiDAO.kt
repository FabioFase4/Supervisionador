package com.fabio.eagleeyes.pai.home

import android.util.Log
import com.fabio.eagleeyes.usuario.Usuario
import com.fabio.eagleeyes.global.FirebaseConfig
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await

class HomePaiDAO (private val database: DatabaseReference = FirebaseConfig.getDatabase()){

    suspend fun buscarPai(uid: String): Usuario? {
        return try {
            val snapshot = database.child("usuarios").child("pais").child(uid).get().await()
            snapshot.getValue(Usuario::class.java)
        } catch (e: Exception) {
            Log.e("HOME_PAI_DAO", "Erro ao buscar pai: ${e.message}")
            null
        }
    }

    suspend fun listarFilhos(emailDoPai: String): List<Usuario> {
        if (emailDoPai.isBlank()) {
            Log.e("HOME_PAI_DAO", "E-mail do pai está vazio")
            return emptyList()
        }

        return try {
            val emailOriginal = emailDoPai.trim()
            val emailLower = emailOriginal.lowercase()
            val emailComVirgula = emailOriginal.replace(".", ",")
            val emailLowerComVirgula = emailLower.replace(".", ",")

            // Lista de termos para busca (Firebase é case-sensitive)
            val termosDeBusca = listOf(emailOriginal, emailLower, emailComVirgula, emailLowerComVirgula).distinct()

            Log.d("HOME_PAI_DAO", "Iniciando busca resiliente para o pai: $emailOriginal")

            // O caminho no seu JSON é 'usuarios/filhos'
            val ref = database.child("usuarios").child("filhos")
            val listaResultados = mutableListOf<Usuario>()
            val uidsEncontrados = mutableSetOf<String>()

            for (termo in termosDeBusca) {
                val snapshot = ref.orderByChild("emailResponsavel").equalTo(termo).get().await()

                if (snapshot.exists()) {
                    Log.d("HOME_PAI_DAO", "Sucesso! Filhos encontrados para o termo: $termo")
                    for (filhoSnap in snapshot.children) {
                        val uid = filhoSnap.key ?: ""
                        if (!uidsEncontrados.contains(uid)) {
                            val filho = filhoSnap.getValue(Usuario::class.java)
                            if (filho != null) {
                                filho.uid = uid
                                listaResultados.add(filho)
                                uidsEncontrados.add(uid)
                                Log.d("HOME_PAI_DAO", "Filho adicionado: ${filho.nome}")
                            }
                        }
                    }
                }
            }

            if (listaResultados.isEmpty()) {
                Log.w("HOME_PAI_DAO", "Nenhum filho encontrado após testar todos os termos: $termosDeBusca")
            }

            listaResultados
        } catch (e: Exception) {
            Log.e("HOME_PAI_DAO", "Erro crítico ao listar filhos: ${e.message}")
            emptyList()
        }
    }
}