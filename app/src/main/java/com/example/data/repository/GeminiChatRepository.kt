package com.example.data.repository

import android.util.Log
import com.example.domain.model.ChatCategory
import com.example.domain.model.GeminiChatMessage
import com.example.domain.model.MessageRole
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

private const val TAG = "GeminiFinanceChat"

interface GeminiChatRepository {
    suspend fun streamChatMessage(
        category: ChatCategory,
        history: List<GeminiChatMessage>,
        userMessage: String
    ): Flow<String>
}

class GeminiChatRepositoryImpl : GeminiChatRepository {

    // Helper map of lazily evaluated generative models with role-specific system instructions
    // gemini-3.8-flash is the recommended frontier-tier general use model for rich finance & crypto reasoning
    private fun getModelForCategory(category: ChatCategory) =
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(category.systemInstruction)
            }
        )

    override suspend fun streamChatMessage(
        category: ChatCategory,
        history: List<GeminiChatMessage>,
        userMessage: String
    ): Flow<String> = flow {
        val model = getModelForCategory(category)

        // Convert domain chat history to Firebase content objects
        val formattedHistory = history.mapNotNull { msg ->
            when (msg.role) {
                MessageRole.USER -> content("user") { text(msg.text) }
                MessageRole.MODEL -> if (msg.text.isNotBlank()) content("model") { text(msg.text) } else null
            }
        }

        val chat = model.startChat(history = formattedHistory)

        try {
            // Realistic 60-second coroutine timeout for streaming response
            withTimeout(60_000L) {
                chat.sendMessageStream(userMessage).collect { chunk ->
                    val chunkText = chunk.text.orEmpty()
                    if (chunkText.isNotEmpty()) {
                        emit(chunkText)
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Chat streaming timed out", e)
            throw IllegalStateException("AI response timed out after 60 seconds. Please retry.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini chat streaming failed", e)
            throw e
        }
    }
}
