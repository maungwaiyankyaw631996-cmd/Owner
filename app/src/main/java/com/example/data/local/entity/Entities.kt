package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "slips",
    indices = [Index(value = ["date", "sessionType"])]
)
data class SlipEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerName: String,
    val totalAmount: Double,
    val betCount: Int,
    val rawText: String,
    val date: String, // e.g. "2026-08-17"
    val sessionType: String, // "MORNING" or "EVENING"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "bet_items",
    foreignKeys = [
        ForeignKey(
            entity = SlipEntity::class,
            parentColumns = ["id"],
            childColumns = ["slipId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["slipId"]),
        Index(value = ["date", "sessionType", "number"])
    ]
)
data class BetItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val slipId: Long,
    val number: String, // "00".."99"
    val amount: Double,
    val formulaType: String,
    val date: String,
    val sessionType: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_settlements")
data class DailySettlementEntity(
    @PrimaryKey
    val id: String, // e.g. "2026-08-17_MORNING"
    val date: String,
    val sessionType: String,
    val winningNumber: String? = null,
    val payoutMultiplier: Double = 80.0,
    val commissionRate: Double = 10.0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "dealer_settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val minBetAmount: Double = 100.0,
    val maxBetAmount: Double = 1000.0,
    val payoutMultiplier: Double = 80.0,
    val commissionPercent: Double = 10.0,
    val currency: String = "Baht (฿)"
)
