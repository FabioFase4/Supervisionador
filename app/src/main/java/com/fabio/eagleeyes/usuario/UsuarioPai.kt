package com.fabio.eagleeyes.usuario

class UsuarioPai (
    nome: String,
    email: String,
    senha: String,
    telefone: String,
    cpf: String,
    genero: String,
    val numeroFilhos: Int // Campo específico de Pai
) : Usuario(
    nome = nome,
    email = email,
    senha = senha,
    telefone = telefone,
    cpf = cpf,
    genero = genero,
    tipo = "Pai"
)
