package com.fabio.eagleeyes.global

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.fabio.eagleeyes.R
import com.fabio.eagleeyes.SessionManager
import com.fabio.eagleeyes.login.LoginActivity

/**
 * Base Activity para as telas principais (Home).
 * Centraliza Logout, Gerenciamento de Sessão e UI comum.
 */
abstract class BaseHomeActivity<VB : ViewBinding> : AppCompatActivity() {

    protected lateinit var binding: VB
    protected lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflateBinding()
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        
        setupBaseUI()
        setupSpecificLogic()
    }

    abstract fun inflateBinding(): VB
    abstract fun setupSpecificLogic()

    private fun setupBaseUI() {
        // Tenta encontrar um botão de logout padrão nos layouts
        findViewById<View>(R.id.btnLogout)?.setOnClickListener {
            confirmarLogout()
        }
    }

    protected fun confirmarLogout() {
        AlertDialog.Builder(this)
            .setTitle("Sair")
            .setMessage("Deseja realmente encerrar a sessão?")
            .setPositiveButton("Sair") { _, _ ->
                executarLogout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    protected open fun executarLogout() {
        FirebaseConfig.getAuth().signOut()
        // O SessionManager deve ter um método para limpar ou simplesmente sobrescrevemos
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    protected fun mostrarErro(mensagem: String?) {
        mensagem?.let {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    protected fun controlarLoading(estaCarregando: Boolean) {
        findViewById<View>(R.id.progressBar)?.visibility = if (estaCarregando) View.VISIBLE else View.GONE
    }
}
