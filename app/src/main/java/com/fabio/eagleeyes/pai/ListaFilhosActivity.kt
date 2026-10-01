package com.fabio.eagleeyes.pai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.fabio.eagleeyes.adapter.FilhoAdapter
import com.fabio.eagleeyes.databinding.ActivityListaFilhosBinding
import com.fabio.eagleeyes.global.FirebaseConfig
import com.fabio.eagleeyes.pai.home.HomePaiDAO
import com.fabio.eagleeyes.pai.home.HomePaiRepository
import com.fabio.eagleeyes.pai.home.HomePaiViewModel
import com.fabio.eagleeyes.pai.home.HomePaiViewModelFactory
import com.fabio.eagleeyes.cadastro.CadastroOpcoesActivity

class ListaFilhosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaFilhosBinding
    private lateinit var viewModel: HomePaiViewModel
    private lateinit var adapter: FilhoAdapter
    private var emailPai: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityListaFilhosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        emailPai = intent.getStringExtra("EMAIL_PAI")

        setupArchitecture()
        setupRecyclerView()
        setupObservers()
        setupCliques()

        // Carrega a lista de filhos vinculados a este pai
        emailPai?.let { viewModel.carregarFilhos(it) }
    }

    private fun setupArchitecture() {
        val database = FirebaseConfig.getDatabase()
        val dao = HomePaiDAO(database)
        val repository = HomePaiRepository(dao)
        val factory = HomePaiViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[HomePaiViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = FilhoAdapter(mutableListOf())
        binding.rvFilhos.layoutManager = LinearLayoutManager(this)
        binding.rvFilhos.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.carregando.observe(this) { estaCarregando ->
            binding.progressBar.visibility = if (estaCarregando) View.VISIBLE else View.GONE
            binding.btnCadastrarFilho.isEnabled = !estaCarregando
        }

        viewModel.listaFilhos.observe(this) { filhos ->
            adapter.updateList(filhos ?: emptyList())
        }

        viewModel.erro.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupCliques() {
        binding.btnCadastrarFilho.setOnClickListener {
            // Direciona para a tela de opções, mas aqui o contexto já é de adicionar um filho
            val intent = Intent(this, CadastroOpcoesActivity::class.java)
            intent.putExtra("VINCULO_MODO", true) 
            startActivity(intent)
        }

        binding.btnVoltar.setOnClickListener {
            finish()
        }
    }
}
