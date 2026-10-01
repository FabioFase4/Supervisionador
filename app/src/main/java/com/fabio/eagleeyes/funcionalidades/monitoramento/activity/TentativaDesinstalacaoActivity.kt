package com.fabio.eagleeyes.funcionalidades.monitoramento.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button

import androidx.appcompat.app.AppCompatActivity

import com.fabio.eagleeyes.R
import com.fabio.eagleeyes.filho.home.HomeFilhoActivity

class TentativaDesinstalacaoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tentativa_desinstalacao)

        val btnVoltarHome = findViewById<Button>(R.id.btnVoltarHome)

        btnVoltarHome.setOnClickListener {
            val intent = Intent(this, HomeFilhoActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Bloqueia o botão de voltar para manter o aviso na tela
    }
}