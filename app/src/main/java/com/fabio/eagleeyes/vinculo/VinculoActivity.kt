package com.fabio.eagleeyes.vinculo

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.fabio.eagleeyes.databinding.ActivityVinculoBinding
import kotlinx.coroutines.launch

class VinculoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVinculoBinding
    private lateinit var model: VinculoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVinculoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        model = ViewModelProvider(this)[VinculoViewModel::class.java]
        binding.btnVoltar.setOnClickListener { finish() }
        binding.btnCopiarId.setOnClickListener {
            model.state.value.perfil?.let {
                getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("ID da conta", it.uid))
                Toast.makeText(this, "ID copiado", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnVincular.setOnClickListener { model.vincular(binding.edtOutroId.text.toString()) }
        binding.btnAtualizarVinculo.setOnClickListener { model.carregar() }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.state.collect { state ->
                    val perfil = state.perfil
                    binding.txtMeuId.text = perfil?.uid.orEmpty()
                    binding.layoutOutroId.hint = if (perfil?.responsavel == true) "ID do jovem" else "ID do responsável"
                    binding.txtStatusVinculo.text = when {
                        perfil == null -> "Carregue seu perfil para vincular as contas."
                        perfil.responsavel -> "Cole o ID do jovem abaixo ou compartilhe seu ID com ele. Os filhos vinculados aparecem em Meus filhos."
                        perfil.idResponsavel.isNullOrBlank() -> "Nenhum responsável vinculado. Cole o ID do responsável abaixo ou compartilhe seu ID com ele."
                        else -> "ID do responsável vinculado:\n${perfil.idResponsavel}"
                    }
                    binding.btnVincular.isEnabled = !state.carregando && perfil != null
                    binding.btnCopiarId.isEnabled = perfil != null
                    binding.btnAtualizarVinculo.isEnabled = !state.carregando
                    binding.progressBar.isVisible = state.carregando
                    binding.txtResultado.text = state.mensagem.orEmpty()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        model.carregar()
    }
}
