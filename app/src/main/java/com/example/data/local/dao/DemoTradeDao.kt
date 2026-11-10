package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DemoTradeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DemoTradeDao {
    @Query("SELECT * FROM demo_trades ORDER BY openedAt DESC")
    fun getAllTrades(): Flow<List<DemoTradeEntity>>

    @Query("SELECT * FROM demo_trades WHERE status = 'OPEN' ORDER BY openedAt DESC")
    fun getOpenTrades(): Flow<List<DemoTradeEntity>>

    @Query("SELECT * FROM demo_trades WHERE status = 'OPEN' ORDER BY openedAt DESC")
    suspend fun getOpenTradesList(): List<DemoTradeEntity>

    @Query("SELECT * FROM demo_trades WHERE status != 'OPEN' ORDER BY closedAt DESC")
    fun getClosedTrades(): Flow<List<DemoTradeEntity>>

    @Query("SELECT * FROM demo_trades WHERE status != 'OPEN' ORDER BY closedAt DESC")
    suspend fun getClosedTradesList(): List<DemoTradeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: DemoTradeEntity)

    @Update
    suspend fun updateTrade(trade: DemoTradeEntity)

    @Query("DELETE FROM demo_trades WHERE id = :id")
    suspend fun deleteTrade(id: String)

    @Query("SELECT SUM(realizedPnl) FROM demo_trades WHERE status != 'OPEN'")
    fun getTotalRealizedPnl(): Flow<Double?>
}
