package com.fabio.eagleeyes.cadastro

import com.fabio.eagleeyes.databinding.ActivityRegistroPaiBinding
import com.fabio.eagleeyes.global.Validator
import com.fabio.eagleeyes.usuario.UsuarioPai

class CadastroPaiActivity : CadastroActivity<ActivityRegistroPaiBinding>() {

    override fun inflateBinding() = ActivityRegistroPaiBinding.inflate(layoutInflater)

    override fun setupSpecificLogic() {
        binding.btnCadastrar.setOnClickListener {
            val camposComuns = binding.layoutCamposComuns
            
            // Valida campos comuns e o campo específico do Pai
            val baseValida = validarCamposBase(camposComuns)
            val especificoValido = Validator.validarCampoVazio(binding.layoutQntFilhos)

            if (baseValida && especificoValido) {
                val pai = UsuarioPai(
                    nome = camposComuns.edtNome.text.toString().trim(),
                    email = camposComuns.edtEmail.text.toString().trim(),
                    senha = camposComuns.edtSenha.text.toString().trim(),
                    telefone = camposComuns.edtTelefone.text.toString().trim(),
                    cpf = camposComuns.edtCPF.text.toString().trim(),
                    // Busca o gênero diretamente do RadioGroup da Activity
                    genero = getGeneroSelecionado(binding.rgGenero.checkedRadioButtonId),
                    numeroFilhos = binding.edtQntFilhos.text.toString().toIntOrNull() ?: 0
                )
                viewModel.cadastrarPai(pai)
            }
        }
    }
}
