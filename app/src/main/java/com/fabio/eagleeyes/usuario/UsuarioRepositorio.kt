package com.fabio.eagleeyes.usuario

class UsuarioRepositorio (private val usuarioDao: UsuarioDAO = UsuarioDAO()){
    suspend fun login (email: String, senha: String): String?
    {
        return usuarioDao.login(email, senha)
    }

    suspend fun cadastrar (usuario: Usuario, local: String): String?
    {
        return usuarioDao.cadastrar(usuario, local)
    }
}