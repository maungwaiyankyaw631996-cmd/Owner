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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DealerSettings
import com.example.data.model.NumberMatrixCell
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
import com.example.ui.theme.MatrixEmpty
import com.example.ui.theme.MatrixNearLimit
import com.example.ui.theme.MatrixNormal
import com.example.ui.theme.MatrixOverLimit
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

enum class MatrixFilter {
    ALL,
    OVER_LIMIT,
    ACTIVE,
    EMPTY
}

@Composable
fun Matrix100Screen(
    cells: List<NumberMatrixCell>,
    settings: DealerSettings,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(MatrixFilter.ALL) }
    var selectedCell by remember { mutableStateOf<NumberMatrixCell?>(null) }
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }
    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    val currency = settings.currency

    val overLimitCells = cells.filter { it.isOverLimit }
    val activeCells = cells.filter { it.totalBet > 0 }
    val emptyCells = cells.filter { it.totalBet == 0.0 }

    val displayedCells = when (selectedFilter) {
        MatrixFilter.ALL -> cells
        MatrixFilter.OVER_LIMIT -> overLimitCells
        MatrixFilter.ACTIVE -> activeCells
        MatrixFilter.EMPTY -> emptyCells
    }

    val totalExcess = overLimitCells.sumOf { it.overAmount }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Top Navigation Header
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
                        text = "ဂဏန်း (၁၀၀) ကွက် ဇယား",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Limit: ${settings.maxBetAmount.toInt()} $currency (ထက်ကျော်လျှင် ပြန်ချရန်)",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Copy Over-limit Pass list
            if (overLimitCells.isNotEmpty()) {
                Button(
                    onClick = {
                        val lines = overLimitCells.map { "${it.number} - ${it.overAmount.toInt()}" }
                        val copyText = "🔴 2D ပြန်ချ/လွှဲ စာရင်း:\n" + lines.joinToString("\n") + "\nစုစုပေါင်း = ${totalExcess.toInt()} $currency"
                        clipboardManager.setText(AnnotatedString(copyText))
                        isCopied = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonWarning, contentColor = NavyDark),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isCopied) "ကူးပြီး" else "ပြန်ချစာရင်း", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == MatrixFilter.ALL,
                    onClick = { selectedFilter = MatrixFilter.ALL },
                    label = { Text("အားလုံး (100)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = NavyDark,
                        containerColor = CardDark,
                        labelColor = TextPrimary
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == MatrixFilter.OVER_LIMIT,
                    onClick = { selectedFilter = MatrixFilter.OVER_LIMIT },
                    label = {
                        Text(
                            "🔴 ပြန်ချရန် (${overLimitCells.size})",
                            color = if (selectedFilter == MatrixFilter.OVER_LIMIT) NavyDark else CrimsonLight
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CrimsonWarning,
                        selectedLabelColor = NavyDark,
                        containerColor = CardDark,
                        labelColor = CrimsonLight
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == MatrixFilter.ACTIVE,
                    onClick = { selectedFilter = MatrixFilter.ACTIVE },
                    label = { Text("🟢 ထိုးပြီး (${activeCells.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldSuccess,
                        selectedLabelColor = NavyDark,
                        containerColor = CardDark,
                        labelColor = TextPrimary
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == MatrixFilter.EMPTY,
                    onClick = { selectedFilter = MatrixFilter.EMPTY },
                    label = { Text("⚪ လွတ်နေ (${emptyCells.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TextSecondary,
                        selectedLabelColor = NavyDark,
                        containerColor = CardDark,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Color Legend Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CardDark)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = EmeraldDark, label = "ပုံမှန်")
            LegendItem(color = GoldDark, label = "80% နီးကပ်")
            LegendItem(color = CrimsonWarning, label = "Limit ကျော် (+ပို)")
            LegendItem(color = MatrixEmpty, label = "မရှိ")
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 10-column Number Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            contentPadding = PaddingValues(bottom = 90.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(displayedCells) { cell ->
                MatrixGridCell(
                    cell = cell,
                    currency = currency,
                    onClick = { selectedCell = cell }
                )
            }
        }
    }

    // Number Detail Modal Dialog
    if (selectedCell != null) {
        val cell = selectedCell!!
        AlertDialog(
            onDismissRequest = { selectedCell = null },
            containerColor = SurfaceDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (cell.isOverLimit) CrimsonWarning else GoldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cell.number,
                            color = NavyDark,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ဂဏန်း [${cell.number}] အသေးစိတ်",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailRow(label = "စုစုပေါင်း ထိုးကြေး:", value = "${numberFormatter.format(cell.totalBet)} $currency", isBold = true)
                    DetailRow(label = "သတ်မှတ် Limit:", value = "${numberFormatter.format(cell.limit)} $currency")
                    DetailRow(label = "ဒိုင်ကိုင် ပမာဏ (Retained):", value = "${numberFormatter.format(cell.retainedAmount)} $currency", valueColor = EmeraldLight)
                    if (cell.isOverLimit) {
                        DetailRow(
                            label = "ပိုလျှံ/ပြန်ချရန် ပမာဏ:",
                            value = "+${numberFormatter.format(cell.overAmount)} $currency",
                            valueColor = CrimsonLight,
                            isBold = true
                        )
                    }
                    DetailRow(label = "ပါဝင်သော ဘောင်ချာအရေအတွက်:", value = "${cell.betCount} စလစ်")

                    if (cell.isOverLimit) {
                        Surface(
                            color = CrimsonDark.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CrimsonWarning.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "⚠️ ဤဂဏန်းသည် Limit ကျော်လွန်နေသဖြင့် ပိုလျှံငွေ (${cell.overAmount.toInt()} $currency) ကို အခြားဒိုင်သို့ ပြန်လည်လွှဲပြောင်း (ပြန်ချ) ရန် သတိပေးပါသည်!",
                                color = CrimsonLight,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val text = "${cell.number} - ${if (cell.isOverLimit) cell.overAmount.toInt() else cell.totalBet.toInt()}"
                        clipboardManager.setText(AnnotatedString(text))
                        selectedCell = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDark)
                ) {
                    Text("ဂဏန်း ကော်ပီယူမည်", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCell = null }) {
                    Text("ပိတ်မည်", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun MatrixGridCell(
    cell: NumberMatrixCell,
    currency: String,
    onClick: () -> Unit
) {
    val bgColor = when {
        cell.isOverLimit -> CrimsonDark.copy(alpha = 0.4f)
        cell.isNearLimit -> GoldDark.copy(alpha = 0.3f)
        cell.totalBet > 0 -> EmeraldDark.copy(alpha = 0.3f)
        else -> CardDark
    }

    val borderColor = when {
        cell.isOverLimit -> CrimsonWarning
        cell.isNearLimit -> GoldPrimary
        cell.totalBet > 0 -> EmeraldSuccess.copy(alpha = 0.5f)
        else -> BorderDark.copy(alpha = 0.4f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(8.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(borderColor))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = cell.number,
                color = if (cell.isOverLimit) CrimsonLight else if (cell.totalBet > 0) GoldLight else TextSecondary,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            if (cell.totalBet > 0) {
                Text(
                    text = if (cell.totalBet >= 1000) "${(cell.totalBet / 1000).toInt()}k" else "${cell.totalBet.toInt()}",
                    color = if (cell.isOverLimit) CrimsonLight else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                if (cell.isOverLimit) {
                    Surface(
                        color = CrimsonWarning,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "+${cell.overAmount.toInt()}",
                            color = NavyDark,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "-",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = TextSecondary, fontSize = 10.sp)
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 13.sp)
        Text(
            text = value,
            color = valueColor,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}
