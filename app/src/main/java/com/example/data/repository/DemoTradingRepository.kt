package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.DemoTradeEntity
import com.example.domain.model.DemoTrade
import com.example.domain.model.TradeSide
import com.example.domain.model.TradeStatus
import com.example.domain.model.TraderProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

interface DemoTradingRepository {
    val traderProfile: StateFlow<TraderProfile>
    fun getOpenTrades(): Flow<List<DemoTrade>>
    fun getClosedTrades(): Flow<List<DemoTrade>>
    suspend fun openTrade(
        pair: String,
        side: TradeSide,
        entryPrice: Double,
        leverage: Int,
        marginUsdt: Double,
        takeProfit: Double,
        stopLoss: Double,
        signalId: String? = null
    ): DemoTrade
    suspend fun closeTrade(tradeId: String, currentMarketPrice: Double)
    suspend fun resetAccount()
}

class DemoTradingRepositoryImpl(
    private val database: AppDatabase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : DemoTradingRepository {

    private val demoTradeDao = database.demoTradeDao()

    private val _traderProfile = MutableStateFlow(
        TraderProfile(
            username = "AlphaTrader_Pro",
            accountTier = "VIP Elite Diamond",
            virtualBalance = 10000.0,
            initialBalance = 10000.0,
            totalTrades = 0,
            winRate = 80.0,
            bestTradePnl = 0.0,
            totalPnl = 0.0
        )
    )
    override val traderProfile: StateFlow<TraderProfile> = _traderProfile.asStateFlow()

    init {
        scope.launch {
            seedSampleTradesIfEmpty()
            recalculateProfile()
        }
    }

    override fun getOpenTrades(): Flow<List<DemoTrade>> {
        return demoTradeDao.getOpenTrades().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getClosedTrades(): Flow<List<DemoTrade>> {
        return demoTradeDao.getClosedTrades().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun openTrade(
        pair: String,
        side: TradeSide,
        entryPrice: Double,
        leverage: Int,
        marginUsdt: Double,
        takeProfit: Double,
        stopLoss: Double,
        signalId: String?
    ): DemoTrade {
        val newTrade = DemoTrade(
            id = UUID.randomUUID().toString(),
            signalId = signalId,
            pair = pair,
            side = side,
            entryPrice = entryPrice,
            exitPrice = null,
            leverage = leverage,
            marginUsdt = marginUsdt,
            takeProfit = takeProfit,
            stopLoss = stopLoss,
            status = TradeStatus.OPEN,
            openedAt = System.currentTimeMillis(),
            closedAt = null,
            realizedPnl = 0.0
        )

        // Deduct margin from balance
        val currentBalance = _traderProfile.value.virtualBalance
        val updatedBalance = (currentBalance - marginUsdt).coerceAtLeast(0.0)
        _traderProfile.value = _traderProfile.value.copy(virtualBalance = updatedBalance)

        demoTradeDao.insertTrade(DemoTradeEntity.fromDomain(newTrade))
        return newTrade
    }

    override suspend fun closeTrade(tradeId: String, currentMarketPrice: Double) {
        val allTrades = demoTradeDao.getAllTrades()
        // Find trade
        scope.launch {
            val openTradesEntities = demoTradeDao.getOpenTrades()
            // To be accurate, we update directly
        }
    }

    suspend fun executeClose(trade: DemoTrade, exitPrice: Double, reason: TradeStatus = TradeStatus.CLOSED_MANUAL) {
        val pnl = trade.calculateUnrealizedPnl(exitPrice)
        val closedTrade = trade.copy(
            exitPrice = exitPrice,
            status = reason,
            closedAt = System.currentTimeMillis(),
            realizedPnl = (pnl * 100).toLong() / 100.0
        )

        // Return margin + realized PnL back to balance
        val currentBalance = _traderProfile.value.virtualBalance
        val newBalance = currentBalance + trade.marginUsdt + pnl
        _traderProfile.value = _traderProfile.value.copy(virtualBalance = (newBalance * 100).toLong() / 100.0)

        demoTradeDao.updateTrade(DemoTradeEntity.fromDomain(closedTrade))
        recalculateProfile()
    }

    override suspend fun resetAccount() {
        _traderProfile.value = TraderProfile(
            username = "AlphaTrader_Pro",
            accountTier = "VIP Elite Diamond",
            virtualBalance = 10000.0,
            initialBalance = 10000.0,
            totalTrades = 0,
            winRate = 0.0,
            bestTradePnl = 0.0,
            totalPnl = 0.0
        )
    }

    private suspend fun recalculateProfile() {
        // compute stats from closed trades
    }

    private suspend fun seedSampleTradesIfEmpty() {
        val sampleTrades = listOf(
            DemoTrade(
                id = "trade-001",
                signalId = "sig-101",
                pair = "BTC/USDT",
                side = TradeSide.LONG,
                entryPrice = 95800.0,
                exitPrice = null,
                leverage = 10,
                marginUsdt = 500.0,
                takeProfit = 99200.0,
                stopLoss = 94200.0,
                status = TradeStatus.OPEN,
                openedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
                closedAt = null
            ),
            DemoTrade(
                id = "trade-002",
                signalId = "sig-103",
                pair = "SOL/USDT",
                side = TradeSide.LONG,
                entryPrice = 191.20,
                exitPrice = null,
                leverage = 15,
                marginUsdt = 300.0,
                takeProfit = 208.00,
                stopLoss = 184.50,
                status = TradeStatus.OPEN,
                openedAt = System.currentTimeMillis() - 1000 * 60 * 45,
                closedAt = null
            ),
            DemoTrade(
                id = "trade-003",
                signalId = "sig-102",
                pair = "ETH/USDT",
                side = TradeSide.LONG,
                entryPrice = 2710.0,
                exitPrice = 2785.0,
                leverage = 10,
                marginUsdt = 400.0,
                takeProfit = 2780.0,
                stopLoss = 2640.0,
                status = TradeStatus.CLOSED_TP,
                openedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 12,
                closedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 8,
                realizedPnl = 110.70
            ),
            DemoTrade(
                id = "trade-004",
                signalId = "sig-105",
                pair = "AVAX/USDT",
                side = TradeSide.SHORT,
                entryPrice = 36.80,
                exitPrice = 34.20,
                leverage = 5,
                marginUsdt = 300.0,
                takeProfit = 34.20,
                stopLoss = 37.90,
                status = TradeStatus.CLOSED_TP,
                openedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 24,
                closedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 18,
                realizedPnl = 105.98
            )
        )
        sampleTrades.forEach { trade ->
            demoTradeDao.insertTrade(DemoTradeEntity.fromDomain(trade))
        }
    }
}
