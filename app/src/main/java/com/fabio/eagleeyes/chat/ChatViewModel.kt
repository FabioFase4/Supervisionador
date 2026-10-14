package com.fabio.eagleeyes.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

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
    private var openingSaved = false
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
                val last = state.value.messages.lastOrNull()
                if (last == null) {
                    // A abertura depende somente da IA, sem ler ou gravar o histórico.
                    val opening = ChatMessage(
                        id = "abertura-${UUID.randomUUID()}",
                        role = "assistant",
                        content = repository.reply(emptyList())
                    )
                    mutableState.value = state.value.copy(messages = listOf(opening))
                } else if (last.role == "user") {
                    saving = true
                    // Ao iniciar a conversa, arquive também a abertura exibida ao jovem.
                    if (!openingSaved) {
                        val opening = storageOperation { history.save(state.value.messages.first()) }
                        mutableState.value = state.value.copy(messages = listOf(opening) + state.value.messages.drop(1))
                        openingSaved = true
                    }
                    val saved = storageOperation { history.save(last) }
                    mutableState.value = state.value.copy(messages = state.value.messages.dropLast(1) + saved)
                    saving = false
                    val reply = pendingReply ?: ChatMessage(
                        // A resposta fica imediatamente depois da mensagem correspondente na ordenação por chave.
                        id = "${last.id}_resposta",
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
