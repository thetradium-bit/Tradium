package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.DemoTrade
import com.example.domain.model.TradeSide
import com.example.domain.model.TradeStatus

@Entity(tableName = "demo_trades")
data class DemoTradeEntity(
    @PrimaryKey val id: String,
    val signalId: String?,
    val pair: String,
    val side: String,
    val entryPrice: Double,
    val exitPrice: Double?,
    val leverage: Int,
    val marginUsdt: Double,
    val takeProfit: Double,
    val stopLoss: Double,
    val status: String,
    val openedAt: Long,
    val closedAt: Long?,
    val realizedPnl: Double
) {
    fun toDomain(): DemoTrade {
        return DemoTrade(
            id = id,
            signalId = signalId,
            pair = pair,
            side = try { TradeSide.valueOf(side) } catch (e: Exception) { TradeSide.LONG },
            entryPrice = entryPrice,
            exitPrice = exitPrice,
            leverage = leverage,
            marginUsdt = marginUsdt,
            takeProfit = takeProfit,
            stopLoss = stopLoss,
            status = try { TradeStatus.valueOf(status) } catch (e: Exception) { TradeStatus.OPEN },
            openedAt = openedAt,
            closedAt = closedAt,
            realizedPnl = realizedPnl
        )
    }

    companion object {
        fun fromDomain(trade: DemoTrade): DemoTradeEntity {
            return DemoTradeEntity(
                id = trade.id,
                signalId = trade.signalId,
                pair = trade.pair,
                side = trade.side.name,
                entryPrice = trade.entryPrice,
                exitPrice = trade.exitPrice,
                leverage = trade.leverage,
                marginUsdt = trade.marginUsdt,
                takeProfit = trade.takeProfit,
                stopLoss = trade.stopLoss,
                status = trade.status.name,
                openedAt = trade.openedAt,
                closedAt = trade.closedAt,
                realizedPnl = trade.realizedPnl
            )
        }
    }
}
