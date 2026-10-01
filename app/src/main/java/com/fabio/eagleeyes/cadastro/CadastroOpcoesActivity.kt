package com.fabio.eagleeyes.cadastro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fabio.eagleeyes.databinding.ActivitySelecaoPerfilBinding
import com.fabio.eagleeyes.login.LoginActivity

/**
 * Activity para seleção de perfil (Pai ou Filho).
 * Agora direciona corretamente para as Activities específicas refatoradas.
 */
class CadastroOpcoesActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySelecaoPerfilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySelecaoPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCliques()
    }

    private fun setupCliques() {
        binding.cardPai.setOnClickListener {
            iniciarCadastro(CadastroPaiActivity::class.java)
        }

        binding.cardFilho.setOnClickListener {
            iniciarCadastro(CadastroFilhoActivity::class.java)
        }

        binding.btnVoltar.setOnClickListener {
            voltarParaLogin()
        }
    }

    private fun iniciarCadastro(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
        // Opcional: finish() se você não quiser que o usuário volte para a seleção de perfil
    }

    private fun voltarParaLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
