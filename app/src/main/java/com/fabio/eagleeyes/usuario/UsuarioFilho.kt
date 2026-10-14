package com.fabio.eagleeyes.usuario

class UsuarioFilho (
    nome: String,
    email: String,
    senha: String,
    telefone: String,
    cpf: String,
    genero: String,
    idResponsavel: String? = null
) : Usuario(
    nome = nome,
    email = email,
    senha = senha,
    telefone = telefone,
    cpf = cpf,
    genero = genero,
    idResponsavel = idResponsavel,
    tipo = "Filho"
)
