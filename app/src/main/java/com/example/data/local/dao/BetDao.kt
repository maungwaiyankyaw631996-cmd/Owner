package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.BetItemEntity
import com.example.data.local.entity.DailySettlementEntity
import com.example.data.local.entity.SettingsEntity
import com.example.data.local.entity.SlipEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlip(slip: SlipEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBetItems(items: List<BetItemEntity>)

    @Transaction
    suspend fun insertSlipWithItems(slip: SlipEntity, items: List<BetItemEntity>): Long {
        val slipId = insertSlip(slip)
        val itemsWithId = items.map { it.copy(slipId = slipId) }
        insertBetItems(itemsWithId)
        return slipId
    }

    @Query("DELETE FROM slips WHERE id = :slipId")
    suspend fun deleteSlipById(slipId: Long)

    @Query("SELECT * FROM slips WHERE date = :date AND sessionType = :sessionType ORDER BY createdAt DESC")
    fun getSlipsForSession(date: String, sessionType: String): Flow<List<SlipEntity>>

    @Query("SELECT * FROM bet_items WHERE date = :date AND sessionType = :sessionType ORDER BY id ASC")
    fun getBetItemsForSession(date: String, sessionType: String): Flow<List<BetItemEntity>>

    @Query("SELECT * FROM bet_items WHERE slipId = :slipId ORDER BY id ASC")
    fun getBetItemsForSlip(slipId: Long): Flow<List<BetItemEntity>>

    @Query("SELECT * FROM daily_settlements WHERE id = :id LIMIT 1")
    fun getSettlementById(id: String): Flow<DailySettlementEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettlement(settlement: DailySettlementEntity)

    @Query("SELECT * FROM dealer_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<SettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: SettingsEntity)

    @Query("DELETE FROM slips WHERE date = :date AND sessionType = :sessionType")
    suspend fun clearSessionData(date: String, sessionType: String)
}
