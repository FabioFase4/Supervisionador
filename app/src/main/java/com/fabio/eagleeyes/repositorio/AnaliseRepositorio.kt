package com.fabio.eagleeyes.repositorio

import com.fabio.eagleeyes.dao.AnaliseDAO

class AnaliseRepositorio {
    private val analiseDAO = AnaliseDAO()

    suspend fun novaRequisicaoPai(prompt: String, usarBackend: Boolean = true): String {
        return analiseDAO.analiseTempoTelaFilhoParaPai(prompt, usarBackend)
    }

    suspend fun novaRequisicaoFilho(prompt: String, usarBackend: Boolean = true): String {
        return analiseDAO.analiseTempoTelaParaFilho(prompt, usarBackend)
    }
}