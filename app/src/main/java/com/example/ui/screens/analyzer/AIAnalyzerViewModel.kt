package com.example.ui.screens.analyzer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AIAnalyzerRepository
import com.example.data.repository.DemoTradingRepository
import com.example.domain.model.AIAnalysisResult
import com.example.domain.model.CandleData
import com.example.domain.model.TradeSide
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AIAnalyzerUiState(
    val selectedPair: String = "BTC/USDT",
    val selectedTimeframe: String = "4h",
    val selectedStrategy: String = "Smart Money Concepts (SMC)",
    val candles: List<CandleData> = emptyList(),
    val analysisResult: AIAnalysisResult? = null,
    val aiPatterns: List<com.example.domain.model.AIPatternOverlay> = emptyList(),
    val aiPriceLevels: List<com.example.domain.model.AIPriceLevel> = emptyList(),
    val selectedPattern: com.example.domain.model.AIPatternOverlay? = null,
    val isAnalyzing: Boolean = false,
    val tradeSuccessMessage: String? = null
)

class AIAnalyzerViewModel(
    private val analyzerRepository: AIAnalyzerRepository,
    private val demoTradingRepository: DemoTradingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIAnalyzerUiState())
    val uiState: StateFlow<AIAnalyzerUiState> = _uiState.asStateFlow()

    init {
        loadMarketDataAndAnalyze()
    }

    fun setPair(pair: String) {
        _uiState.value = _uiState.value.copy(selectedPair = pair)
        loadMarketDataAndAnalyze()
    }

    fun setTimeframe(tf: String) {
        _uiState.value = _uiState.value.copy(selectedTimeframe = tf)
        loadMarketDataAndAnalyze()
    }

    fun setStrategy(strategy: String) {
        _uiState.value = _uiState.value.copy(selectedStrategy = strategy)
        runAnalysis()
    }

    fun loadMarketDataAndAnalyze() {
        viewModelScope.launch {
            val candles = analyzerRepository.getCandleData(
                _uiState.value.selectedPair,
                _uiState.value.selectedTimeframe
            )
            _uiState.value = _uiState.value.copy(candles = candles)
            runAnalysis()
        }
    }

    fun runAnalysis() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzing = true)
            val result = analyzerRepository.runAiAnalysis(
                _uiState.value.selectedPair,
                _uiState.value.selectedTimeframe,
                _uiState.value.selectedStrategy
            )

            val candles = _uiState.value.candles
            val patterns = mutableListOf<com.example.domain.model.AIPatternOverlay>()
            val priceLevels = mutableListOf<com.example.domain.model.AIPriceLevel>()

            if (candles.isNotEmpty()) {
                val lastIdx = candles.size - 1
                // Extract Fair Value Gap (FVG) or Order Block in the last few candles
                val fvgStart = (lastIdx - 8).coerceAtLeast(0)
                val fvgEnd = (lastIdx - 2).coerceAtLeast(fvgStart)
                val candleA = candles[fvgStart]
                val candleB = candles[fvgEnd]

                val isBullish = result.bias.contains("BULLISH")

                patterns.add(
                    com.example.domain.model.AIPatternOverlay(
                        id = "pat_fvg_1",
                        type = if (isBullish) com.example.domain.model.AIPatternType.FAIR_VALUE_GAP else com.example.domain.model.AIPatternType.BEARISH_ORDER_BLOCK,
                        startCandleIndex = fvgStart,
                        endCandleIndex = fvgEnd,
                        topPrice = maxOf(candleA.high, candleB.high),
                        bottomPrice = minOf(candleA.low, candleB.low) * 0.995f,
                        label = result.patternDetected,
                        confidence = result.confidence,
                        targetPrice = result.recommendedTp1.toFloat(),
                        stopPrice = result.recommendedSl.toFloat(),
                        description = result.summary
                    )
                )

                // Add support & resistance & entry lines
                priceLevels.add(
                    com.example.domain.model.AIPriceLevel(
                        label = "Key Support",
                        price = result.keySupport.toFloat(),
                        levelType = com.example.domain.model.AILevelType.SUPPORT
                    )
                )
                priceLevels.add(
                    com.example.domain.model.AIPriceLevel(
                        label = "Key Resistance",
                        price = result.keyResistance.toFloat(),
                        levelType = com.example.domain.model.AILevelType.RESISTANCE
                    )
                )
                priceLevels.add(
                    com.example.domain.model.AIPriceLevel(
                        label = "AI Entry",
                        price = result.recommendedEntry.toFloat(),
                        levelType = com.example.domain.model.AILevelType.ENTRY
                    )
                )
                priceLevels.add(
                    com.example.domain.model.AIPriceLevel(
                        label = "TP 1",
                        price = result.recommendedTp1.toFloat(),
                        levelType = com.example.domain.model.AILevelType.TAKE_PROFIT
                    )
                )
            }

            _uiState.value = _uiState.value.copy(
                isAnalyzing = false,
                analysisResult = result,
                aiPatterns = patterns,
                aiPriceLevels = priceLevels,
                selectedPattern = patterns.firstOrNull()
            )
        }
    }

    fun selectPattern(pattern: com.example.domain.model.AIPatternOverlay?) {
        _uiState.value = _uiState.value.copy(selectedPattern = pattern)
    }

    fun executePaperTradeFromAnalysis() {
        val analysis = _uiState.value.analysisResult ?: return
        viewModelScope.launch {
            val isLong = analysis.bias.contains("BULLISH")
            demoTradingRepository.openTrade(
                pair = analysis.pair,
                side = if (isLong) TradeSide.LONG else TradeSide.SHORT,
                entryPrice = analysis.recommendedEntry,
                leverage = 10,
                marginUsdt = 500.0,
                takeProfit = analysis.recommendedTp1,
                stopLoss = analysis.recommendedSl,
                signalId = "ai-scan-${System.currentTimeMillis()}"
            )
            _uiState.value = _uiState.value.copy(
                tradeSuccessMessage = "Executed Paper Trade: ${analysis.pair} (Entry: $${analysis.recommendedEntry})!"
            )
        }
    }

    fun clearTradeSuccessMessage() {
        _uiState.value = _uiState.value.copy(tradeSuccessMessage = null)
    }
}
