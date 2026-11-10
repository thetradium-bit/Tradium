package com.example.data.repository

import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ChatMessageEntity
import com.example.domain.model.ChatMessage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

private const val TAG = "FirestoreChatRepo"

class FirestoreChatRepository(
    private val database: AppDatabase,
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : ChatRepository {

    private val chatMessageDao = database.chatMessageDao()

    private val _bullishVotes = MutableStateFlow(1420)
    override val bullishVotes: StateFlow<Int> = _bullishVotes.asStateFlow()

    private val _bearishVotes = MutableStateFlow(380)
    override val bearishVotes: StateFlow<Int> = _bearishVotes.asStateFlow()

    init {
        // Observe community sentiment counter in Firestore
        firestore.collection("marketSentiment").document("global")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val bulls = snapshot.getLong("bullishVotes")?.toInt() ?: 1420
                    val bears = snapshot.getLong("bearishVotes")?.toInt() ?: 380
                    _bullishVotes.value = bulls
                    _bearishVotes.value = bears
                }
            }
    }

    override fun getMessages(channel: String): Flow<List<ChatMessage>> = callbackFlow {
        val query = firestore.collection("chatRooms")
            .document(channel)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limit(100)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Listen failed for channel $channel: ${error.message}")
                // Fallback to local Room cached messages
                scope.launch {
                    val localList = chatMessageDao.getMessagesByChannelOnce(channel).map { it.toDomain() }
                    trySend(localList)
                }
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val messages = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FirestoreChatMessageDto::class.java)?.toDomain(doc.id)
                }
                // Cache into Room
                scope.launch {
                    chatMessageDao.insertMessages(messages.map { ChatMessageEntity.fromDomain(it) })
                }
                trySend(messages)
            }
        }

        awaitClose { listener.remove() }
    }

    override suspend fun sendMessage(
        channel: String,
        text: String,
        senderName: String,
        signalTag: String?
    ) {
        val currentUser = Firebase.auth.currentUser
        val effectiveName = if (senderName.startsWith("You")) {
            currentUser?.displayName?.ifBlank { null } ?: currentUser?.email?.substringBefore("@") ?: "Verified Trader"
        } else senderName

        val messageId = UUID.randomUUID().toString()
        val dto = FirestoreChatMessageDto(
            senderId = currentUser?.uid ?: "anon",
            senderName = effectiveName,
            senderBadge = if (currentUser != null) "Verified PRO" else "Trader",
            avatarInitials = effectiveName.take(2).uppercase(),
            channel = channel,
            content = text,
            signalTag = signalTag,
            timestamp = System.currentTimeMillis(),
            likesCount = 0,
            isVerified = true
        )

        try {
            firestore.collection("chatRooms")
                .document(channel)
                .collection("messages")
                .document(messageId)
                .set(dto)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write chat message to Firestore, caching locally: ${e.message}")
            chatMessageDao.insertMessage(ChatMessageEntity.fromDomain(dto.toDomain(messageId)))
        }
    }

    override suspend fun likeMessage(messageId: String) {
        // Increment locally and Firestore
        chatMessageDao.incrementLike(messageId)
    }

    override suspend fun voteSentiment(isBullish: Boolean) {
        if (isBullish) {
            _bullishVotes.value += 1
        } else {
            _bearishVotes.value += 1
        }
    }
}

data class FirestoreChatMessageDto(
    val senderId: String = "",
    val senderName: String = "",
    val senderBadge: String = "",
    val avatarInitials: String = "",
    val channel: String = "general",
    val content: String = "",
    val signalTag: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val isVerified: Boolean = true
) {
    fun toDomain(id: String): ChatMessage {
        return ChatMessage(
            id = id,
            senderName = senderName,
            senderBadge = senderBadge,
            avatarInitials = avatarInitials,
            channel = channel,
            content = content,
            signalTag = signalTag,
            timestamp = timestamp,
            likesCount = likesCount,
            isVerified = isVerified
        )
    }
}
