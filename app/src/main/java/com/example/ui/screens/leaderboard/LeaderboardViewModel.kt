package com.example.ui.screens.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.LeaderboardRepository
import com.example.domain.model.LeaderboardUser
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LeaderboardViewModel(
    private val leaderboardRepository: LeaderboardRepository
) : ViewModel() {

    val users: StateFlow<List<LeaderboardUser>> = leaderboardRepository.getLeaderboard().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun toggleFollow(userId: String) {
        viewModelScope.launch {
            leaderboardRepository.toggleFollow(userId)
        }
    }
}
