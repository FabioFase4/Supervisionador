package com.fabio.eagleeyes.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
) {
    val canSend: Boolean get() = messages.lastOrNull()?.role == "assistant" && !loading && error == null
}

class ChatViewModel(
    private val repository: ChatResponder = ChatRepository(),
    private val history: ChatHistoryStore = ChatHistoryRepository()
) : ViewModel() {
    private val mutableState = MutableStateFlow(ChatState())
    val state = mutableState.asStateFlow()
    var draft = ""
    private var loaded = false
    private var pendingReply: ChatMessage? = null

    init { continueConversation() }

    fun send(text: String) {
        val message = text.trim()
        if (!state.value.canSend || message.isEmpty() || message.length > 4000) return
        try {
            val entry = history.newMessage(message)
            draft = ""
            mutableState.value = state.value.copy(messages = state.value.messages + entry)
            continueConversation()
        } catch (error: Exception) {
            mutableState.value = state.value.copy(error = error.message)
        }
    }

    fun retry() {
        if (!state.value.loading && state.value.error != null) continueConversation()
    }

    private fun continueConversation() {
        mutableState.value = state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            var saving = false
            try {
                if (!loaded) {
                    mutableState.value = state.value.copy(messages = storageOperation { history.load() })
                    loaded = true
                }
                val last = state.value.messages.lastOrNull()
                if (last == null || last.role == "user") {
                    if (last != null) {
                        saving = true
                        val saved = storageOperation { history.save(last) }
                        mutableState.value = state.value.copy(messages = state.value.messages.dropLast(1) + saved)
                    }
                    saving = false
                    val reply = pendingReply ?: ChatMessage(
                        // A resposta fica imediatamente depois da mensagem correspondente na ordenação por chave.
                        id = last?.let { "${it.id}_resposta" } ?: "-00000000000000000000",
                        role = "assistant",
                        content = repository.reply(state.value.messages.map { Message(it.role, it.content) })
                    ).also { pendingReply = it }
                    saving = true
                    val savedReply = storageOperation { history.save(reply) }
                    mutableState.value = state.value.copy(messages = state.value.messages + savedReply)
                    pendingReply = null
                }
                mutableState.value = state.value.copy(loading = false)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.value = state.value.copy(loading = false, error = when {
                    saving -> "Não foi possível confirmar o salvamento no Firebase. Confira sua conexão e tente novamente."
                    !loaded -> "Não foi possível recuperar sua conversa. Confira a conexão e as permissões da conta e tente novamente."
                    else -> error.message ?: "A resposta foi interrompida. Tente novamente."
                })
            }
        }
    }

    private suspend fun <T> storageOperation(action: suspend () -> T): T = try {
        withTimeout(20_000) { action() }
    } catch (error: TimeoutCancellationException) {
        throw ChatException("O Firebase demorou para responder. Confira sua conexão e tente novamente.")
    }
}
