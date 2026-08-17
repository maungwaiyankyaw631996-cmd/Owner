package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DealerSettings
import com.example.data.model.NumberMatrixCell
import com.example.data.model.ParseResult
import com.example.data.model.SessionSettlement
import com.example.data.model.SessionType
import com.example.ui.TwoDViewModel
import com.example.ui.components.OverLimitAlertDialog
import com.example.ui.components.SessionHeader
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.AddSlipScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.Matrix100Screen
import com.example.ui.screens.SettlementScreen
import com.example.ui.screens.SlipsListScreen
import com.example.ui.theme.CardDark
import com.example.ui.theme.CrimsonWarning
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.collectLatest

enum class AppScreen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("ပင်မ", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    ADD_SLIP("+ ဘောင်ချာ", Icons.Filled.AddCircle, Icons.Filled.AddCircle),
    MATRIX("၁၀၀ ကွက်", Icons.Filled.GridOn, Icons.Outlined.GridOn),
    SLIPS("စလစ်များ", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    SETTLEMENT("ရှင်းတမ်း", Icons.Filled.Calculate, Icons.Outlined.Calculate)
}

class MainActivity : ComponentActivity() {

    private val viewModel: TwoDViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: TwoDViewModel) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // States from ViewModel
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val selectedSession by viewModel.selectedSession.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val slips by viewModel.slips.collectAsStateWithLifecycle()
    val matrixCells by viewModel.matrixCells.collectAsStateWithLifecycle()
    val settlement by viewModel.sessionSettlement.collectAsStateWithLifecycle()
    val overLimitAlertDetails by viewModel.overLimitAlertDetails.collectAsStateWithLifecycle()

    // Add Slip state
    val rawTextInput by viewModel.rawTextInput.collectAsStateWithLifecycle()
    val customerNameInput by viewModel.customerNameInput.collectAsStateWithLifecycle()
    val parseResult by viewModel.parseResult.collectAsStateWithLifecycle()
    val isOcrLoading by viewModel.isOcrLoading.collectAsStateWithLifecycle()
    val isVoiceListening by viewModel.isVoiceListening.collectAsStateWithLifecycle()

    val overLimitCount = matrixCells.count { it.isOverLimit }

    // Toast listener
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = NavyDark,
        topBar = {
            if (currentScreen == AppScreen.DASHBOARD || currentScreen == AppScreen.MATRIX) {
                SessionHeader(
                    selectedDate = selectedDate,
                    selectedSession = selectedSession,
                    onDateSelected = { viewModel.selectDate(it) },
                    onSessionSelected = { viewModel.selectSession(it) },
                    onOpenSettings = { showSettingsDialog = true }
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                contentColor = TextPrimary,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                AppScreen.values().forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            if (screen == AppScreen.MATRIX && overLimitCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = CrimsonWarning) {
                                            Text("$overLimitCount", color = NavyDark, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            }
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavyDark,
                            selectedTextColor = GoldPrimary,
                            indicatorColor = GoldPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(NavyDark)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    AppScreen.DASHBOARD -> {
                        DashboardScreen(
                            settlement = settlement,
                            matrixCells = matrixCells,
                            settings = settings,
                            onNavigateToAddSlip = { currentScreen = AppScreen.ADD_SLIP },
                            onNavigateToMatrix = { currentScreen = AppScreen.MATRIX },
                            onNavigateToSlips = { currentScreen = AppScreen.SLIPS },
                            onNavigateToSettlement = { currentScreen = AppScreen.SETTLEMENT },
                            onSetWinningNumber = { viewModel.setWinningNumber(it) }
                        )
                    }

                    AppScreen.ADD_SLIP -> {
                        AddSlipScreen(
                            rawText = rawTextInput,
                            customerName = customerNameInput,
                            parseResult = parseResult,
                            settings = settings,
                            isOcrLoading = isOcrLoading,
                            isVoiceListening = isVoiceListening,
                            onRawTextChanged = { viewModel.onRawTextChanged(it) },
                            onCustomerNameChanged = { viewModel.onCustomerNameChanged(it) },
                            onAppendFormula = { viewModel.appendQuickFormula(it) },
                            onClearForm = { viewModel.clearInputForm() },
                            onSaveSlip = {
                                viewModel.saveCurrentSlip(
                                    onSaved = { currentScreen = AppScreen.DASHBOARD }
                                )
                            },
                            onBack = { currentScreen = AppScreen.DASHBOARD },
                            onProcessImageOcr = { viewModel.processImageOcr(it) },
                            onProcessVoiceResult = { viewModel.processVoiceResult(it) },
                            onVoiceListeningStateChanged = { viewModel.setVoiceListeningState(it) }
                        )
                    }

                    AppScreen.MATRIX -> {
                        Matrix100Screen(
                            cells = matrixCells,
                            settings = settings,
                            onBack = { currentScreen = AppScreen.DASHBOARD }
                        )
                    }

                    AppScreen.SLIPS -> {
                        SlipsListScreen(
                            slips = slips,
                            settings = settings,
                            onDeleteSlip = { viewModel.deleteSlip(it) },
                            onClearAllSlips = { viewModel.clearCurrentSession() },
                            onBack = { currentScreen = AppScreen.DASHBOARD }
                        )
                    }

                    AppScreen.SETTLEMENT -> {
                        SettlementScreen(
                            settlement = settlement,
                            matrixCells = matrixCells,
                            settings = settings,
                            onSetWinningNumber = { viewModel.setWinningNumber(it) },
                            onBack = { currentScreen = AppScreen.DASHBOARD }
                        )
                    }
                }
            }
        }
    }

    // Over-limit Popup Alert
    if (overLimitAlertDetails != null) {
        OverLimitAlertDialog(
            details = overLimitAlertDetails!!,
            currency = settings.currency,
            onDismiss = { viewModel.dismissOverLimitAlert() }
        )
    }

    // Settings Modal
    if (showSettingsDialog) {
        SettingsDialog(
            currentSettings = settings,
            onSave = {
                viewModel.updateSettings(it)
                showSettingsDialog = false
            },
            onDismiss = { showSettingsDialog = false }
        )
    }
}
