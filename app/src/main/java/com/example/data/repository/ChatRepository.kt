package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.ChatMessageEntity
import com.example.domain.model.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

interface ChatRepository {
    val bullishVotes: StateFlow<Int>
    val bearishVotes: StateFlow<Int>
    fun getMessages(channel: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(channel: String, text: String, senderName: String = "You (Alpha Pro)", signalTag: String? = null)
    suspend fun likeMessage(messageId: String)
    suspend fun voteSentiment(isBullish: Boolean)
}

class ChatRepositoryImpl(
    private val database: AppDatabase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : ChatRepository {

    private val chatMessageDao = database.chatMessageDao()

    private val _bullishVotes = MutableStateFlow(1420)
    override val bullishVotes: StateFlow<Int> = _bullishVotes.asStateFlow()

    private val _bearishVotes = MutableStateFlow(380)
    override val bearishVotes: StateFlow<Int> = _bearishVotes.asStateFlow()

    init {
        scope.launch {
            seedInitialChatMessagesIfEmpty()
        }
    }

    override fun getMessages(channel: String): Flow<List<ChatMessage>> {
        return chatMessageDao.getMessagesByChannel(channel).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun sendMessage(
        channel: String,
        text: String,
        senderName: String,
        signalTag: String?
    ) {
        val newMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderName = senderName,
            senderBadge = "VIP Trader",
            avatarInitials = senderName.take(2).uppercase(),
            channel = channel,
            content = text,
            signalTag = signalTag,
            timestamp = System.currentTimeMillis(),
            likesCount = 0,
            isVerified = true
        )
        chatMessageDao.insertMessage(ChatMessageEntity.fromDomain(newMsg))
    }

    override suspend fun likeMessage(messageId: String) {
        chatMessageDao.incrementLike(messageId)
    }

    override suspend fun voteSentiment(isBullish: Boolean) {
        if (isBullish) {
            _bullishVotes.value += 1
        } else {
            _bearishVotes.value += 1
        }
    }

    private suspend fun seedInitialChatMessagesIfEmpty() {
        val initialMessages = listOf(
            ChatMessage(
                id = "chat-01",
                senderName = "SatoshiWhale",
                senderBadge = "AI Master",
                avatarInitials = "SW",
                channel = "general",
                content = "BTC consolidating nicely above $96k support zone. 4H stochastic RSI just signaled fresh crossover. Long targets $99.2k intact! 🚀",
                signalTag = "BTC/USDT LONG",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 40,
                likesCount = 28,
                isVerified = true
            ),
            ChatMessage(
                id = "chat-02",
                senderName = "AlphaQuant_AI",
                senderBadge = "Algorithm Bot",
                avatarInitials = "AQ",
                channel = "general",
                content = "📊 Liquidation Heatmap Alert: $140M in short liquidations clustered between $97,800 - $98,400. High probability of a violent squeeze incoming.",
                signalTag = "HEATMAP UPDATE",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 25,
                likesCount = 42,
                isVerified = true
            ),
            ChatMessage(
                id = "chat-03",
                senderName = "CryptoViking",
                senderBadge = "Top Trader",
                avatarInitials = "CV",
                channel = "general",
                content = "SOL target 1 hit smoothly at $198.50! Moved SL to break-even ($191.20) for a completely risk-free ride to $208. Let winners run! 🔥",
                signalTag = "SOL/USDT TP1 HIT",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 12,
                likesCount = 19,
                isVerified = true
            ),
            ChatMessage(
                id = "chat-04",
                senderName = "WhaleWatcher_0x",
                senderBadge = "On-Chain Desk",
                avatarInitials = "WW",
                channel = "general",
                content = "Over 12,000 ETH just pulled off Coinbase Prime to private cold storage. Spot absorption remains relentless. Don't short the bull!",
                signalTag = "ON-CHAIN ALPHA",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 3,
                likesCount = 35,
                isVerified = true
            )
        )
        chatMessageDao.insertMessages(initialMessages.map { ChatMessageEntity.fromDomain(it) })
    }
}
