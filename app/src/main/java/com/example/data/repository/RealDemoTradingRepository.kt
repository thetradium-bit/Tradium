package com.example.data.repository

import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.DemoTradeEntity
import com.example.domain.model.DemoTrade
import com.example.domain.model.TradeSide
import com.example.domain.model.TradeStatus
import com.example.domain.model.TraderProfile
import com.example.domain.trading.TradingFormulas
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

private const val TAG = "RealDemoTradingRepo"

class RealDemoTradingRepository(
    private val database: AppDatabase,
    private val marketDataRepository: MarketDataRepository,
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : DemoTradingRepository {

    private val demoTradeDao = database.demoTradeDao()

    private val _traderProfile = MutableStateFlow(
        TraderProfile(
            username = "Trader",
            accountTier = "Verified Paper Trader",
            virtualBalance = 10000.0,
            initialBalance = 10000.0,
            totalTrades = 0,
            winRate = 0.0,
            bestTradePnl = 0.0,
            totalPnl = 0.0
        )
    )
    override val traderProfile: StateFlow<TraderProfile> = _traderProfile.asStateFlow()

    init {
        scope.launch {
            // Listen to auth profile if signed in
            val user = Firebase.auth.currentUser
            if (user != null) {
                firestore.collection("users").document(user.uid)
                    .addSnapshotListener { snapshot, _ ->
                        if (snapshot != null && snapshot.exists()) {
                            val balance = snapshot.getDouble("virtualBalance") ?: 10000.0
                            val initial = snapshot.getDouble("initialBalance") ?: 10000.0
                            val total = snapshot.getLong("totalTrades")?.toInt() ?: 0
                            val winRate = snapshot.getDouble("winRate") ?: 0.0
                            val bestPnl = snapshot.getDouble("bestTradePnl") ?: 0.0
                            val totalPnl = snapshot.getDouble("totalPnl") ?: 0.0
                            val username = snapshot.getString("username") ?: "Trader_${user.uid.take(4)}"

                            _traderProfile.value = TraderProfile(
                                username = username,
                                accountTier = snapshot.getString("accountTier") ?: "Verified Paper Trader",
                                virtualBalance = balance,
                                initialBalance = initial,
                                totalTrades = total,
                                winRate = winRate,
                                bestTradePnl = bestPnl,
                                totalPnl = totalPnl
                            )
                        }
                    }
            }
            recalculateProfile()
            startPriceWatcher()
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
        // Enforce valid balance check
        val currentBalance = _traderProfile.value.virtualBalance
        if (marginUsdt > currentBalance) {
            throw IllegalArgumentException("Insufficient virtual USDT balance! Available: $${currentBalance}")
        }

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

        // Deduct margin from available balance
        val updatedBalance = BigDecimal.valueOf(currentBalance)
            .subtract(BigDecimal.valueOf(marginUsdt))
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()

        _traderProfile.value = _traderProfile.value.copy(virtualBalance = updatedBalance)
        demoTradeDao.insertTrade(DemoTradeEntity.fromDomain(newTrade))

        // Sync with Firestore user trades
        syncTradeWithFirestore(newTrade, updatedBalance)

        return newTrade
    }

    override suspend fun closeTrade(tradeId: String, currentMarketPrice: Double) {
        val openEntities = demoTradeDao.getOpenTradesList()
        val target = openEntities.find { it.id == tradeId }?.toDomain() ?: return
        executeClose(target, currentMarketPrice, TradeStatus.CLOSED_MANUAL)
    }

    suspend fun executeClose(trade: DemoTrade, exitPrice: Double, status: TradeStatus) {
        val pnl = TradingFormulas.calculateFuturesPnl(
            side = trade.side,
            entryPrice = trade.entryPrice,
            currentPrice = exitPrice,
            leverage = trade.leverage,
            marginUsdt = trade.marginUsdt
        )

        val closedTrade = trade.copy(
            exitPrice = exitPrice,
            status = status,
            closedAt = System.currentTimeMillis(),
            realizedPnl = pnl
        )

        // Return original margin + realized PnL back into virtual wallet
        val updatedBalance = BigDecimal.valueOf(_traderProfile.value.virtualBalance)
            .add(BigDecimal.valueOf(trade.marginUsdt))
            .add(BigDecimal.valueOf(pnl))
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()

        _traderProfile.value = _traderProfile.value.copy(virtualBalance = updatedBalance)
        demoTradeDao.updateTrade(DemoTradeEntity.fromDomain(closedTrade))

        recalculateProfile()
        syncTradeWithFirestore(closedTrade, updatedBalance)
    }

    override suspend fun resetAccount() {
        val defaultBalance = 10000.0
        _traderProfile.value = TraderProfile(
            username = _traderProfile.value.username,
            accountTier = _traderProfile.value.accountTier,
            virtualBalance = defaultBalance,
            initialBalance = defaultBalance,
            totalTrades = 0,
            winRate = 0.0,
            bestTradePnl = 0.0,
            totalPnl = 0.0
        )
        val user = Firebase.auth.currentUser
        if (user != null) {
            val map = mapOf(
                "virtualBalance" to defaultBalance,
                "initialBalance" to defaultBalance,
                "totalTrades" to 0,
                "winRate" to 0.0,
                "bestTradePnl" to 0.0,
                "totalPnl" to 0.0
            )
            firestore.collection("users").document(user.uid).set(map, SetOptions.merge())
        }
    }

    private suspend fun recalculateProfile() {
        val closedTrades = demoTradeDao.getClosedTradesList().map { it.toDomain() }
        if (closedTrades.isEmpty()) return

        val total = closedTrades.size
        val winning = closedTrades.count { it.realizedPnl > 0 }
        val winRate = ((winning.toDouble() / total.toDouble()) * 1000.0).toLong() / 10.0
        val totalPnl = closedTrades.sumOf { it.realizedPnl }
        val bestPnl = closedTrades.maxOfOrNull { it.realizedPnl } ?: 0.0

        val current = _traderProfile.value
        _traderProfile.value = current.copy(
            totalTrades = total,
            winRate = winRate,
            totalPnl = (totalPnl * 100.0).toLong() / 100.0,
            bestTradePnl = (bestPnl * 100.0).toLong() / 100.0
        )

        val user = Firebase.auth.currentUser
        if (user != null) {
            val map = mapOf(
                "totalTrades" to total,
                "winRate" to winRate,
                "totalPnl" to _traderProfile.value.totalPnl,
                "bestTradePnl" to _traderProfile.value.bestTradePnl
            )
            firestore.collection("users").document(user.uid).set(map, SetOptions.merge())
        }
    }

    private fun syncTradeWithFirestore(trade: DemoTrade, newBalance: Double) {
        val user = Firebase.auth.currentUser ?: return
        scope.launch {
            try {
                firestore.collection("users").document(user.uid)
                    .collection("trades").document(trade.id)
                    .set(trade)
                    .await()

                firestore.collection("users").document(user.uid)
                    .update("virtualBalance", newBalance)
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed syncing trade to firestore: ${e.message}")
            }
        }
    }

    /**
     * Watches real market prices and evaluates TP / SL / Liquidation for open positions
     */
    private fun startPriceWatcher() {
        scope.launch {
            marketDataRepository.livePrices.collect { update ->
                val openTrades = demoTradeDao.getOpenTradesList().map { it.toDomain() }
                for (trade in openTrades) {
                    val cleanTradePair = trade.pair.replace("/", "").uppercase()
                    val cleanUpdateSymbol = update.symbol.replace("/", "").uppercase()
                    if (cleanTradePair == cleanUpdateSymbol) {
                        val currentPrice = update.price
                        // 1. Check Take Profit
                        if (trade.side == TradeSide.LONG && currentPrice >= trade.takeProfit) {
                            executeClose(trade, trade.takeProfit, TradeStatus.CLOSED_TP)
                        } else if (trade.side == TradeSide.SHORT && currentPrice <= trade.takeProfit) {
                            executeClose(trade, trade.takeProfit, TradeStatus.CLOSED_TP)
                        }
                        // 2. Check Stop Loss
                        else if (trade.side == TradeSide.LONG && currentPrice <= trade.stopLoss) {
                            executeClose(trade, trade.stopLoss, TradeStatus.CLOSED_SL)
                        } else if (trade.side == TradeSide.SHORT && currentPrice >= trade.stopLoss) {
                            executeClose(trade, trade.stopLoss, TradeStatus.CLOSED_SL)
                        }
                    }
                }
            }
        }
    }
}
