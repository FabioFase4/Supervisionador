package com.fabio.eagleeyes.cadastro

import com.fabio.eagleeyes.databinding.ActivityRegistroFilhoBinding
import com.fabio.eagleeyes.global.Validator
import com.fabio.eagleeyes.usuario.UsuarioFilho

class CadastroFilhoActivity : CadastroActivity<ActivityRegistroFilhoBinding>() {

    override fun inflateBinding() = ActivityRegistroFilhoBinding.inflate(layoutInflater)

    override fun setupSpecificLogic() {
        binding.btnCadastrar.setOnClickListener {
            val camposComuns = binding.layoutCamposComuns
            
            // Valida campos comuns e o ID do Responsável (UID do Pai)
            val baseValida = validarCamposBase(camposComuns)
            val idValido = Validator.validarCampoVazio(binding.layoutIDResponsavel)

            if (baseValida && idValido) {
                val filho = UsuarioFilho(
                    nome = camposComuns.edtNome.text.toString().trim(),
                    email = camposComuns.edtEmail.text.toString().trim(),
                    senha = camposComuns.edtSenha.text.toString().trim(),
                    telefone = camposComuns.edtTelefone.text.toString().trim(),
                    cpf = camposComuns.edtCPF.text.toString().trim(),
                    // Busca o gênero diretamente do RadioGroup da Activity principal
                    genero = getGeneroSelecionado(binding.rgGenero.checkedRadioButtonId),
                    idResponsavel = binding.edtIDResponsavel.text.toString().trim()
                )
                viewModel.cadastrarFilho(filho)
            }
        }
    }
}
