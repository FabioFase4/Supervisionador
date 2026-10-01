package com.fabio.eagleeyes.funcionalidades.monitoramento.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity

import com.fabio.eagleeyes.R
import com.fabio.eagleeyes.filho.home.HomeFilhoActivity

class BloqueioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bloqueio)

        val appBloqueado = intent.getStringExtra("APP_BLOQUEADO") ?: "Aplicativo"

        val txtNomeAppBloqueado = findViewById<TextView>(R.id.txtNomeAppBloqueado)
        val btnVoltarHome = findViewById<Button>(R.id.btnVoltarHome)

        txtNomeAppBloqueado.text = appBloqueado

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
    }
}