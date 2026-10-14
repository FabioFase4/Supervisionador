package com.fabio.eagleeyes.filho.home

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.fabio.eagleeyes.R
import com.fabio.eagleeyes.alerta.activitiy.AlertasFilhoActivity
import com.fabio.eagleeyes.databinding.ActivityHomeFilhoBinding
import com.fabio.eagleeyes.funcionalidades.historico.RelatorioFilhoActivity
import com.fabio.eagleeyes.funcionalidades.monitoramento.BloqueioAppAccessibilityService
import com.fabio.eagleeyes.funcionalidades.monitoramento.MonitoramentoService
import com.fabio.eagleeyes.global.BaseHomeActivity
import com.fabio.eagleeyes.global.FirebaseConfig
import com.fabio.eagleeyes.global.PermissionHelper
import com.fabio.eagleeyes.global.PolicyManagerReceiver
import com.fabio.eagleeyes.repositorio.UsoRepositorio

class HomeFilhoActivity : BaseHomeActivity<ActivityHomeFilhoBinding>() {

    private lateinit var viewModel: HomeFilhoViewModel
    private val devicePolicyManager by lazy { getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager }
    private val compName by lazy { ComponentName(this, PolicyManagerReceiver::class.java) }

    private val emailUser by lazy { intent.getStringExtra("EMAIL") }

    // Launchers modernos para capturar o retorno das configurações de forma limpa
    private val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        atualizarStatusConfiguracao()
    }

    override fun inflateBinding() = ActivityHomeFilhoBinding.inflate(layoutInflater)

    override fun setupSpecificLogic() {
        setupViewModel()
        setupObservers()
        configurarCliques()
        
        // Inicia o monitoramento caso a permissão de uso já tenha sido concedida
        if (PermissionHelper.temPermissaoUso(this)) {
            iniciarServicoMonitoramento()
        }
    }

    override fun onResume() {
        super.onResume()
        atualizarStatusConfiguracao()
    }

    private fun setupViewModel() {
        val usoRepositorio = UsoRepositorio(this)
        val factory = HomeFilhoViewModelFactory(usoRepositorio)
        viewModel = ViewModelProvider(this, factory)[HomeFilhoViewModel::class.java]
    }

    private fun setupObservers() {
        viewModel.userName.observe(this) { nome -> binding.txtNomeDetalhes.text = "Olá, $nome" }
        viewModel.userEmail.observe(this) { email -> binding.txtEmailDetalhes.text = email }

        viewModel.respostaIA.observe(this) { resposta ->
            if (resposta.isNotEmpty()) {
                binding.cardRespostaIA.visibility = View.VISIBLE
                binding.txtRespostaIA.text = resposta
            }
        }

        viewModel.carregando.observe(this) { isLoading ->
            controlarLoading(isLoading)
            if (isLoading) {
                binding.cardRespostaIA.visibility = View.VISIBLE
                binding.txtRespostaIA.text = "Analisando seu uso de tela..."
            }
        }

        viewModel.erro.observe(this) { msg -> mostrarErro(msg) }
    }

    private fun configurarCliques() {
        binding.btnChatbox.setOnClickListener {
            startActivity(Intent(this, com.fabio.eagleeyes.chat.ChatActivity::class.java))
        }
        binding.btnVincularContas.setOnClickListener {
            startActivity(Intent(this, com.fabio.eagleeyes.vinculo.VinculoActivity::class.java))
        }

        binding.btnVerHistorico.setOnClickListener {
            val intent = Intent(this, RelatorioFilhoActivity::class.java).apply {
                putExtra("FILHO_UID", FirebaseConfig.getAuth().currentUser?.uid)
                putExtra("FILHO_NOME", viewModel.userName.value)
                putExtra("FILHO_EMAIL", emailUser)
            }
            startActivity(intent)
        }

        binding.btnVerAlertasFilho.setOnClickListener {
            val intent = Intent(this, AlertasFilhoActivity::class.java).apply {
                putExtra("FILHO_EMAIL", viewModel.userEmail.value)
                putExtra("FILHO_NOME", viewModel.userName.value)
            }
            startActivity(intent)
        }

        binding.btnAnaliseIA.setOnClickListener {
            if (PermissionHelper.temPermissaoUso(this)) {
                viewModel.realizarAnaliseIA()
            } else {
                settingsLauncher.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        }

        binding.btnAtivarAdmin.setOnClickListener { ativarFluxoProtecao() }
    }

    override fun executarLogout() {
        // Para o serviço antes de deslogar (comportamento específico do Filho)
        stopService(Intent(this, MonitoramentoService::class.java))
        super.executarLogout()
    }

    private fun ativarFluxoProtecao() {
        when {
            // 1. Notificações (Android 13+) - Necessário para o serviço aparecer
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }

            // 2. Permissão de Acesso ao Uso (Essencial para o monitoramento funcionar!)
            !PermissionHelper.temPermissaoUso(this) -> {
                Toast.makeText(this, "Ative o acesso de uso para o EagleEyes", Toast.LENGTH_LONG).show()
                settingsLauncher.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }

            // 3. Administrador do Dispositivo
            !devicePolicyManager.isAdminActive(compName) -> {
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
                    putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.device_admin_description))
                }
                settingsLauncher.launch(intent)
            }

            // 4 Sobreposição de Tela (Overlay)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this) -> {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                settingsLauncher.launch(intent)
            }

            // 5. Serviço de Acessibilidade
            !PermissionHelper.isAccessibilityServiceEnabled(this, BloqueioAppAccessibilityService::class.java) -> {
                Toast.makeText(this, "Ative o 'EagleEyes Bloqueio' na lista.", Toast.LENGTH_LONG).show()
                settingsLauncher.launch(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            else -> Toast.makeText(this, "Toda a proteção já está ativa!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun atualizarStatusConfiguracao() {
        val admin = devicePolicyManager.isAdminActive(compName)
        val overlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(this) else true
        val acessibilidade = PermissionHelper.isAccessibilityServiceEnabled(this, BloqueioAppAccessibilityService::class.java)

        val tudoAtivo = admin && overlay && acessibilidade
        binding.btnAtivarAdmin.apply {
            text = if (tudoAtivo) "Proteção Ativa" else "Concluir Configuração"
            alpha = if (tudoAtivo) 0.6f else 1.0f
        }
    }

    private fun iniciarServicoMonitoramento() {
        val intent = Intent(this, MonitoramentoService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
        else startService(intent)
    }
}