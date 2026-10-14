package com.fabio.eagleeyes.chat

import com.fabio.eagleeyes.global.FirebaseConfig
import com.google.firebase.database.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class ChatMessage(val id: String = "", val role: String = "", val content: String = "", val createdAt: Long = 0)

interface ChatHistoryStore {
    fun newMessage(text: String): ChatMessage
    suspend fun save(message: ChatMessage): ChatMessage
}

class ChatHistoryRepository : ChatHistoryStore {
    private val uid = FirebaseConfig.getAuth().currentUser?.uid.orEmpty()
    private val db = FirebaseConfig.getDatabase()
    private val messages get() = db.child("chatbox").child(uid).child("mensagens")

    private fun checkSession() {
        check(uid.isNotBlank() && FirebaseConfig.getAuth().currentUser?.uid == uid) { "Entre novamente na conta do jovem para conversar." }
    }

    override fun newMessage(text: String): ChatMessage {
        checkSession()
        return ChatMessage(id = requireNotNull(messages.push().key), role = "user", content = text)
    }

    override suspend fun save(message: ChatMessage): ChatMessage {
        checkSession()
        require(message.role in listOf("user", "assistant") && message.content.isNotBlank())
        return suspendCancellableCoroutine { continuation ->
            // Chaves estáveis e escrita imutável permitem repetir após falhas sem duplicar mensagens.
            messages.child(message.id).runTransaction(object : Transaction.Handler {
                override fun doTransaction(data: MutableData): Transaction.Result {
                    if (data.value == null) data.value = mapOf(
                        "id" to message.id, "role" to message.role, "content" to message.content,
                        "createdAt" to ServerValue.TIMESTAMP
                    )
                    return Transaction.success(data)
                }
                override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                    if (!continuation.isActive) return
                    try {
                        checkSession()
                        if (error != null) throw error.toException()
                        check(committed) { "A gravação foi interrompida." }
                        val saved = requireNotNull(snapshot?.getValue(ChatMessage::class.java))
                        continuation.resume(saved.copy(id = message.id))
                    } catch (failure: Exception) { continuation.resumeWithException(failure) }
                }
            }, false)
        }
    }
}
