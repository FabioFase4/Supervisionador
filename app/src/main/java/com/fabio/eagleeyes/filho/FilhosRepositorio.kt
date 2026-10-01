package com.fabio.eagleeyes.filho

import com.fabio.eagleeyes.funcionalidades.historico.HistoricoUso
import com.fabio.eagleeyes.funcionalidades.regra.Regra

class FilhosRepositorio(private val dao: FilhoDAO = FilhoDAO()) {

    suspend fun buscarNomeFilho(uid: String): String? {
        return dao.buscarNomeFilho(uid)
    }

    suspend fun buscarHistorico(uidFilho: String): List<HistoricoUso> {
        return dao.buscarHistorico(uidFilho)
    }

    suspend fun registrarAtividade(uidFilho: String, historico: HistoricoUso): Boolean {
        return dao.registrarAtividade(uidFilho, historico)
    }

    suspend fun gerarHistoricoTeste(emailFilho: String): Boolean {
        return dao.gerarHistoricoTeste(emailFilho)
    }

    suspend fun buscarRegras(emailFilho: String): List<Regra> {
        return dao.buscarRegras(emailFilho)
    }

    suspend fun salvarRegra(regra: Regra): Boolean {
        return dao.salvarRegra(regra)
    }

    suspend fun excluirRegra(emailFilho: String, idRegra: String): Boolean {
        return dao.excluirRegra(emailFilho, idRegra)
    }
}
