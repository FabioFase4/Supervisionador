package com.fabio.eagleeyes.filho

import android.util.Log
import com.fabio.eagleeyes.global.FirebaseConfig
import com.fabio.eagleeyes.funcionalidades.historico.HistoricoUso
import com.fabio.eagleeyes.funcionalidades.regra.Regra

import kotlinx.coroutines.tasks.await

class FilhoDAO {
    private val rootDb = FirebaseConfig.getDatabase()

    private fun String.toFirebaseKey(): String = this.replace(".", ",")

    suspend fun buscarNomeFilho(uid: String): String? {
        return try {
            val snapshot = rootDb.child("usuarios").child("filhos").child(uid).get().await()
            snapshot.child("nome").getValue(String::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun salvarRegra(regra: Regra): Boolean {
        return try {
            val emailChave = regra.emailFilho.toFirebaseKey()
            val ref = rootDb.child("regras").child(emailChave)
            
            val chave = if (regra.id.isEmpty()) {
                ref.push().key ?: return false
            } else {
                regra.id
            }
            
            regra.id = chave
            ref.child(chave).setValue(regra).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun excluirRegra(emailFilho: String, idRegra: String): Boolean {
        return try {
            val emailChave = emailFilho.toFirebaseKey()
            rootDb.child("regras").child(emailChave).child(idRegra).removeValue().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun buscarRegras(emailFilho: String): List<Regra> {
        return try {
            val emailChave = emailFilho.toFirebaseKey()
            val snapshot = rootDb.child("regras").child(emailChave).get().await()
            val lista = mutableListOf<Regra>()
            if (snapshot.exists()) {
                for (item in snapshot.children) {
                    item.getValue(Regra::class.java)?.let { lista.add(it) }
                }
            }
            lista
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun buscarHistorico(uidFilho: String): List<HistoricoUso> {
        return try {
            // CAMINHO ALTERADO: historicoUso/{uidFilho}
            val snapshot = FirebaseConfig.getHistoricoUsoRef()
                .child(uidFilho)
                .get()
                .await()
            
            val lista = mutableListOf<HistoricoUso>()
            
            if (snapshot.exists()) {
                // Navega pelas datas (ex: 2024-05-22)
                for (diaSnapshot in snapshot.children) {
                    val dataStr = diaSnapshot.key ?: ""
                    
                    // Navega pelos apps de cada data
                    for (appSnapshot in diaSnapshot.children) {
                        val packageName = appSnapshot.key?.replace("_", ".") ?: ""
                        
                        // O valor agora é salvo diretamente em minutos pelo MonitoramentoService
                        val tempoMinutos = (appSnapshot.value as? Long) ?: (appSnapshot.value as? Int)?.toLong() ?: 0
                        
                        if (tempoMinutos > 0) {
                            lista.add(HistoricoUso(
                                nomeApp = packageName,
                                tempoGasto = tempoMinutos,
                                data = dataStr
                            ))
                        }
                    }
                }
            }
            // Retorna os mais recentes primeiro
            lista.sortedByDescending { it.data }
        } catch (e: Exception) {
            Log.e("FilhoDAO", "Erro ao buscar histórico: ${e.message}")
            emptyList()
        }
    }

    suspend fun registrarAtividade(uidFilho: String, historico: HistoricoUso): Boolean {
        return try {
            // CAMINHO ALTERADO: historicoUso/{uidFilho}/{data}
            val ref = FirebaseConfig.getHistoricoUsoRef()
                .child(uidFilho)
                .child(historico.data)
            
            val packageChave = historico.nomeApp.replace(".", "_")
            // Salva diretamente em minutos para manter a consistência
            ref.child(packageChave).setValue(historico.tempoGasto).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun gerarHistoricoTeste(uidFilho: String): Boolean {
        return try {
            val data = "2024-05-22"
            // CAMINHO ALTERADO: historicoUso/{uidFilho}/{data}
            val ref = FirebaseConfig.getHistoricoUsoRef().child(uidFilho).child(data)
            
            // Dados de teste em minutos
            val dadosTeste = mapOf(
                "com_instagram_android" to 45L,
                "com_google_android_youtube" to 120L,
                "com_whatsapp" to 30L
            )
            
            ref.setValue(dadosTeste).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
