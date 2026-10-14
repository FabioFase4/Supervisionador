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
        var failRole: String? = null
        var failAfterCommit = false
        private var next = 0
        override fun newMessage(text: String) = ChatMessage("user-${++next}", "user", text)
        override suspend fun save(message: ChatMessage): ChatMessage {
            if (failRole == message.role && !failAfterCommit) error("Gravação recusada")
            val saved = messages.getOrPut(message.id) { message.copy(createdAt = 100) }
            if (failRole == message.role) error("Confirmação perdida")
            return saved
        }
    }

    @Test fun startsFreshWithoutReadingOrChangingTheStoredConversation() = runTest(dispatcher) {
        val history = MemoryHistory().apply { messages["old"] = ChatMessage("old", "user", "Conversa antiga") }
        val model = ChatViewModel(ChatResponder { messages ->
            assertTrue(messages.isEmpty())
            "Mensagem do dia"
        }, history)
        advanceUntilIdle()
        assertEquals("Mensagem do dia", model.state.value.messages.single().content)
        assertEquals(listOf("old"), history.messages.keys.toList())
        assertTrue(model.state.value.canSend)
    }

    @Test fun displaysDailyOpeningWithoutAnyFirebaseOperation() = runTest(dispatcher) {
        val history = object : ChatHistoryStore {
            override fun newMessage(text: String): ChatMessage = error("Não deve acessar o Firebase")
            override suspend fun save(message: ChatMessage): ChatMessage = error("Permissão negada")
        }
        val model = ChatViewModel(ChatResponder { "Olá" }, history)
        advanceUntilIdle()
        assertEquals("Olá", model.state.value.messages.single().content)
        assertNull(model.state.value.error)
        assertTrue(model.state.value.canSend)
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
        val history = MemoryHistory()
        var requests = 0
        val model = ChatViewModel(ChatResponder { messages ->
            requests++
            if (messages.isNotEmpty()) {
                history.failRole = "assistant"
                history.failAfterCommit = true
            }
            "Olá"
        }, history)
        advanceUntilIdle()
        model.send("Meu dia")
        advanceUntilIdle()
        assertNotNull(model.state.value.error)
        history.failRole = null
        model.retry()
        advanceUntilIdle()
        assertEquals(2, requests)
        assertEquals(3, history.messages.size)
        assertTrue(model.state.value.canSend)
    }

    @Test fun retriesFailedDailyOpeningWithoutSavingAnything() = runTest(dispatcher) {
        val history = MemoryHistory()
        var fail = true
        val model = ChatViewModel(ChatResponder {
            if (fail) throw ChatException("Sem conexão com a IA")
            "Olá"
        }, history)
        advanceUntilIdle()
        assertNotNull(model.state.value.error)
        assertTrue(model.state.value.messages.isEmpty())
        fail = false
        model.retry()
        advanceUntilIdle()
        assertEquals("Olá", model.state.value.messages.single().content)
        assertTrue(history.messages.isEmpty())
        assertTrue(model.state.value.canSend)
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
            override fun newMessage(text: String) = ChatMessage("user-1", "user", text)
            override suspend fun save(message: ChatMessage): ChatMessage = awaitCancellation()
        }
        val model = ChatViewModel(ChatResponder { "Olá" }, store)
        advanceUntilIdle()
        assertTrue(model.state.value.canSend)
        model.send("Meu dia")
        advanceUntilIdle()
        assertFalse(model.state.value.loading)
        assertNotNull(model.state.value.error)
        assertFalse(model.state.value.canSend)
    }

    @Test fun savesBothSidesButStartsANewConversationWhenReopened() = runTest(dispatcher) {
        val history = MemoryHistory()
        val model = ChatViewModel(ChatResponder { "Resposta da IA" }, history)
        advanceUntilIdle()
        repeat(55) { index ->
            model.send("Mensagem $index")
            advanceUntilIdle()
        }
        assertEquals(55, history.messages.values.count { it.role == "user" })
        assertEquals(56, history.messages.values.count { it.role == "assistant" })
        assertTrue(history.messages.values.all { it.createdAt > 0 })
        val reopened = ChatViewModel(ChatResponder { messages ->
            assertTrue(messages.isEmpty())
            "Nova abertura"
        }, history)
        advanceUntilIdle()
        assertEquals("Nova abertura", reopened.state.value.messages.single().content)
        assertNotEquals(model.state.value.messages.first().id, reopened.state.value.messages.first().id)
        assertEquals(111, history.messages.size)
        assertTrue(reopened.state.value.canSend)
    }
}
