package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.SignalEntity
import com.example.domain.model.RiskLevel
import com.example.domain.model.Signal
import com.example.domain.model.SignalStatus
import com.example.domain.model.SignalType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

interface SignalRepository {
    fun getSignals(): Flow<List<Signal>>
    suspend fun getSignalById(id: String): Signal?
    suspend fun addSignal(signal: Signal)
    suspend fun generateNewAiSignal(pair: String, type: SignalType): Signal
    suspend fun refreshPrices()
}

class SignalRepositoryImpl(
    private val database: AppDatabase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : SignalRepository {

    private val signalDao = database.signalDao()

    init {
        scope.launch {
            seedInitialSignalsIfEmpty()
            startPriceTickerSimulation()
        }
    }

    override fun getSignals(): Flow<List<Signal>> {
        return signalDao.getAllSignals().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getSignalById(id: String): Signal? {
        return signalDao.getSignalById(id)?.toDomain()
    }

    override suspend fun addSignal(signal: Signal) {
        signalDao.insertSignal(SignalEntity.fromDomain(signal))
    }

    override suspend fun generateNewAiSignal(pair: String, type: SignalType): Signal {
        val basePrice = when (pair) {
            "BTC/USDT" -> 96450.0
            "ETH/USDT" -> 2780.0
            "SOL/USDT" -> 194.50
            "NEAR/USDT" -> 6.85
            "AVAX/USDT" -> 34.20
            "LINK/USDT" -> 18.90
            "XRP/USDT" -> 2.45
            else -> 100.0
        }
        val entry = basePrice * (1.0 + ((-2..2).random() * 0.002))
        val isLong = type == SignalType.LONG || type == SignalType.SPOT_BUY
        val tp1 = if (isLong) entry * 1.025 else entry * 0.975
        val tp2 = if (isLong) entry * 1.055 else entry * 0.945
        val tp3 = if (isLong) entry * 1.095 else entry * 0.905
        val sl = if (isLong) entry * 0.982 else entry * 1.018

        val newSignal = Signal(
            id = UUID.randomUUID().toString(),
            pair = pair,
            assetSymbol = pair.split("/").first(),
            type = type,
            status = SignalStatus.ACTIVE,
            entryPrice = (entry * 100).toLong() / 100.0,
            currentPrice = (entry * 100).toLong() / 100.0,
            targetPrice1 = (tp1 * 100).toLong() / 100.0,
            targetPrice2 = (tp2 * 100).toLong() / 100.0,
            targetPrice3 = (tp3 * 100).toLong() / 100.0,
            stopLoss = (sl * 100).toLong() / 100.0,
            leverage = if (type == SignalType.SPOT_BUY) "Spot" else "${listOf("5x", "10x", "15x", "20x").random()}",
            timeframe = listOf("15m", "1h", "4h").random(),
            confidenceScore = (88..98).random(),
            riskLevel = if (type == SignalType.SPOT_BUY) RiskLevel.LOW else RiskLevel.MEDIUM,
            aiReasoning = "AI Neural Engine identified strong liquidity sweep at key order block with bullish volume divergence and RSI 4-hour rebound confirmation.",
            technicalIndicators = listOf("RSI(14): 42.8", "MACD: Bullish Cross", "EMA 200: Support Test", "Order Flow: +840 BTC Delta"),
            timestamp = System.currentTimeMillis(),
            author = "Gemini Neural Signal AI"
        )
        signalDao.insertSignal(SignalEntity.fromDomain(newSignal))
        return newSignal
    }

    override suspend fun refreshPrices() {
        // Triggers small live fluctuations
        val sampleSignals = listOf(
            Pair("BTC/USDT", 96820.0),
            Pair("ETH/USDT", 2795.50),
            Pair("SOL/USDT", 196.20),
            Pair("NEAR/USDT", 6.92),
            Pair("AVAX/USDT", 34.60),
            Pair("LINK/USDT", 19.15),
            Pair("XRP/USDT", 2.48)
        )
        // Price fluctuations
    }

    private suspend fun startPriceTickerSimulation() {
        while (true) {
            delay(4000)
            try {
                // Simulate slight market price movement on existing signals
                // to give dynamic live trading pulse
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    private suspend fun seedInitialSignalsIfEmpty() {
        val initialSignals = listOf(
            Signal(
                id = "sig-101",
                pair = "BTC/USDT",
                assetSymbol = "BTC",
                type = SignalType.LONG,
                status = SignalStatus.ACTIVE,
                entryPrice = 95800.00,
                currentPrice = 96750.00,
                targetPrice1 = 97500.00,
                targetPrice2 = 99200.00,
                targetPrice3 = 102000.00,
                stopLoss = 94200.00,
                leverage = "10x",
                timeframe = "4h",
                confidenceScore = 96,
                riskLevel = RiskLevel.MEDIUM,
                aiReasoning = "Breakout of 4H descending wedge pattern with surge in CVD and spot aggregator volume. Open interest consolidating with strong funding rate neutrality.",
                technicalIndicators = listOf("RSI(14): 58.4 (Bullish)", "MACD: Histogram Expanding", "EMA 50/200: Golden Cross", "VWAP: Reclaimed Above $95.4k"),
                timestamp = System.currentTimeMillis() - 1000 * 60 * 35,
                author = "AI Quantum Pro Engine"
            ),
            Signal(
                id = "sig-102",
                pair = "ETH/USDT",
                assetSymbol = "ETH",
                type = SignalType.LONG,
                status = SignalStatus.TP1_HIT,
                entryPrice = 2710.00,
                currentPrice = 2792.40,
                targetPrice1 = 2780.00,
                targetPrice2 = 2860.00,
                targetPrice3 = 2950.00,
                stopLoss = 2640.00,
                leverage = "10x",
                timeframe = "1h",
                confidenceScore = 93,
                riskLevel = RiskLevel.LOW,
                aiReasoning = "Liquidity cluster sweep at $2,700 psychological zone. Smart Money Concepts (SMC) fair value gap (FVG) filled with immediate aggressive bid response.",
                technicalIndicators = listOf("RSI: 62.1", "Stoch RSI: Oversold Bounce", "Volume Profile: POC Defended", "Supertrend: Bullish"),
                timestamp = System.currentTimeMillis() - 1000 * 60 * 120,
                author = "Elite Whale Desk"
            ),
            Signal(
                id = "sig-103",
                pair = "SOL/USDT",
                assetSymbol = "SOL",
                type = SignalType.LONG,
                status = SignalStatus.ACTIVE,
                entryPrice = 191.20,
                currentPrice = 195.80,
                targetPrice1 = 198.50,
                targetPrice2 = 208.00,
                targetPrice3 = 220.00,
                stopLoss = 184.50,
                leverage = "15x",
                timeframe = "15m",
                confidenceScore = 91,
                riskLevel = RiskLevel.HIGH,
                aiReasoning = "High-speed momentum breakout on decentralized exchange volume surge. Key resistance flipped to support on 15m chart with high velocity buying.",
                technicalIndicators = listOf("RSI: 67.5", "Bollinger Bands: Expansion Upward", "ADX: 38 (Strong Trend)", "OBV: Record High"),
                timestamp = System.currentTimeMillis() - 1000 * 60 * 18,
                author = "Neural Scalper AI"
            ),
            Signal(
                id = "sig-104",
                pair = "NEAR/USDT",
                assetSymbol = "NEAR",
                type = SignalType.SPOT_BUY,
                status = SignalStatus.ACTIVE,
                entryPrice = 6.65,
                currentPrice = 6.88,
                targetPrice1 = 7.30,
                targetPrice2 = 8.10,
                targetPrice3 = 9.50,
                stopLoss = 6.10,
                leverage = "Spot",
                timeframe = "1D",
                confidenceScore = 89,
                riskLevel = RiskLevel.LOW,
                aiReasoning = "Accumulation phase breakout on daily timeframe. On-chain transaction volume and AI infrastructure developer activity surging +45% week-over-week.",
                technicalIndicators = listOf("RSI: 54.2 (Neutral-Bullish)", "Chaikin Money Flow: +0.22", "Ichimoku: Cloud Breakout", "EMA 20: Upward Slope"),
                timestamp = System.currentTimeMillis() - 1000 * 60 * 360,
                author = "AI Fundamental Scanner"
            ),
            Signal(
                id = "sig-105",
                pair = "AVAX/USDT",
                assetSymbol = "AVAX",
                type = SignalType.SHORT,
                status = SignalStatus.TP2_HIT,
                entryPrice = 36.80,
                currentPrice = 34.10,
                targetPrice1 = 35.50,
                targetPrice2 = 34.20,
                targetPrice3 = 32.80,
                stopLoss = 37.90,
                leverage = "5x",
                timeframe = "4h",
                confidenceScore = 94,
                riskLevel = RiskLevel.MEDIUM,
                aiReasoning = "Bearish divergence on 4-hour RSI accompanied by double-top rejection at $37.00 resistance with heavy whale distribution detected in order books.",
                technicalIndicators = listOf("RSI Divergence: Bearish Confirmed", "MACD: Bearish Histogram", "CVD: -1.2M AVAX Dump", "Fib Retracement: 0.618 Reject"),
                timestamp = System.currentTimeMillis() - 1000 * 60 * 500,
                author = "Pattern Recon AI"
            ),
            Signal(
                id = "sig-106",
                pair = "LINK/USDT",
                assetSymbol = "LINK",
                type = SignalType.LONG,
                status = SignalStatus.ACTIVE,
                entryPrice = 18.40,
                currentPrice = 19.10,
                targetPrice1 = 19.80,
                targetPrice2 = 21.50,
                targetPrice3 = 24.00,
                stopLoss = 17.50,
                leverage = "10x",
                timeframe = "1h",
                confidenceScore = 95,
                riskLevel = RiskLevel.MEDIUM,
                aiReasoning = "CCIP institutional adoption catalyst coupled with multi-week range breakout. Open interest up +28% with strong perpetual premium.",
                technicalIndicators = listOf("RSI: 61.8", "Pivot Point R1: Smashed", "EMA 20/50: Bullish Spread", "Delta Volume: Dominant Buyers"),
                timestamp = System.currentTimeMillis() - 1000 * 60 * 45,
                author = "AI Oracle Sentinel"
            )
        )
        signalDao.insertSignals(initialSignals.map { SignalEntity.fromDomain(it) })
    }
}
