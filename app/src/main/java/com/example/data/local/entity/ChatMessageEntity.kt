package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.ChatMessage

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val senderName: String,
    val senderBadge: String,
    val avatarInitials: String,
    val channel: String,
    val content: String,
    val signalTag: String?,
    val timestamp: Long,
    val likesCount: Int,
    val isVerified: Boolean
) {
    fun toDomain(): ChatMessage {
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

    companion object {
        fun fromDomain(msg: ChatMessage): ChatMessageEntity {
            return ChatMessageEntity(
                id = msg.id,
                senderName = msg.senderName,
                senderBadge = msg.senderBadge,
                avatarInitials = msg.avatarInitials,
                channel = msg.channel,
                content = msg.content,
                signalTag = msg.signalTag,
                timestamp = msg.timestamp,
                likesCount = msg.likesCount,
                isVerified = msg.isVerified
            )
        }
    }
}
