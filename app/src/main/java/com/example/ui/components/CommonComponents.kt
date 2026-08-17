package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DealerSettings
import com.example.data.model.SessionType
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardDarkElevated
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.CrimsonWarning
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SessionHeader(
    selectedDate: String,
    selectedSession: SessionType,
    onDateSelected: (String) -> Unit,
    onSessionSelected: (SessionType) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // App Bar Title & Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GoldDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "2D",
                            color = NavyDark,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "2D ဒိုင် ရှင်းတမ်း",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "စာရင်းကိုင်နှင့် အလျော်အစား စနစ်",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .testTag("settings_button")
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CardDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "သတ်မှတ်ချက်များ",
                        tint = GoldPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Date Navigation Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CardDark)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "နေ့စွဲ",
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "နေ့စွဲ: $selectedDate",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                // Quick Date Switchers
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (selectedDate != todayStr) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onDateSelected(todayStr) },
                            color = GoldPrimary
                        ) {
                            Text(
                                text = "ယနေ့",
                                color = NavyDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Yesterday switch
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                if (selectedDate == todayStr) onDateSelected(yesterdayStr) else onDateSelected(todayStr)
                            },
                        color = if (selectedDate == yesterdayStr) GoldDark else CardDarkElevated
                    ) {
                        Text(
                            text = if (selectedDate == yesterdayStr) "မနေ့က" else "ရက်ပြောင်း",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Session Tabs (Morning / Evening)
            TabRow(
                selectedTabIndex = if (selectedSession == SessionType.MORNING) 0 else 1,
                containerColor = CardDark,
                contentColor = GoldPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (selectedSession == SessionType.MORNING) 0 else 1]),
                        color = GoldPrimary,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedSession == SessionType.MORNING,
                    onClick = { onSessionSelected(SessionType.MORNING) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LightMode,
                                contentDescription = null,
                                tint = if (selectedSession == SessionType.MORNING) GoldPrimary else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "မနက် (12:01 PM)",
                                fontWeight = if (selectedSession == SessionType.MORNING) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedSession == SessionType.MORNING) GoldPrimary else TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    },
                    modifier = Modifier.testTag("session_tab_morning")
                )
                Tab(
                    selected = selectedSession == SessionType.EVENING,
                    onClick = { onSessionSelected(SessionType.EVENING) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = if (selectedSession == SessionType.EVENING) GoldPrimary else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ညနေ (04:30 PM)",
                                fontWeight = if (selectedSession == SessionType.EVENING) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedSession == SessionType.EVENING) GoldPrimary else TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    },
                    modifier = Modifier.testTag("session_tab_evening")
                )
            }
        }
    }
}

@Composable
fun OverLimitAlertDialog(
    details: List<String>,
    currency: String,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = CrimsonWarning,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "သတိပေးချက်: ထိုးကြေး Limit ကျော်လွန်မှု",
                    color = CrimsonLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "အောက်ပါ ဂဏန်းများသည် သတ်မှတ်ထားသော Limit ထက် ကျော်လွန်နေသဖြင့် ဒိုင်မကိုင်တော့ဘဲ အခြားသို့ ပြန်လည်လွှဲပြောင်း (ပြန်ချ) ရန် သတိပေးပါသည်-",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = NavyDark),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CrimsonWarning.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        details.forEach { line ->
                            Text(
                                text = "• $line",
                                color = CrimsonLight,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val copyText = details.joinToString("\n")
                    clipboardManager.setText(AnnotatedString(copyText))
                    isCopied = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDark)
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isCopied) "ကော်ပီကူးပြီး" else "ပြန်ချစာရင်း ကူးမည်", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "နားလည်ပါပြီ", color = TextSecondary)
            }
        }
    )
}

@Composable
fun SettingsDialog(
    currentSettings: DealerSettings,
    onSave: (DealerSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var minBetText by remember { mutableStateOf(currentSettings.minBetAmount.toInt().toString()) }
    var maxBetText by remember { mutableStateOf(currentSettings.maxBetAmount.toInt().toString()) }
    var multiplierText by remember { mutableStateOf(currentSettings.payoutMultiplier.toInt().toString()) }
    var commissionText by remember { mutableStateOf(currentSettings.commissionPercent.toInt().toString()) }
    var currency by remember { mutableStateOf(currentSettings.currency) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = GoldPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ဒိုင် စည်းမျဉ်းနှင့် သတ်မှတ်ချက်များ",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Currency selection
                Text(
                    text = "သုံးစွဲမည့် ငွေကြေးအမျိုးအစား:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val currencies = listOf("Baht (฿)", "MMK (Ks)")
                    currencies.forEach { curr ->
                        FilterChip(
                            selected = currency == curr,
                            onClick = { currency = curr },
                            label = { Text(curr) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = NavyDark,
                                containerColor = CardDark,
                                labelColor = TextPrimary
                            )
                        )
                    }
                }

                // Minimum Bet
                OutlinedTextField(
                    value = minBetText,
                    onValueChange = { minBetText = it },
                    label = { Text("အနည်းဆုံး ထိုးကြေး (Min Bet)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedLabelColor = GoldPrimary,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Maximum Limit per single number
                OutlinedTextField(
                    value = maxBetText,
                    onValueChange = { maxBetText = it },
                    label = { Text("တစ်ကွက် အများဆုံး Limit (Max Bet/Number)") },
                    supportingText = { Text("ဥပမာ- 1000 ထားပါက 1000 ကျော်လျှင် ပိုလျှံ/ပြန်ချ သတိပေးမည်", color = TextMuted, fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedLabelColor = GoldPrimary,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Multiplier
                OutlinedTextField(
                    value = multiplierText,
                    onValueChange = { multiplierText = it },
                    label = { Text("ပေါက်ကြေး / လျော်ကြေးဆ (Multiplier)") },
                    supportingText = { Text("ဥပမာ- 80 ဆ, 85 ဆ, 90 ဆ", color = TextMuted, fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedLabelColor = GoldPrimary,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Commission Rate
                OutlinedTextField(
                    value = commissionText,
                    onValueChange = { commissionText = it },
                    label = { Text("ကော်မရှင် ရာခိုင်နှုန်း (Commission %)") },
                    supportingText = { Text("ဥပမာ- 10%, 15% သို့မဟုတ် 0%", color = TextMuted, fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedLabelColor = GoldPrimary,
                        unfocusedLabelColor = TextSecondary,
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
                    val min = minBetText.toDoubleOrNull() ?: 100.0
                    val max = maxBetText.toDoubleOrNull() ?: 1000.0
                    val mult = multiplierText.toDoubleOrNull() ?: 80.0
                    val comm = commissionText.toDoubleOrNull() ?: 10.0
                    onSave(
                        DealerSettings(
                            minBetAmount = min,
                            maxBetAmount = max,
                            payoutMultiplier = mult,
                            commissionPercent = comm,
                            currency = currency
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDark)
            ) {
                Text("သိမ်းဆည်းမည်", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ပယ်ဖျက်", color = TextSecondary)
            }
        }
    )
}
