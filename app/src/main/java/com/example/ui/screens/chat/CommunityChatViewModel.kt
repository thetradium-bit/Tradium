package com.example.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ChatRepository
import com.example.domain.model.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CommunityChatUiState(
    val currentChannel: String = "general",
    val inputMessage: String = "",
    val signalTag: String? = null
)

class CommunityChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityChatUiState())
    val uiState: StateFlow<CommunityChatUiState> = _uiState.asStateFlow()

    val bullishVotes: StateFlow<Int> = chatRepository.bullishVotes
    val bearishVotes: StateFlow<Int> = chatRepository.bearishVotes

    val messages: StateFlow<List<ChatMessage>> = _uiState
        .flatMapLatest { state ->
            chatRepository.getMessages(state.currentChannel)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectChannel(channel: String) {
        _uiState.value = _uiState.value.copy(currentChannel = channel)
    }

    fun setInputMessage(text: String) {
        _uiState.value = _uiState.value.copy(inputMessage = text)
    }

    fun setSignalTag(tag: String?) {
        _uiState.value = _uiState.value.copy(signalTag = tag)
    }

    fun sendMessage() {
        val text = _uiState.value.inputMessage.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            chatRepository.sendMessage(
                channel = _uiState.value.currentChannel,
                text = text,
                signalTag = _uiState.value.signalTag
            )
            _uiState.value = _uiState.value.copy(inputMessage = "", signalTag = null)
        }
    }

    fun likeMessage(id: String) {
        viewModelScope.launch {
            chatRepository.likeMessage(id)
        }
    }

    fun voteSentiment(isBullish: Boolean) {
        viewModelScope.launch {
            chatRepository.voteSentiment(isBullish)
        }
    }
}
