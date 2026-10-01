package com.fabio.eagleeyes.funcionalidades.regra

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.fabio.eagleeyes.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class CriarRegraActivity : AppCompatActivity() {

    private lateinit var edtNomeAppRegra: TextInputEditText
    private lateinit var rgTipoRegra: RadioGroup
    private lateinit var edtLimiteTempo: TextInputEditText
    private lateinit var edtHoraInicio: TextInputEditText
    private lateinit var edtHoraFim: TextInputEditText
    private lateinit var btnSalvarRegra: MaterialButton
    private lateinit var btnCancelarRegra: MaterialButton
    private lateinit var progressBar: ProgressBar

    private lateinit var layoutLimite: View
    private lateinit var layoutHorario: View

    private lateinit var viewModel: RegraViewModel
    private var emailFilho: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_regra)

        carregarViews()
        setupTimePickers()

        viewModel = ViewModelProvider(this).get(RegraViewModel::class.java)
        emailFilho = intent.getStringExtra("FILHO_EMAIL") ?: ""

        rgTipoRegra.setOnCheckedChangeListener { _, checkedId ->
            modificarVisibilidades(checkedId)
        }

        btnSalvarRegra.setOnClickListener {
            validarEEnviar()
        }

        btnCancelarRegra.setOnClickListener { finish() }

        configurarObservers()
    }

    private fun carregarViews() {
        edtNomeAppRegra = findViewById(R.id.edtNomeAppRegra)
        rgTipoRegra = findViewById(R.id.rgTipoRegra)
        edtLimiteTempo = findViewById(R.id.edtLimiteTempo)
        edtHoraInicio = findViewById(R.id.edtHoraInicio)
        edtHoraFim = findViewById(R.id.edtHoraFim)
        layoutLimite = findViewById(R.id.layoutLimiteTempo)
        layoutHorario = findViewById(R.id.layoutHorarioPermitido)
        btnSalvarRegra = findViewById(R.id.btnSalvarRegra)
        btnCancelarRegra = findViewById(R.id.btnCancelarRegra)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupTimePickers() {
        // Torna os campos de hora não editáveis por teclado para forçar o uso do Picker
        edtHoraInicio.isFocusable = false
        edtHoraFim.isFocusable = false

        edtHoraInicio.setOnClickListener {
            showTimePickerDialog { time -> edtHoraInicio.setText(time) }
        }
        edtHoraFim.setOnClickListener {
            showTimePickerDialog { time -> edtHoraFim.setText(time) }
        }
    }

    private fun showTimePickerDialog(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(this, { _, hour, minute ->
            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            onTimeSelected(formattedTime)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
        timePicker.show()
    }

    private fun modificarVisibilidades(checkedId: Int) {
        layoutLimite.visibility = if (checkedId == R.id.rbLimiteTempo) View.VISIBLE else View.GONE
        layoutHorario.visibility = if (checkedId == R.id.rbHorario) View.VISIBLE else View.GONE
    }

    private fun validarEEnviar(): Boolean {
        val nomeApp = edtNomeAppRegra.text.toString().trim()
        
        if (emailFilho.isEmpty()) {
            Toast.makeText(this, "Erro: Filho não identificado", Toast.LENGTH_SHORT).show()
            return false
        }

        if (nomeApp.isEmpty()) {
            edtNomeAppRegra.error = "Informe o nome do app"
            return false
        }

        val tipo: String
        val valor: String

        when (rgTipoRegra.checkedRadioButtonId) {
            R.id.rbBloqueio -> {
                tipo = "BLOQUEIO"
                valor = "TOTAL"
            }
            R.id.rbLimiteTempo -> {
                tipo = "LIMITE_TEMPO"
                valor = edtLimiteTempo.text.toString().trim()
                if (valor.isEmpty()) {
                    edtLimiteTempo.error = "Informe os minutos"
                    return false
                }
            }
            R.id.rbHorario -> {
                tipo = "HORARIO"
                val h1 = edtHoraInicio.text.toString().trim()
                val h2 = edtHoraFim.text.toString().trim()
                if (h1.isEmpty() || h2.isEmpty()) {
                    Toast.makeText(this, "Informe o horário completo", Toast.LENGTH_SHORT).show()
                    return false
                }
                valor = "$h1-$h2"
            }
            else -> {
                Toast.makeText(this, "Selecione um tipo de regra", Toast.LENGTH_SHORT).show()
                return false
            }
        }

        val novaRegra = Regra(
            emailFilho = emailFilho,
            nomeApp = nomeApp,
            tipo = tipo,
            valor = valor,
            ativa = true
        )

        progressBar.visibility = View.VISIBLE
        btnSalvarRegra.isEnabled = false
        viewModel.salvarRegra(novaRegra)
        return true
    }

    private fun configurarObservers() {
        viewModel.sucesso.observe(this) { sucesso ->
            if (sucesso) {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Regra aplicada com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        viewModel.erro.observe(this) { erro ->
            if (erro.isNotEmpty()) {
                progressBar.visibility = View.GONE
                btnSalvarRegra.isEnabled = true
                Toast.makeText(this, erro, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
