package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DealerSettings
import com.example.data.model.NumberMatrixCell
import com.example.data.model.SessionSettlement
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardDarkElevated
import com.example.ui.theme.CrimsonDark
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.CrimsonWarning
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SettlementScreen(
    settlement: SessionSettlement,
    matrixCells: List<NumberMatrixCell>,
    settings: DealerSettings,
    onSetWinningNumber: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var winningInput by remember { mutableStateOf(settlement.winningNumber ?: "") }
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }
    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    val currency = settings.currency

    val winningCell = if (!settlement.winningNumber.isNullOrBlank()) {
        matrixCells.find { it.number == settlement.winningNumber }
    } else null

    val isProfit = settlement.netProfitLoss >= 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Navigation Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CardDark)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "ပြန်ထွက်မည်",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "တစ်နေ့တာ ရှင်းတမ်းနှင့် အလျော်အစား",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${settlement.date} • ${settlement.sessionType.titleMm}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Winning Number Declaration Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GoldPrimary.copy(alpha = 0.5f)))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ပေါက်ဂဏန်း ထည့်သွင်း သတ်မှတ်ခြင်း",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = winningInput,
                            onValueChange = { if (it.length <= 2) winningInput = it },
                            placeholder = { Text("ဥပမာ- 25") },
                            label = { Text("2D ပေါက်ဂဏန်း") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = CardDark,
                                unfocusedContainerColor = CardDark
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_winning_number")
                        )

                        Button(
                            onClick = { onSetWinningNumber(winningInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDark),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("btn_calculate_settlement")
                        ) {
                            Icon(imageVector = Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("တွက်မည်", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (settlement.winningNumber != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CardDark)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(GoldPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = settlement.winningNumber,
                                        color = NavyDark,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("ပေါက်ဂဏန်း ထိုးထားငွေ:", color = TextSecondary, fontSize = 11.sp)
                                    Text(
                                        text = "${numberFormatter.format(winningCell?.totalBet ?: 0.0)} $currency (ဒိုင်ကိုင်: ${numberFormatter.format(winningCell?.retainedAmount ?: 0.0)})",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Text(
                                text = "${settlement.payoutMultiplier.toInt()} ဆ",
                                color = GoldLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Full Ledger Statement Card (ဝင်ငွေ / ထွက်ငွေ ရှင်းတမ်း)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDark))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ဒိုင် ငွေစာရင်း ရှင်းတမ်းအပြည့်အစုံ",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // 1. Total Turnover
                    StatementRow(
                        label = "(+) စုစုပေါင်း အရောင်း (Total Inflow)",
                        value = "${numberFormatter.format(settlement.totalTurnover)} $currency",
                        valueColor = TextPrimary
                    )

                    // 2. Over Limit Pass
                    if (settlement.totalOverLimit > 0) {
                        StatementRow(
                            label = "(-) ပိုလျှံ/ပြန်ချငွေ (Over Limit Passed)",
                            value = "- ${numberFormatter.format(settlement.totalOverLimit)} $currency",
                            valueColor = CrimsonLight
                        )
                    }

                    // 3. Retained
                    StatementRow(
                        label = "(=) ဒိုင်ကိုင် စုစုပေါင်း (Net Retained)",
                        value = "${numberFormatter.format(settlement.netRetained)} $currency",
                        valueColor = GoldPrimary,
                        isBold = true
                    )

                    // 4. Commission
                    StatementRow(
                        label = "(-) ကော်မရှင် (${settlement.commissionRate.toInt()}%)",
                        value = "- ${numberFormatter.format(settlement.commissionIncome)} $currency",
                        valueColor = IndigoAccent
                    )

                    // 5. Winning Payout
                    if (settlement.winningNumber != null) {
                        StatementRow(
                            label = "(-) ပေါက်ကြေး လျော်ငွေ (${settlement.payoutMultiplier.toInt()} ဆ)",
                            value = "- ${numberFormatter.format(settlement.totalPayout)} $currency",
                            valueColor = CrimsonWarning,
                            isBold = true
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Net Result Banner
                    Surface(
                        color = if (isProfit) EmeraldDark.copy(alpha = 0.25f) else CrimsonDark.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.5.dp,
                                if (isProfit) EmeraldSuccess else CrimsonWarning,
                                RoundedCornerShape(10.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isProfit) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if (isProfit) EmeraldLight else CrimsonLight,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isProfit) "ဒိုင် အသားတင် အမြတ် (Net Profit)" else "ဒိုင် အသားတင် အရှုံး (Net Loss)",
                                        color = if (isProfit) EmeraldLight else CrimsonLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${settlement.totalSlips} စလစ် • ${settlement.totalBetsCount} ကွက်",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Text(
                                text = "${if (isProfit) "+" else ""}${numberFormatter.format(settlement.netProfitLoss)} $currency",
                                color = if (isProfit) EmeraldLight else CrimsonLight,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Copy Settlement Voucher Button
        item {
            Button(
                onClick = {
                    val voucher = buildString {
                        appendLine("📊 2D ဒိုင် ရှင်းတမ်း (${settlement.sessionType.titleMm})")
                        appendLine("📅 နေ့စွဲ: ${settlement.date}")
                        appendLine("------------------------------")
                        appendLine("• စုစုပေါင်း အရောင်း: ${numberFormatter.format(settlement.totalTurnover)} $currency")
                        if (settlement.totalOverLimit > 0) {
                            appendLine("• ပြန်ချ/ပိုလျှံငွေ: ${numberFormatter.format(settlement.totalOverLimit)} $currency")
                        }
                        appendLine("• ဒိုင်ကိုင် ပမာဏ: ${numberFormatter.format(settlement.netRetained)} $currency")
                        appendLine("• ကော်မရှင် (${settlement.commissionRate.toInt()}%): ${numberFormatter.format(settlement.commissionIncome)} $currency")
                        if (settlement.winningNumber != null) {
                            appendLine("• ပေါက်ဂဏန်း: [${settlement.winningNumber}]")
                            appendLine("• ပေါက်ကြေး လျော်ငွေ: ${numberFormatter.format(settlement.totalPayout)} $currency")
                        }
                        appendLine("------------------------------")
                        appendLine("💰 ${if (isProfit) "အသားတင် အမြတ်" else "အသားတင် အရှုံး"}: ${if (isProfit) "+" else ""}${numberFormatter.format(settlement.netProfitLoss)} $currency")
                    }
                    clipboardManager.setText(AnnotatedString(voucher))
                    isCopied = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_copy_settlement_voucher")
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isCopied) "ရှင်းတမ်း Voucher ကော်ပီကူးပြီးပါပြီ" else "📋 ရှင်းတမ်း Voucher ကော်ပီယူမည် (Share)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun StatementRow(
    label: String,
    value: String,
    valueColor: Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal
        )
        Text(
            text = value,
            color = valueColor,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}
