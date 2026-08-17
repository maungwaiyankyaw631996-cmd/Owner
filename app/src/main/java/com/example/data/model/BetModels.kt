package com.example.data.model

enum class SessionType(val titleMm: String, val timeCode: String) {
    MORNING("မနက်ပိုင်း (12:01 PM)", "12:01"),
    EVENING("ညနေပိုင်း (04:30 PM)", "04:30");

    companion object {
        fun fromCode(code: String): SessionType {
            return if (code.equals("EVENING", ignoreCase = true) || code.contains("04:30") || code.contains("ညနေ")) {
                EVENING
            } else {
                MORNING
            }
        }
    }
}

data class ParsedBetItem(
    val number: String,
    val amount: Double,
    val formulaType: String = "တိုက်ရိုက်",
    val isBelowMin: Boolean = false,
    val isAboveMaxSingle: Boolean = false
)

data class ParseResult(
    val items: List<ParsedBetItem>,
    val totalAmount: Double,
    val itemCount: Int,
    val unrecognizedTokens: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val rawText: String = ""
)

data class NumberMatrixCell(
    val number: String,
    val totalBet: Double,
    val limit: Double,
    val retainedAmount: Double,
    val overAmount: Double,
    val isOverLimit: Boolean,
    val isNearLimit: Boolean, // >= 80%
    val betCount: Int
)

data class DealerSettings(
    val minBetAmount: Double = 100.0,
    val maxBetAmount: Double = 1000.0,
    val payoutMultiplier: Double = 80.0,
    val commissionPercent: Double = 10.0,
    val currency: String = "Baht (฿)" // "Baht (฿)" or "MMK (Ks)"
)

data class SessionSettlement(
    val date: String,
    val sessionType: SessionType,
    val totalTurnover: Double,      // စုစုပေါင်း အရောင်း
    val totalOverLimit: Double,     // ပိုလျှံ/ပြန်ချငွေ
    val netRetained: Double,        // ဒိုင်ကိုင် ပမာဏ
    val commissionRate: Double,     // ကော်မရှင် %
    val commissionIncome: Double,   // ကော်မရှင် ရငွေ
    val winningNumber: String?,     // ပေါက်ဂဏန်း
    val payoutMultiplier: Double,   // လျော်ကြေးဆ
    val totalPayout: Double,        // စုစုပေါင်း လျော်ကြေး
    val netProfitLoss: Double,      // ဒိုင်အသားတင် အမြတ်/အရှုံး
    val totalSlips: Int,
    val totalBetsCount: Int
)
