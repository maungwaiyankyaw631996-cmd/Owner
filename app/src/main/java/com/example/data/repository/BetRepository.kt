package com.example.data.repository

import com.example.data.local.dao.BetDao
import com.example.data.local.entity.BetItemEntity
import com.example.data.local.entity.DailySettlementEntity
import com.example.data.local.entity.SettingsEntity
import com.example.data.local.entity.SlipEntity
import com.example.data.model.DealerSettings
import com.example.data.model.ParsedBetItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BetRepository(private val betDao: BetDao) {

    fun getSlipsForSession(date: String, sessionType: String): Flow<List<SlipEntity>> =
        betDao.getSlipsForSession(date, sessionType)

    fun getBetItemsForSession(date: String, sessionType: String): Flow<List<BetItemEntity>> =
        betDao.getBetItemsForSession(date, sessionType)

    fun getSettlement(date: String, sessionType: String): Flow<DailySettlementEntity?> =
        betDao.getSettlementById("${date}_$sessionType")

    suspend fun saveSlip(
        customerName: String,
        rawText: String,
        date: String,
        sessionType: String,
        items: List<ParsedBetItem>
    ): Long {
        val totalAmount = items.sumOf { it.amount }
        val slip = SlipEntity(
            customerName = customerName.ifBlank { "အမည်မသိ မိတ်ဆွေ" },
            totalAmount = totalAmount,
            betCount = items.size,
            rawText = rawText,
            date = date,
            sessionType = sessionType
        )

        val betEntities = items.map {
            BetItemEntity(
                slipId = 0, // will be replaced in DAO transaction
                number = it.number,
                amount = it.amount,
                formulaType = it.formulaType,
                date = date,
                sessionType = sessionType
            )
        }

        return betDao.insertSlipWithItems(slip, betEntities)
    }

    suspend fun deleteSlip(slipId: Long) {
        betDao.deleteSlipById(slipId)
    }

    suspend fun clearSession(date: String, sessionType: String) {
        betDao.clearSessionData(date, sessionType)
    }

    suspend fun saveWinningNumber(
        date: String,
        sessionType: String,
        winningNumber: String,
        multiplier: Double,
        commission: Double
    ) {
        val id = "${date}_$sessionType"
        val settlement = DailySettlementEntity(
            id = id,
            date = date,
            sessionType = sessionType,
            winningNumber = winningNumber,
            payoutMultiplier = multiplier,
            commissionRate = commission
        )
        betDao.saveSettlement(settlement)
    }

    fun getSettings(): Flow<DealerSettings> = betDao.getSettings().map { entity ->
        if (entity != null) {
            DealerSettings(
                minBetAmount = entity.minBetAmount,
                maxBetAmount = entity.maxBetAmount,
                payoutMultiplier = entity.payoutMultiplier,
                commissionPercent = entity.commissionPercent,
                currency = entity.currency
            )
        } else {
            DealerSettings()
        }
    }

    suspend fun updateSettings(settings: DealerSettings) {
        val entity = SettingsEntity(
            id = 1,
            minBetAmount = settings.minBetAmount,
            maxBetAmount = settings.maxBetAmount,
            payoutMultiplier = settings.payoutMultiplier,
            commissionPercent = settings.commissionPercent,
            currency = settings.currency
        )
        betDao.saveSettings(entity)
    }
}
