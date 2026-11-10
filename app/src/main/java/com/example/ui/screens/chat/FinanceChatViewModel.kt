package com.example.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.GeminiChatRepository
import com.example.domain.model.ChatCategory
import com.example.domain.model.GeminiChatMessage
import com.example.domain.model.MessageRole
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class FinanceChatUiState(
    val selectedCategory: ChatCategory = ChatCategory.CRYPTO_MARKET,
    val messagesByCategory: Map<ChatCategory, List<GeminiChatMessage>> = emptyMap(),
    val inputText: String = "",
    val isStreaming: Boolean = false,
    val currentStreamingId: String? = null,
    val errorMessage: String? = null
)

class FinanceChatViewModel(
    private val geminiChatRepository: GeminiChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinanceChatUiState())
    val uiState: StateFlow<FinanceChatUiState> = _uiState.asStateFlow()

    private var activeStreamJob: Job? = null

    init {
        // Initialize welcoming guidance messages for each category
        val initialMap = ChatCategory.entries.associateWith { category ->
            listOf(
                GeminiChatMessage(
                    id = "welcome_${category.id}",
                    role = MessageRole.MODEL,
                    text = "Welcome to Tradium's **${category.title}** AI Assistant.\n\n" +
                            "${category.description}.\n\n" +
                            "Ask any question below or pick a suggested topic to get started!"
                )
            )
        }
        _uiState.value = _uiState.value.copy(messagesByCategory = initialMap)
    }

    fun selectCategory(category: ChatCategory) {
        if (_uiState.value.selectedCategory == category) return
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            errorMessage = null
        )
    }

    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun sendPrompt(promptText: String) {
        val trimmed = promptText.trim()
        if (trimmed.isEmpty() || _uiState.value.isStreaming) return

        val category = _uiState.value.selectedCategory
        val currentCategoryMessages = _uiState.value.messagesByCategory[category].orEmpty()

        val userMessage = GeminiChatMessage(
            id = UUID.randomUUID().toString(),
            role = MessageRole.USER,
            text = trimmed
        )

        val streamingMessageId = UUID.randomUUID().toString()
        val initialAssistantMessage = GeminiChatMessage(
            id = streamingMessageId,
            role = MessageRole.MODEL,
            text = "",
            isStreaming = true
        )

        val updatedList = currentCategoryMessages + userMessage + initialAssistantMessage
        val updatedMap = _uiState.value.messagesByCategory.toMutableMap()
        updatedMap[category] = updatedList

        _uiState.value = _uiState.value.copy(
            messagesByCategory = updatedMap,
            inputText = "",
            isStreaming = true,
            currentStreamingId = streamingMessageId,
            errorMessage = null
        )

        // Launch streaming inference coroutine
        activeStreamJob?.cancel()
        activeStreamJob = viewModelScope.launch {
            val accumulated = StringBuilder()
            try {
                // Pass prior completed messages as history (excluding the pending placeholder)
                val history = currentCategoryMessages.filter { it.text.isNotBlank() }

                geminiChatRepository.streamChatMessage(
                    category = category,
                    history = history,
                    userMessage = trimmed
                ).collect { chunk ->
                    accumulated.append(chunk)
                    updateStreamingMessage(category, streamingMessageId, accumulated.toString(), isDone = false)
                }

                // Finalize stream successfully
                val finalText = if (accumulated.isBlank()) "I analyzed your request, but could not produce a response. Please try rephrasing." else accumulated.toString()
                updateStreamingMessage(category, streamingMessageId, finalText, isDone = true)

                _uiState.value = _uiState.value.copy(
                    isStreaming = false,
                    currentStreamingId = null
                )
            } catch (e: CancellationException) {
                // Normal coroutine cancellation
                throw e
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Failed to generate AI response. Please check network/App Check."
                val errorText = accumulated.toString().ifEmpty {
                    "⚠️ **AI Assistant Encountered An Issue**\n\n$errorMsg\n\n*Tap retry or pick another prompt.*"
                }

                updateStreamingMessage(category, streamingMessageId, errorText, isDone = true, error = errorMsg)

                _uiState.value = _uiState.value.copy(
                    isStreaming = false,
                    currentStreamingId = null,
                    errorMessage = errorMsg
                )
            }
        }
    }

    private fun updateStreamingMessage(
        category: ChatCategory,
        messageId: String,
        newText: String,
        isDone: Boolean,
        error: String? = null
    ) {
        val currentCategoryMessages = _uiState.value.messagesByCategory[category].orEmpty()
        val updatedList = currentCategoryMessages.map { msg ->
            if (msg.id == messageId) {
                msg.copy(
                    text = newText,
                    isStreaming = !isDone,
                    error = error
                )
            } else {
                msg
            }
        }
        val updatedMap = _uiState.value.messagesByCategory.toMutableMap()
        updatedMap[category] = updatedList
        _uiState.value = _uiState.value.copy(messagesByCategory = updatedMap)
    }

    fun clearHistory() {
        val category = _uiState.value.selectedCategory
        val initialMessage = listOf(
            GeminiChatMessage(
                id = "reset_${category.id}_${System.currentTimeMillis()}",
                role = MessageRole.MODEL,
                text = "Session refreshed for **${category.title}**.\n\nAsk any question or pick a suggested topic below!"
            )
        )
        val updatedMap = _uiState.value.messagesByCategory.toMutableMap()
        updatedMap[category] = initialMessage
        _uiState.value = _uiState.value.copy(
            messagesByCategory = updatedMap,
            errorMessage = null
        )
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
