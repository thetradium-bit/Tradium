package com.example.domain.model

data class ChatMessage(
    val id: String,
    val senderName: String,
    val senderBadge: String, // "AI Master", "Whale Analyst", "Top Trader", "PRO"
    val avatarInitials: String,
    val channel: String,     // "general", "btc-alpha", "altcoin-gems", "ai-signals"
    val content: String,
    val signalTag: String? = null,
    val timestamp: Long,
    val likesCount: Int = 0,
    val isVerified: Boolean = true
)
