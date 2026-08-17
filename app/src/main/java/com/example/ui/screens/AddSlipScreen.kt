package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DealerSettings
import com.example.data.model.ParseResult
import com.example.data.model.ParsedBetItem
import com.example.speech.VoiceRecognitionHelper
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardDarkElevated
import com.example.ui.theme.CrimsonDark
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.CrimsonWarning
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
fun AddSlipScreen(
    rawText: String,
    customerName: String,
    parseResult: ParseResult?,
    settings: DealerSettings,
    isOcrLoading: Boolean,
    isVoiceListening: Boolean,
    onRawTextChanged: (String) -> Unit,
    onCustomerNameChanged: (String) -> Unit,
    onAppendFormula: (String) -> Unit,
    onClearForm: () -> Unit,
    onSaveSlip: () -> Unit,
    onBack: () -> Unit,
    onProcessImageOcr: (Bitmap) -> Unit,
    onProcessVoiceResult: (String) -> Unit,
    onVoiceListeningStateChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    val currency = settings.currency

    // Speech Recognizer Helper
    val voiceHelper = remember {
        VoiceRecognitionHelper(
            context = context,
            onResult = { text -> onProcessVoiceResult(text) },
            onError = { /* Error handled via viewmodel/toast */ },
            onListeningStateChanged = { listening -> onVoiceListeningStateChanged(listening) }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceHelper.stopListening()
        }
    }

    // Photo Pickers
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                }
                onProcessImageOcr(bitmap)
            } catch (e: Exception) {
                // Ignore decoding error
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            onProcessImageOcr(it)
        }
    }

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
                    Text(
                        text = "ဘောင်ချာ အသစ်ထည့်ရန်",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(onClick = onClearForm) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ရှင်းမည်", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }

        // Customer / Agent Name Field
        item {
            OutlinedTextField(
                value = customerName,
                onValueChange = onCustomerNameChanged,
                label = { Text("ထိုးသူ အမည် / Viber / ဖုန်းနံပါတ်") },
                placeholder = { Text("ဥပမာ- ကိုကျော် / Agent 1") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = BorderDark,
                    focusedLabelColor = GoldPrimary,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = CardDark,
                    unfocusedContainerColor = CardDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_customer_name")
            )
        }

        // Smart Input Toolbox (Paste & Run, Voice to Text, Image OCR)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDark))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "စာသား အလိုအလျောက် သွင်းနည်းများ (Auto-Run):",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Paste & Run Button
                        Button(
                            onClick = {
                                val clipText = clipboardManager.getText()?.text
                                if (!clipText.isNullOrBlank()) {
                                    val updated = if (rawText.isBlank()) clipText else "$rawText\n$clipText"
                                    onRawTextChanged(updated)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDark),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("btn_paste_run")
                        ) {
                            Icon(imageVector = Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("📋 Paste & Run", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // 2. Image OCR Photo Picker
                        OutlinedButton(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_ocr_gallery")
                        ) {
                            if (isOcrLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = GoldPrimary, strokeWidth = 2.dp)
                            } else {
                                Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = IndigoAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ပုံ OCR", fontSize = 12.sp)
                            }
                        }

                        // 3. Camera Capture
                        OutlinedButton(
                            onClick = { cameraLauncher.launch() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(0.9f)
                                .testTag("btn_ocr_camera")
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = GoldLight, modifier = Modifier.size(16.dp))
                        }

                        // 4. Voice Dictation
                        Button(
                            onClick = {
                                if (isVoiceListening) {
                                    voiceHelper.stopListening()
                                } else {
                                    voiceHelper.startListening()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isVoiceListening) CrimsonWarning else CardDarkElevated,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(0.9f)
                                .testTag("btn_voice_record")
                        ) {
                            Icon(
                                imageVector = if (isVoiceListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "အသံသွင်း",
                                tint = if (isVoiceListening) NavyDark else EmeraldLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (isVoiceListening) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CrimsonDark.copy(alpha = 0.2f))
                                .padding(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = CrimsonLight, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "အသံဖမ်းယူနေပါသည်... (ဥပမာ- ၁၂ ၅၀၀၊ ၃၄ R ၁၀၀၀၊ အပူး ၅၀၀)",
                                color = CrimsonLight,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Quick Formula Pills (R, အပူး, အခွေ, နက္ခတ်, ပါဝါ, ညီကို, ၁ ပတ်, ထိပ်, ပိတ်, ဘရိတ်)
        item {
            Column {
                Text(
                    text = "အမြန် ထိုးကြေး ပုံသေနည်းများ:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val formulas = listOf(
                        "R (အပြန်)" to "12 R 500",
                        "အပူး (00-99)" to "အပူး 500",
                        "ပါဝါ" to "ပါဝါ 500",
                        "နက္ခတ်" to "နက္ခတ် 500",
                        "ညီကို" to "ညီကို 500",
                        "အခွေ" to "123 ခွေ 500",
                        "၁ ပတ်" to "1 ပတ် 500",
                        "ထိပ်" to "1 ထိပ် 500",
                        "ပိတ်" to "1 ပိတ် 500",
                        "ဘရိတ်" to "5 ဘရိတ် 500"
                    )
                    items(formulas) { (label, formula) ->
                        Surface(
                            color = CardDark,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                                .clickable { onAppendFormula(formula) }
                        ) {
                            Text(
                                text = "+ $label",
                                color = GoldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Raw Text Input Box
        item {
            OutlinedTextField(
                value = rawText,
                onValueChange = onRawTextChanged,
                label = { Text("စာသား ရိုက်ထည့် / Paste ထည့်ရန်") },
                placeholder = {
                    Text(
                        "ဥပမာ:\n12-500\n34 R 1000\nအပူး 500\n123 ခွေ 300\n1 ပတ် 500\n5 ဘရိတ် 500"
                    )
                },
                minLines = 4,
                maxLines = 8,
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
                    .testTag("input_raw_text")
            )
        }

        // Live Parsed Result Summary & Warnings
        if (parseResult != null && parseResult.items.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GoldPrimary.copy(alpha = 0.4f)))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ခွဲထုတ်ပြီး ထိုးကြေး စာရင်း",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "စုစုပေါင်း (${parseResult.itemCount}) ကွက်",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${numberFormatter.format(parseResult.totalAmount)} $currency",
                                color = GoldPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Warnings (Min limit / Max limit)
                        if (parseResult.warnings.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CrimsonDark.copy(alpha = 0.2f))
                                    .border(1.dp, CrimsonWarning.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                parseResult.warnings.forEach { warning ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = CrimsonLight, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = warning, color = CrimsonLight, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Items Grid / List Preview
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CardDark)
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            parseResult.items.take(15).forEach { item ->
                                ParsedBetRow(item = item, currency = currency, formatter = numberFormatter)
                            }
                            if (parseResult.items.size > 15) {
                                Text(
                                    text = "+ နောက်ထပ် (${parseResult.items.size - 15}) ကွက် ကျန်ရှိပါသည်...",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Primary Save Slip Button
        item {
            Button(
                onClick = onSaveSlip,
                enabled = parseResult != null && parseResult.items.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = NavyDark,
                    disabledContainerColor = CardDarkElevated,
                    disabledContentColor = TextMuted
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_save_slip")
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (parseResult != null && parseResult.items.isNotEmpty()) {
                        "ဘောင်ချာ သိမ်းဆည်းမည် (${numberFormatter.format(parseResult.totalAmount)} $currency)"
                    } else {
                        "ဂဏန်းများ ထည့်သွင်းပြီး သိမ်းဆည်းမည်"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun ParsedBetRow(
    item: ParsedBetItem,
    currency: String,
    formatter: NumberFormat
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (item.isAboveMaxSingle) CrimsonWarning else GoldDark),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.number,
                    color = NavyDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = item.formulaType,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (item.isAboveMaxSingle) {
                Text(
                    text = "Limit ကျော်",
                    color = CrimsonLight,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
            Text(
                text = "${formatter.format(item.amount)} $currency",
                color = if (item.isAboveMaxSingle) CrimsonLight else TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
}
