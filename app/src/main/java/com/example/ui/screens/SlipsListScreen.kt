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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SlipEntity
import com.example.data.model.DealerSettings
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardDarkElevated
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.CrimsonWarning
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SlipsListScreen(
    slips: List<SlipEntity>,
    settings: DealerSettings,
    onDeleteSlip: (Long) -> Unit,
    onClearAllSlips: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var slipToDelete by remember { mutableStateOf<SlipEntity?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }
    val expandedStates = remember { mutableStateMapOf<Long, Boolean>() }
    val clipboardManager = LocalClipboardManager.current
    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val currency = settings.currency

    val filteredSlips = slips.filter {
        searchQuery.isBlank() ||
                it.customerName.contains(searchQuery, ignoreCase = true) ||
                it.rawText.contains(searchQuery, ignoreCase = true)
    }

    val totalTurnover = slips.sumOf { it.totalAmount }
    val totalBets = slips.sumOf { it.betCount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            text = "ဘောင်ချာများ စာရင်း",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "စုစုပေါင်း (${slips.size}) စလစ် / ($totalBets) ကွက်",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                if (slips.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearConfirm = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CardDark)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "အားလုံးဖျက်မည်",
                            tint = CrimsonLight
                        )
                    }
                }
            }
        }

        // Summary Bar
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDark))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "စုစုပေါင်း စလစ်ငွေ", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            text = "${numberFormatter.format(totalTurnover)} $currency",
                            color = GoldPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Surface(
                        color = CardDark,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${slips.size} စလစ် (${totalBets} ကွက်)",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Search Field
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("အမည် သို့မဟုတ် ဂဏန်းဖြင့် ရှာရန်...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted)
                },
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
                    .fillMaxWidth()
                    .testTag("input_search_slips")
            )
        }

        // Slips List Items
        if (filteredSlips.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "လက်ရှိ အချိန်ပိုင်းတွင် ဘောင်ချာ မရှိသေးပါ" else "ရှာဖွေမှုနှင့် ကိုက်ညီသော စလစ် မရှိပါ",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(filteredSlips, key = { it.id }) { slip ->
                val isExpanded = expandedStates[slip.id] ?: false

                SlipCard(
                    slip = slip,
                    currency = currency,
                    formatter = numberFormatter,
                    timeFormatter = timeFormatter,
                    isExpanded = isExpanded,
                    onToggleExpand = { expandedStates[slip.id] = !isExpanded },
                    onCopySlip = {
                        val text = "🧾 2D ဘောင်ချာ [${slip.customerName}]\nနေ့စွဲ: ${slip.date} (${slip.sessionType})\n\n${slip.rawText}\n\nစုစုပေါင်း (${slip.betCount} ကွက်) = ${numberFormatter.format(slip.totalAmount)} $currency"
                        clipboardManager.setText(AnnotatedString(text))
                    },
                    onDeleteClick = { slipToDelete = slip }
                )
            }
        }
    }

    // Delete Single Slip Dialog
    if (slipToDelete != null) {
        val slip = slipToDelete!!
        AlertDialog(
            onDismissRequest = { slipToDelete = null },
            containerColor = SurfaceDark,
            title = {
                Text(
                    text = "ဘောင်ချာ ပယ်ဖျက်ရန် အတည်ပြုပါ",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "${slip.customerName} ၏ စုစုပေါင်း (${numberFormatter.format(slip.totalAmount)} $currency) တန်ဖိုးရှိ စလစ်ကို အပြီးတိုင် ဖျက်မည်မှာ သေချာပါသလား?",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSlip(slip.id)
                        slipToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonWarning, contentColor = NavyDark)
                ) {
                    Text("ဖျက်မည်", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { slipToDelete = null }) {
                    Text("ပယ်ဖျက်", color = TextSecondary)
                }
            }
        )
    }

    // Clear All Slips Dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = SurfaceDark,
            title = {
                Text(
                    text = "အချိန်ပိုင်း စာရင်းအားလုံး ရှင်းလင်းမည်",
                    color = CrimsonLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "လက်ရှိ အချိန်ပိုင်းရှိ ဘောင်ချာနှင့် ဂဏန်းစာရင်း အားလုံးကို ဖျက်ပစ်မည် ဖြစ်ပါသည်။ ဆက်လက်လုပ်ဆောင်မည်လား?",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllSlips()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonWarning, contentColor = NavyDark)
                ) {
                    Text("အားလုံး ရှင်းမည်", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("ပယ်ဖျက်", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun SlipCard(
    slip: SlipEntity,
    currency: String,
    formatter: NumberFormat,
    timeFormatter: SimpleDateFormat,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onCopySlip: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDark)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldDark.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = slip.customerName,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${timeFormatter.format(Date(slip.createdAt))} • ${slip.betCount} ကွက်",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${formatter.format(slip.totalAmount)} $currency",
                        color = GoldPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onToggleExpand, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Surface(
                        color = NavyDark,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = slip.rawText,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onCopySlip) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = GoldLight, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ဘောင်ချာ ကူးမည်", color = GoldLight, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = onDeleteClick) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = CrimsonLight, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ဖျက်မည်", color = CrimsonLight, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
