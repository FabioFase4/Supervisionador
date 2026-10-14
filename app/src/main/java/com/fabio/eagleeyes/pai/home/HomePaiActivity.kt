package com.fabio.eagleeyes.pai.home

import android.Manifest
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.Toast
import com.fabio.eagleeyes.vinculo.VinculoActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.fabio.eagleeyes.alerta.activitiy.AlertasActivity
import com.fabio.eagleeyes.databinding.ActivityHomePaiBinding
import com.fabio.eagleeyes.global.BaseHomeActivity
import com.fabio.eagleeyes.global.FirebaseConfig
import com.fabio.eagleeyes.pai.ListaFilhosActivity

class HomePaiActivity : BaseHomeActivity<ActivityHomePaiBinding>() {

    private lateinit var viewModel: HomePaiViewModel
    private val PERMISSION_REQUEST_CODE = 112

    override fun inflateBinding() = ActivityHomePaiBinding.inflate(layoutInflater)

    override fun setupSpecificLogic() {
        setupViewModel()
        setupObservers()
        verificarPermissoesNecessarias()
        configurarCliques()
        carregarDados()
    }

    private fun setupViewModel() {
        val database = FirebaseConfig.getDatabase()
        val dao = HomePaiDAO(database)
        val repository = HomePaiRepository(dao)
        val factory = HomePaiViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[HomePaiViewModel::class.java]
    }

    private fun carregarDados() {
        val user = FirebaseConfig.getAuth().currentUser
        if (user == null) {
            executarLogout()
            return
        }
        viewModel.carregarDadosIniciais(user.uid)
    }

    private fun setupObservers() {
        viewModel.carregando.observe(this) { estaCarregando ->
            controlarLoading(estaCarregando)
            binding.btnPerguntarIA.isEnabled = !estaCarregando
        }

        viewModel.respostaIA.observe(this) { resposta ->
            if (resposta.isNotEmpty()) {
                binding.txtRespostaIA.visibility = View.VISIBLE
                binding.txtRespostaIA.text = resposta
            }
        }

        viewModel.erro.observe(this) { msg ->
            mostrarErro(msg)
        }
    }

    private fun configurarCliques() {
        binding.btnMeusFilhos.setOnClickListener {
            val intent = Intent(this, ListaFilhosActivity::class.java)
            startActivity(intent)
        }

        binding.btnVerAlertas.setOnClickListener {
            startActivity(Intent(this, AlertasActivity::class.java))
        }

        binding.btnInstrucoesVinculo.setOnClickListener {
            startActivity(Intent(this, VinculoActivity::class.java))
        }

        binding.btnPerguntarIA.setOnClickListener {
            val pergunta = binding.edtPesquisaIA.text.toString().trim()
            if (pergunta.isNotEmpty())
                viewModel.analisarComportamentoComIA(pergunta)
            else
                Toast.makeText(this, "Digite sua dúvida para a IA.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verificarPermissoesNecessarias() {
        val permissoes = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            permissoes.add(Manifest.permission.POST_NOTIFICATIONS)

        if (permissoes.isNotEmpty())
            ActivityCompat.requestPermissions(this, permissoes.toTypedArray(), PERMISSION_REQUEST_CODE)
    }
}
