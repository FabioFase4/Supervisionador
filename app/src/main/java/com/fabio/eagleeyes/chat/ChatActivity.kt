package com.fabio.eagleeyes.chat

import com.fabio.eagleeyes.R

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    private lateinit var model: ChatViewModel
    private lateinit var chatContainer: LinearLayout
    private lateinit var input: EditText
    private lateinit var send: MaterialButton
    private lateinit var scroll: ScrollView
    private var renderedCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)
        findViewById<View>(R.id.btnChatVoltar).setOnClickListener { finish() }
        model = ViewModelProvider(this)[ChatViewModel::class.java]
        chatContainer = findViewById(R.id.chatContainer)
        input = findViewById(R.id.inputMensagem)
        send = findViewById(R.id.btnEnviar)
        scroll = findViewById(R.id.scrollView)
        ViewCompat.setAccessibilityHeading(findViewById(R.id.welcomeTitle), true)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, keyboard.bottom))
            insets
        }
        input.setText(model.draft)
        input.doAfterTextChanged {
            model.draft = it?.toString().orEmpty()
            updateSend()
        }
        send.setOnClickListener { sendMessage() }
        input.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }
        findViewById<View>(R.id.btnRetry).setOnClickListener { model.retry() }
        findViewById<View>(R.id.btnAbout).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.chatbox_about).setMessage(R.string.chatbox_about_body)
                .setPositiveButton(R.string.chatbox_close, null).show()
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.state.collect { render(it) }
            }
        }
    }

    private fun sendMessage() {
        if (!model.state.value.canSend || input.text.isNullOrBlank()) return
        model.send(input.text.toString())
        if (model.draft.isEmpty()) input.text.clear()
    }

    private fun updateSend() {
        send.isEnabled = model.state.value.canSend && !input.text.isNullOrBlank()
        send.alpha = if (send.isEnabled) 1f else 0.4f
    }

    private fun render(state: ChatState) {
        val addedMessages = state.messages.size > renderedCount
        state.messages.drop(renderedCount).forEach(::addMessage)
        renderedCount = state.messages.size
        findViewById<View>(R.id.loadingContainer).isVisible = state.loading
        findViewById<TextView>(R.id.loadingText).setText(
            if (state.messages.isEmpty()) R.string.chatbox_loading_initial else R.string.chatbox_loading_reply
        )
        findViewById<View>(R.id.errorContainer).isVisible = state.error != null
        findViewById<TextView>(R.id.errorText).text = state.error
        updateSend()
        // Keep the welcome card visible for the opening; follow subsequent replies.
        if ((addedMessages || state.error != null) && state.messages.size > 1) {
            scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
        }
    }

    private fun addMessage(message: ChatMessage) {
        val isUser = message.role == "user"
        val group = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(18) }
            setPadding(if (isUser) dp(36) else 0, 0, if (isUser) 0 else dp(24), 0)
            gravity = if (isUser) Gravity.END else Gravity.START
        }
        group.addView(TextView(this).apply {
            text = if (isUser) "VOCÊ" else getString(R.string.chatbox_ai_label)
            textSize = 11f
            letterSpacing = 0.1f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(getColor(R.color.chat_muted))
            setPadding(dp(4), 0, dp(4), dp(7))
        })
        group.addView(TextView(this).apply {
            text = message.content
            textSize = 16f
            setLineSpacing(dp(4).toFloat(), 1f)
            setTextColor(getColor(if (isUser) R.color.chat_on_primary else R.color.chat_text))
            setPadding(dp(18), dp(15), dp(18), dp(15))
            background = GradientDrawable().apply {
                setColor(getColor(if (isUser) R.color.chat_primary else R.color.chat_surface))
                cornerRadius = dp(20).toFloat()
                if (!isUser) setStroke(dp(1), getColor(R.color.chat_border))
            }
            setTextIsSelectable(true)
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
        })
        chatContainer.addView(group)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
