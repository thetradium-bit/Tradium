package com.example.ui.screens.trading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.DemoTradingRepository
import com.example.data.repository.DemoTradingRepositoryImpl
import com.example.domain.model.DemoTrade
import com.example.domain.model.TradeSide
import com.example.domain.model.TraderProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DemoTradingUiState(
    val selectedTab: Int = 0, // 0 = Open Positions, 1 = Trade History, 2 = Manual Order
    val orderPair: String = "BTC/USDT",
    val orderSide: TradeSide = TradeSide.LONG,
    val orderLeverage: Int = 10,
    val orderMargin: String = "500",
    val orderTp: String = "99200",
    val orderSl: String = "94200",
    val currentBtcPrice: Double = 96750.0,
    val currentEthPrice: Double = 2792.0,
    val currentSolPrice: Double = 195.80,
    val feedbackMessage: String? = null
)

class DemoTradingViewModel(
    private val demoTradingRepository: DemoTradingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DemoTradingUiState())
    val uiState: StateFlow<DemoTradingUiState> = _uiState.asStateFlow()

    val profile: StateFlow<TraderProfile> = demoTradingRepository.traderProfile

    val openTrades: StateFlow<List<DemoTrade>> = demoTradingRepository.getOpenTrades().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val closedTrades: StateFlow<List<DemoTrade>> = demoTradingRepository.getClosedTrades().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    fun setOrderSide(side: TradeSide) {
        _uiState.value = _uiState.value.copy(orderSide = side)
    }

    fun setOrderPair(pair: String) {
        val basePrice = when (pair) {
            "BTC/USDT" -> 96750.0
            "ETH/USDT" -> 2792.0
            "SOL/USDT" -> 195.80
            "NEAR/USDT" -> 6.88
            "AVAX/USDT" -> 34.10
            else -> 100.0
        }
        val isLong = _uiState.value.orderSide == TradeSide.LONG
        val tp = if (isLong) basePrice * 1.04 else basePrice * 0.96
        val sl = if (isLong) basePrice * 0.98 else basePrice * 1.02

        _uiState.value = _uiState.value.copy(
            orderPair = pair,
            orderTp = tp.toInt().toString(),
            orderSl = sl.toInt().toString()
        )
    }

    fun setOrderLeverage(lev: Int) {
        _uiState.value = _uiState.value.copy(orderLeverage = lev)
    }

    fun setOrderMargin(margin: String) {
        _uiState.value = _uiState.value.copy(orderMargin = margin)
    }

    fun setOrderTp(tp: String) {
        _uiState.value = _uiState.value.copy(orderTp = tp)
    }

    fun setOrderSl(sl: String) {
        _uiState.value = _uiState.value.copy(orderSl = sl)
    }

    fun submitNewOrder() {
        val pair = _uiState.value.orderPair
        val currentPrice = when (pair) {
            "BTC/USDT" -> _uiState.value.currentBtcPrice
            "ETH/USDT" -> _uiState.value.currentEthPrice
            "SOL/USDT" -> _uiState.value.currentSolPrice
            else -> 100.0
        }
        val margin = _uiState.value.orderMargin.toDoubleOrNull() ?: 500.0
        val tp = _uiState.value.orderTp.toDoubleOrNull() ?: (currentPrice * 1.04)
        val sl = _uiState.value.orderSl.toDoubleOrNull() ?: (currentPrice * 0.98)

        viewModelScope.launch {
            demoTradingRepository.openTrade(
                pair = pair,
                side = _uiState.value.orderSide,
                entryPrice = currentPrice,
                leverage = _uiState.value.orderLeverage,
                marginUsdt = margin,
                takeProfit = tp,
                stopLoss = sl
            )
            _uiState.value = _uiState.value.copy(
                selectedTab = 0, // Switch to Open Positions
                feedbackMessage = "Successfully opened ${pair} ${_uiState.value.orderSide} with $${margin} Margin!"
            )
        }
    }

    fun closePosition(trade: DemoTrade) {
        viewModelScope.launch {
            val currentPrice = when (trade.pair) {
                "BTC/USDT" -> _uiState.value.currentBtcPrice
                "ETH/USDT" -> _uiState.value.currentEthPrice
                "SOL/USDT" -> _uiState.value.currentSolPrice
                else -> trade.entryPrice * 1.02
            }
            if (demoTradingRepository is DemoTradingRepositoryImpl) {
                demoTradingRepository.executeClose(trade, currentPrice)
            }
            _uiState.value = _uiState.value.copy(
                feedbackMessage = "Position for ${trade.pair} closed successfully at $${currentPrice}!"
            )
        }
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(feedbackMessage = null)
    }
}
