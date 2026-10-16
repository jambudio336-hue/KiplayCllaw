package com.k1play.agentpro.memory

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationStore @Inject constructor(
    private val messageDao: MessageDao
) {
    private var currentConversationId = "default"

    suspend fun addMessage(role: String, content: String) {
        messageDao.insert(
            MessageEntity(
                role = role,
                content = content,
                conversationId = currentConversationId
            )
        )
    }

    suspend fun getRecentMessages(limit: Int = 50): List<MessageEntity> {
        return messageDao.getRecent(currentConversationId, limit).reversed()
    }

    suspend fun getAllMessages(): List<MessageEntity> = messageDao.getAll(currentConversationId)

    suspend fun getMessages(conversationId: String): List<MessageEntity> = messageDao.getAll(conversationId)

    suspend fun getConversationIds(): List<String> = messageDao.getConversationIds()

    suspend fun clearCurrentConversation() {
        messageDao.clearConversation(currentConversationId)
    }

    suspend fun messageCount(): Int {
        return messageDao.count(currentConversationId)
    }

    fun newConversation(id: String) {
        currentConversationId = id
    }

    fun currentId(): String = currentConversationId
}
