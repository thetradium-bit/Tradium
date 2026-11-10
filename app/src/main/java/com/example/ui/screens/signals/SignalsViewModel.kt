package com.example.ui.screens.signals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.DemoTradingRepository
import com.example.data.repository.SignalRepository
import com.example.domain.model.Signal
import com.example.domain.model.SignalType
import com.example.domain.model.TradeSide
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SignalFilter {
    ALL,
    LONGS,
    SHORTS,
    SPOT,
    HIGH_CONFIDENCE
}

data class SignalsUiState(
    val selectedFilter: SignalFilter = SignalFilter.ALL,
    val selectedAsset: String = "ALL",
    val selectedSignal: Signal? = null,
    val isGeneratingSignal: Boolean = false,
    val tradeSuccessMessage: String? = null
)

class SignalsViewModel(
    private val signalRepository: SignalRepository,
    private val demoTradingRepository: DemoTradingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignalsUiState())
    val uiState: StateFlow<SignalsUiState> = _uiState.asStateFlow()

    val filteredSignals: StateFlow<List<Signal>> = combine(
        signalRepository.getSignals(),
        _uiState
    ) { signals, state ->
        signals.filter { signal ->
            val matchesFilter = when (state.selectedFilter) {
                SignalFilter.ALL -> true
                SignalFilter.LONGS -> signal.type == SignalType.LONG
                SignalFilter.SHORTS -> signal.type == SignalType.SHORT
                SignalFilter.SPOT -> signal.type == SignalType.SPOT_BUY
                SignalFilter.HIGH_CONFIDENCE -> signal.confidenceScore >= 93
            }
            val matchesAsset = if (state.selectedAsset == "ALL") true else signal.assetSymbol == state.selectedAsset
            matchesFilter && matchesAsset
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setFilter(filter: SignalFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun setAssetFilter(asset: String) {
        _uiState.value = _uiState.value.copy(selectedAsset = asset)
    }

    fun selectSignal(signal: Signal?) {
        _uiState.value = _uiState.value.copy(selectedSignal = signal)
    }

    fun executePaperTrade(signal: Signal) {
        viewModelScope.launch {
            val isLong = signal.type == SignalType.LONG || signal.type == SignalType.SPOT_BUY
            val levInt = signal.leverage.replace("x", "").replace("Spot", "1").toIntOrNull() ?: 10
            demoTradingRepository.openTrade(
                pair = signal.pair,
                side = if (isLong) TradeSide.LONG else TradeSide.SHORT,
                entryPrice = signal.entryPrice,
                leverage = levInt,
                marginUsdt = 500.0,
                takeProfit = signal.targetPrice2,
                stopLoss = signal.stopLoss,
                signalId = signal.id
            )
            _uiState.value = _uiState.value.copy(
                tradeSuccessMessage = "Copied ${signal.pair} ${signal.type.name} to Paper Trading ($500 Margin)!"
            )
        }
    }

    fun clearTradeSuccessMessage() {
        _uiState.value = _uiState.value.copy(tradeSuccessMessage = null)
    }

    fun generateNewAiSignal(pair: String = "BTC/USDT") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeneratingSignal = true)
            val type = if ((0..1).random() == 0) SignalType.LONG else SignalType.SHORT
            val generated = signalRepository.generateNewAiSignal(pair, type)
            _uiState.value = _uiState.value.copy(
                isGeneratingSignal = false,
                selectedSignal = generated,
                tradeSuccessMessage = "Fresh AI Signal Generated for $pair!"
            )
        }
    }
}
