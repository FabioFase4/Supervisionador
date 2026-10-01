package com.fabio.eagleeyes.cadastro

import com.fabio.eagleeyes.usuario.UsuarioFilho
import com.fabio.eagleeyes.usuario.UsuarioPai

class CadastroRepositorio {
    private val cadastroDAO = CadastroDAO()

    suspend fun salvarUsuarioFilho(usuario: UsuarioFilho, noDestino: String): Boolean
    {
        return cadastroDAO.salvarUsuarioFilho(usuario, noDestino)
    }
    suspend fun salvarUsuarioPai(usuario: UsuarioPai, noDestino: String): Boolean
    {
        return cadastroDAO.salvarUsuarioPai(usuario, noDestino)
    }
}