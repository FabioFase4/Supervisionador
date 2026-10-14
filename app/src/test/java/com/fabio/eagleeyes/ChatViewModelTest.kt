package com.fabio.eagleeyes

import com.fabio.eagleeyes.chat.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }

    private class MemoryHistory : ChatHistoryStore {
        val messages = linkedMapOf<String, ChatMessage>()
        var failLoad = false
        var failRole: String? = null
        var failAfterCommit = false
        private var next = 0
        override suspend fun load(): List<ChatMessage> {
            if (failLoad) error("Sem conexão")
            return messages.values.toList()
        }
        override fun newMessage(text: String) = ChatMessage("user-${++next}", "user", text)
        override suspend fun save(message: ChatMessage): ChatMessage {
            if (failRole == message.role && !failAfterCommit) error("Gravação recusada")
            val saved = messages.getOrPut(message.id) { message.copy(createdAt = 100) }
            if (failRole == message.role) error("Confirmação perdida")
            return saved
        }
    }

    @Test fun restoresHistoryWithoutRequestingAnotherOpening() = runTest(dispatcher) {
        val history = MemoryHistory().apply { messages["welcome"] = ChatMessage("welcome", "assistant", "Olá") }
        val model = ChatViewModel(ChatResponder { error("Não deve chamar a API") }, history)
        advanceUntilIdle()
        assertEquals("Olá", model.state.value.messages.single().content)
        assertTrue(model.state.value.canSend)
    }

    @Test fun doesNotCallApiWhenLoadingHistoryFails() = runTest(dispatcher) {
        val history = MemoryHistory().apply { failLoad = true }
        var requests = 0
        val model = ChatViewModel(ChatResponder { requests++; "Olá" }, history)
        advanceUntilIdle()
        assertEquals(0, requests)
        assertFalse(model.state.value.canSend)
        history.failLoad = false
        model.retry()
        advanceUntilIdle()
        assertEquals(1, requests)
        assertEquals(1, history.messages.size)
    }

    @Test fun persistsUserBeforeCallingApiAndRetriesWithoutDuplicate() = runTest(dispatcher) {
        val history = MemoryHistory()
        var requests = 0
        val model = ChatViewModel(ChatResponder { requests++; "Resposta" }, history)
        advanceUntilIdle()
        history.failRole = "user"
        model.send("Meu dia")
        advanceUntilIdle()
        assertEquals(1, requests)
        assertFalse(model.state.value.canSend)
        history.failRole = null
        model.retry()
        advanceUntilIdle()
        assertEquals(2, requests)
        assertEquals(1, history.messages.values.count { it.role == "user" })
        assertEquals(3, model.state.value.messages.size)
    }

    @Test fun retriesSavingTheSameReplyWithoutCallingApiAgain() = runTest(dispatcher) {
        val history = MemoryHistory().apply { failRole = "assistant"; failAfterCommit = true }
        var requests = 0
        val model = ChatViewModel(ChatResponder { requests++; "Olá" }, history)
        advanceUntilIdle()
        assertNotNull(model.state.value.error)
        history.failRole = null
        model.retry()
        advanceUntilIdle()
        assertEquals(1, requests)
        assertEquals(1, history.messages.size)
        assertTrue(model.state.value.canSend)
    }

    @Test fun resumesUnansweredPersistedUserMessage() = runTest(dispatcher) {
        val history = MemoryHistory().apply { messages["pending"] = ChatMessage("pending", "user", "Oi") }
        val model = ChatViewModel(ChatResponder { messages ->
            assertEquals("Oi", messages.last().content)
            "Olá"
        }, history)
        advanceUntilIdle()
        assertEquals(2, history.messages.size)
        assertEquals("pending_resposta", model.state.value.messages.last().id)
    }

    @Test fun apiFailureDoesNotDuplicateUserOnRetry() = runTest(dispatcher) {
        val history = MemoryHistory()
        var fail = false
        val model = ChatViewModel(ChatResponder { if (fail) throw ChatException("Sem internet") else "Oi" }, history)
        advanceUntilIdle()
        fail = true
        model.send("Olá")
        advanceUntilIdle()
        assertEquals(2, history.messages.size)
        fail = false
        model.retry()
        advanceUntilIdle()
        assertEquals(3, history.messages.size)
        assertEquals(1, model.state.value.messages.count { it.role == "user" })
    }

    @Test fun blocksDuplicateSendWhileRequestIsRunning() = runTest(dispatcher) {
        val history = MemoryHistory()
        val model = ChatViewModel(ChatResponder { "Oi" }, history)
        advanceUntilIdle()
        model.send("Primeira")
        model.send("Duplicada")
        advanceUntilIdle()
        assertEquals(listOf("Primeira"), history.messages.values.filter { it.role == "user" }.map { it.content })
    }

    @Test fun stalledFirebaseWriteOffersRetryInsteadOfLoadingForever() = runTest(dispatcher) {
        val store = object : ChatHistoryStore {
            override suspend fun load() = emptyList<ChatMessage>()
            override fun newMessage(text: String) = error("Sem envio")
            override suspend fun save(message: ChatMessage): ChatMessage = awaitCancellation()
        }
        val model = ChatViewModel(ChatResponder { "Olá" }, store)
        advanceUntilIdle()
        assertFalse(model.state.value.loading)
        assertNotNull(model.state.value.error)
        assertFalse(model.state.value.canSend)
    }
}
