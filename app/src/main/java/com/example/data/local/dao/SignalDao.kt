package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.SignalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SignalDao {
    @Query("SELECT * FROM signals ORDER BY timestamp DESC")
    fun getAllSignals(): Flow<List<SignalEntity>>

    @Query("SELECT * FROM signals WHERE id = :id")
    suspend fun getSignalById(id: String): SignalEntity?

    @Query("SELECT * FROM signals WHERE status = :status ORDER BY timestamp DESC")
    fun getSignalsByStatus(status: String): Flow<List<SignalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignals(signals: List<SignalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignal(signal: SignalEntity)

    @Update
    suspend fun updateSignal(signal: SignalEntity)

    @Query("SELECT * FROM signals ORDER BY timestamp DESC")
    suspend fun getAllSignalsList(): List<SignalEntity>

    @Query("UPDATE signals SET currentPrice = :currentPrice WHERE pair = :pair")
    suspend fun updateSignalPriceByPair(pair: String, currentPrice: Double)

    @Query("UPDATE signals SET currentPrice = :currentPrice, status = :status WHERE id = :id")
    suspend fun updateSignalPriceAndStatus(id: String, currentPrice: Double, status: String)

    @Query("DELETE FROM signals WHERE id = :id")
    suspend fun deleteSignal(id: String)
}
