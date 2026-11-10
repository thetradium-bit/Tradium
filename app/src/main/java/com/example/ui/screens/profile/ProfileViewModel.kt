package com.example.ui.screens.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthState
import com.example.data.repository.DemoTradingRepository
import com.example.domain.model.TraderProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val notifyNewSignals: Boolean = true,
    val notifyTpHits: Boolean = true,
    val notifySlAlerts: Boolean = true,
    val notifyCommunityAlpha: Boolean = false,
    val isGeminiConnected: Boolean = true,
    val showAuthDialog: Boolean = false,
    val authLoading: Boolean = false,
    val authError: String? = null,
    val message: String? = null
)

class ProfileViewModel(
    private val demoTradingRepository: DemoTradingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val profile: StateFlow<TraderProfile> = demoTradingRepository.traderProfile

    val authState: StateFlow<AuthState> = authRepository.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthState(user = authRepository.currentUser, isAuthenticated = authRepository.currentUser != null)
        )

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun setShowAuthDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAuthDialog = show, authError = null)
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(authLoading = true, authError = null)
            val result = authRepository.signInWithGoogle(context)
            result.onSuccess { user ->
                _uiState.value = _uiState.value.copy(
                    authLoading = false,
                    showAuthDialog = false,
                    message = "Welcome, ${user.displayName ?: user.email}!"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    authLoading = false,
                    authError = err.localizedMessage ?: "Google Sign-In failed"
                )
            }
        }
    }

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(authLoading = true, authError = null)
            val result = authRepository.signInWithEmail(email, pass)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    authLoading = false,
                    showAuthDialog = false,
                    message = "Successfully signed in as ${it.email}!"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    authLoading = false,
                    authError = err.localizedMessage ?: "Sign-in failed. Please check credentials."
                )
            }
        }
    }

    fun registerWithEmail(email: String, pass: String, username: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(authLoading = true, authError = null)
            val result = authRepository.registerWithEmail(email, pass, username)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    authLoading = false,
                    showAuthDialog = false,
                    message = "Account created! Welcome to Tradium."
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    authLoading = false,
                    authError = err.localizedMessage ?: "Registration failed."
                )
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            authRepository.sendPasswordReset(email)
            _uiState.value = _uiState.value.copy(message = "Password reset instructions sent to $email")
        }
    }

    fun signOut(context: Context? = null) {
        viewModelScope.launch {
            authRepository.signOut(context)
            _uiState.value = _uiState.value.copy(message = "Signed out successfully")
        }
    }

    fun toggleNotifyNewSignals(value: Boolean) {
        _uiState.value = _uiState.value.copy(notifyNewSignals = value)
    }

    fun toggleNotifyTpHits(value: Boolean) {
        _uiState.value = _uiState.value.copy(notifyTpHits = value)
    }

    fun toggleNotifySlAlerts(value: Boolean) {
        _uiState.value = _uiState.value.copy(notifySlAlerts = value)
    }

    fun toggleNotifyCommunityAlpha(value: Boolean) {
        _uiState.value = _uiState.value.copy(notifyCommunityAlpha = value)
    }

    fun resetDemoAccount() {
        viewModelScope.launch {
            demoTradingRepository.resetAccount()
            _uiState.value = _uiState.value.copy(message = "Demo account reset to $10,000 USDT!")
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
