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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
fun DashboardScreen(
    settlement: SessionSettlement,
    matrixCells: List<NumberMatrixCell>,
    settings: DealerSettings,
    onNavigateToAddSlip: () -> Unit,
    onNavigateToMatrix: () -> Unit,
    onNavigateToSlips: () -> Unit,
    onNavigateToSettlement: () -> Unit,
    onSetWinningNumber: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val overLimitCells = matrixCells.filter { it.isOverLimit }
    var showWinningDialog by remember { mutableStateOf(false) }
    var winningInput by remember { mutableStateOf("") }
    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    val currency = settings.currency

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Alert Banner for Over-Limit Hot Numbers (If any)
        if (overLimitCells.isNotEmpty()) {
            item {
                OverLimitBanner(
                    overLimitCount = overLimitCells.size,
                    totalExcessAmount = settlement.totalOverLimit,
                    currency = currency,
                    onClickView = onNavigateToMatrix
                )
            }
        }

        // 2. Primary Financial Ledger Card (ရငွေ၊ ဒိုင်ကိုင်၊ ကော်မရှင်၊ အမြတ်/အရှုံး)
        item {
            PrimaryLedgerCard(
                settlement = settlement,
                currency = currency,
                formatter = numberFormatter,
                onWinningClick = {
                    winningInput = settlement.winningNumber ?: ""
                    showWinningDialog = true
                }
            )
        }

        // 3. Quick Action Buttons Grid
        item {
            Text(
                text = "အဓိက လုပ်ဆောင်ချက်များ",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionCard(
                    title = "ဘောင်ချာထည့်ရန်",
                    subtitle = "Copy-Paste/ပုံ/အသံ",
                    icon = Icons.Default.Add,
                    accentColor = GoldPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_add_slip"),
                    onClick = onNavigateToAddSlip
                )
                ActionCard(
                    title = "ဂဏန်း (၁၀၀) ကွက်",
                    subtitle = if (overLimitCells.isNotEmpty()) "${overLimitCells.size} ကွက် ပိုလျှံ/ပြန်ချ" else "ကွက်စိပ် စာရင်း",
                    icon = Icons.Default.GridOn,
                    accentColor = if (overLimitCells.isNotEmpty()) CrimsonWarning else EmeraldSuccess,
                    badgeText = if (overLimitCells.isNotEmpty()) "${overLimitCells.size} Hot" else null,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_view_matrix"),
                    onClick = onNavigateToMatrix
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionCard(
                    title = "ဘောင်ချာများ",
                    subtitle = "${settlement.totalSlips} စလစ် (${settlement.totalBetsCount} ကွက်)",
                    icon = Icons.Default.ReceiptLong,
                    accentColor = IndigoAccent,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_view_slips"),
                    onClick = onNavigateToSlips
                )
                ActionCard(
                    title = "ရှင်းတမ်းနှင့် အလျော်",
                    subtitle = if (settlement.winningNumber != null) "ပေါက်: [${settlement.winningNumber}]" else "ပေါက်ဂဏန်း သွင်းရန်",
                    icon = Icons.Default.Calculate,
                    accentColor = GoldLight,
                    badgeText = settlement.winningNumber,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_view_settlement"),
                    onClick = onNavigateToSettlement
                )
            }
        }

        // 4. Hot Numbers / High Volume Numbers Ticker
        val activeNumbers = matrixCells.filter { it.totalBet > 0 }.sortedByDescending { it.totalBet }
        if (activeNumbers.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ထိပ်တန်း ထိုးကြေးများသော ဂဏန်းများ",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "အားလုံး (${activeNumbers.size}) ကွက်",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onNavigateToMatrix() }
                    )
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(activeNumbers.take(10)) { cell ->
                        HotNumberChip(cell = cell, currency = currency, formatter = numberFormatter)
                    }
                }
            }
        }
    }

    // Winning Number Input Dialog
    if (showWinningDialog) {
        AlertDialog(
            onDismissRequest = { showWinningDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text(
                    text = "ပေါက်ဂဏန်း သွင်းပြီး အလျော်တွက်မည်",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ထွက်ရှိသော 2D ပေါက်ဂဏန်း ၂ လုံး (၀၀-၉၉) ကို ထည့်သွင်းပါ:",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = winningInput,
                        onValueChange = { if (it.length <= 2) winningInput = it },
                        label = { Text("ပေါက်ဂဏန်း (ဥပမာ- 45)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSetWinningNumber(winningInput)
                        showWinningDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDark)
                ) {
                    Text("တွက်ချက်မည်", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWinningDialog = false }) {
                    Text("ပယ်ဖျက်", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun OverLimitBanner(
    overLimitCount: Int,
    totalExcessAmount: Double,
    currency: String,
    onClickView: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CrimsonDark.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, CrimsonWarning, RoundedCornerShape(12.dp))
            .clickable { onClickView() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CrimsonWarning),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = NavyDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Limit ကျော်လွန်သော ဂဏန်း ($overLimitCount) ခု ရှိပါသည်!",
                        color = CrimsonLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "စုစုပေါင်း ပိုလျှံငွေ: ${totalExcessAmount.toInt()} $currency (ဒိုင်မကိုင်ဘဲ ပြန်ချရန်)",
                        color = TextPrimary,
                        fontSize = 11.sp
                    )
                }
            }
            Surface(
                color = CrimsonWarning,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "စစ်ဆေးမည်",
                    color = NavyDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun PrimaryLedgerCard(
    settlement: SessionSettlement,
    currency: String,
    formatter: NumberFormat,
    onWinningClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(GoldPrimary.copy(alpha = 0.5f), BorderDark)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Winning Number Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "တစ်နေ့တာ ရှင်းတမ်း အချုပ်",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "စုစုပေါင်း အရောင်း (Turnover)",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Winning Number Pill
                Surface(
                    color = if (settlement.winningNumber != null) GoldPrimary else CardDark,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .clickable { onWinningClick() }
                        .border(
                            1.dp,
                            if (settlement.winningNumber != null) GoldLight else BorderDark,
                            RoundedCornerShape(20.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (settlement.winningNumber != null) "ပေါက်: [${settlement.winningNumber}]" else "ပေါက်ဂဏန်းထည့်",
                            color = if (settlement.winningNumber != null) NavyDark else GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Big Total Sales Inflow
            Text(
                text = "${formatter.format(settlement.totalTurnover)} $currency",
                color = GoldPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Breakdown Grid (Retained, OverLimit, Commission, Payout)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CardDark)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ဒိုင်ကိုင် စုစုပေါင်း (Retained):", color = TextSecondary, fontSize = 12.sp)
                    Text(
                        "${formatter.format(settlement.netRetained)} $currency",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }

                if (settlement.totalOverLimit > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ပိုလျှံ/ပြန်ချ (Over Limit):", color = CrimsonLight, fontSize = 12.sp)
                        Text(
                            "- ${formatter.format(settlement.totalOverLimit)} $currency",
                            color = CrimsonLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ကော်မရှင် (${settlement.commissionRate.toInt()}%):", color = TextSecondary, fontSize = 12.sp)
                    Text(
                        "${formatter.format(settlement.commissionIncome)} $currency",
                        color = IndigoAccent,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }

                if (settlement.winningNumber != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("လျော်ငွေ (${settlement.payoutMultiplier.toInt()} ဆ):", color = CrimsonLight, fontSize = 12.sp)
                        Text(
                            "- ${formatter.format(settlement.totalPayout)} $currency",
                            color = CrimsonWarning,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Net Dealer Balance (Profit or Loss)
            val isProfit = settlement.netProfitLoss >= 0
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isProfit) EmeraldDark.copy(alpha = 0.2f) else CrimsonDark.copy(alpha = 0.2f))
                    .border(
                        1.dp,
                        if (isProfit) EmeraldSuccess.copy(alpha = 0.4f) else CrimsonWarning.copy(alpha = 0.4f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isProfit) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (isProfit) EmeraldLight else CrimsonLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isProfit) "ဒိုင် အသားတင် အမြတ်" else "ဒိုင် အသားတင် အရှုံး",
                        color = if (isProfit) EmeraldLight else CrimsonLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Text(
                    text = "${if (isProfit) "+" else ""}${formatter.format(settlement.netProfitLoss)} $currency",
                    color = if (isProfit) EmeraldLight else CrimsonLight,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeText: String? = null
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(accentColor.copy(alpha = 0.3f), BorderDark)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (badgeText != null) {
                    Surface(
                        color = accentColor,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = NavyDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun HotNumberChip(
    cell: NumberMatrixCell,
    currency: String,
    formatter: NumberFormat
) {
    val bgColor = when {
        cell.isOverLimit -> CrimsonDark.copy(alpha = 0.3f)
        cell.isNearLimit -> GoldDark.copy(alpha = 0.25f)
        else -> CardDark
    }
    val borderColor = when {
        cell.isOverLimit -> CrimsonWarning
        cell.isNearLimit -> GoldPrimary
        else -> BorderDark
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.border(1.dp, borderColor, RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (cell.isOverLimit) CrimsonWarning else GoldPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cell.number,
                    color = NavyDark,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "${formatter.format(cell.totalBet)} $currency",
                    color = if (cell.isOverLimit) CrimsonLight else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                if (cell.isOverLimit) {
                    Text(
                        text = "+${cell.overAmount.toInt()} ပိုလျှံ",
                        color = CrimsonWarning,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                } else {
                    Text(
                        text = "${cell.betCount} စလစ်",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
