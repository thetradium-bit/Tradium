package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.RiskLevel
import com.example.domain.model.Signal
import com.example.domain.model.SignalStatus
import com.example.domain.model.SignalType

@Entity(tableName = "signals")
data class SignalEntity(
    @PrimaryKey val id: String,
    val pair: String,
    val assetSymbol: String,
    val type: String,
    val status: String,
    val entryPrice: Double,
    val currentPrice: Double,
    val targetPrice1: Double,
    val targetPrice2: Double,
    val targetPrice3: Double,
    val stopLoss: Double,
    val leverage: String,
    val timeframe: String,
    val confidenceScore: Int,
    val riskLevel: String,
    val aiReasoning: String,
    val technicalIndicators: List<String>,
    val timestamp: Long,
    val author: String
) {
    fun toDomain(): Signal {
        return Signal(
            id = id,
            pair = pair,
            assetSymbol = assetSymbol,
            type = try { SignalType.valueOf(type) } catch (e: Exception) { SignalType.LONG },
            status = try { SignalStatus.valueOf(status) } catch (e: Exception) { SignalStatus.ACTIVE },
            entryPrice = entryPrice,
            currentPrice = currentPrice,
            targetPrice1 = targetPrice1,
            targetPrice2 = targetPrice2,
            targetPrice3 = targetPrice3,
            stopLoss = stopLoss,
            leverage = leverage,
            timeframe = timeframe,
            confidenceScore = confidenceScore,
            riskLevel = try { RiskLevel.valueOf(riskLevel) } catch (e: Exception) { RiskLevel.MEDIUM },
            aiReasoning = aiReasoning,
            technicalIndicators = technicalIndicators,
            timestamp = timestamp,
            author = author
        )
    }

    companion object {
        fun fromDomain(signal: Signal): SignalEntity {
            return SignalEntity(
                id = signal.id,
                pair = signal.pair,
                assetSymbol = signal.assetSymbol,
                type = signal.type.name,
                status = signal.status.name,
                entryPrice = signal.entryPrice,
                currentPrice = signal.currentPrice,
                targetPrice1 = signal.targetPrice1,
                targetPrice2 = signal.targetPrice2,
                targetPrice3 = signal.targetPrice3,
                stopLoss = signal.stopLoss,
                leverage = signal.leverage,
                timeframe = signal.timeframe,
                confidenceScore = signal.confidenceScore,
                riskLevel = signal.riskLevel.name,
                aiReasoning = signal.aiReasoning,
                technicalIndicators = signal.technicalIndicators,
                timestamp = signal.timestamp,
                author = signal.author
            )
        }
    }
}
