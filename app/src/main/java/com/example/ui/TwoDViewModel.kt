package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiOcrService
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BetItemEntity
import com.example.data.local.entity.DailySettlementEntity
import com.example.data.local.entity.SlipEntity
import com.example.data.model.DealerSettings
import com.example.data.model.NumberMatrixCell
import com.example.data.model.ParseResult
import com.example.data.model.SessionSettlement
import com.example.data.model.SessionType
import com.example.data.repository.BetRepository
import com.example.parser.TwoDParser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class TwoDViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BetRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BetRepository(db.betDao())
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDateString: String = dateFormat.format(Date())

    private val _selectedDate = MutableStateFlow(todayDateString)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedSession = MutableStateFlow(determineCurrentSession())
    val selectedSession: StateFlow<SessionType> = _selectedSession.asStateFlow()

    val settings: StateFlow<DealerSettings> = repository.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DealerSettings())

    private val sessionKeyFlow = combine(_selectedDate, _selectedSession) { date, session ->
        Pair(date, session)
    }

    val slips: StateFlow<List<SlipEntity>> = sessionKeyFlow.flatMapLatest { (date, session) ->
        repository.getSlipsForSession(date, session.name)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val betItems: StateFlow<List<BetItemEntity>> = sessionKeyFlow.flatMapLatest { (date, session) ->
        repository.getBetItemsForSession(date, session.name)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settlementEntity: StateFlow<DailySettlementEntity?> = sessionKeyFlow.flatMapLatest { (date, session) ->
        repository.getSettlement(date, session.name)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Calculated 100 Matrix Cells (00..99)
    val matrixCells: StateFlow<List<NumberMatrixCell>> = combine(
        betItems,
        settings
    ) { items, currentSettings ->
        val map = items.groupBy { it.number }
        val limit = currentSettings.maxBetAmount

        (0..99).map { i ->
            val numStr = String.format(Locale.US, "%02d", i)
            val numItems = map[numStr] ?: emptyList()
            val totalBet = numItems.sumOf { it.amount }
            val isOver = totalBet > limit
            val isNear = totalBet >= (limit * 0.8) && !isOver
            val retained = if (isOver) limit else totalBet
            val over = if (isOver) totalBet - limit else 0.0

            NumberMatrixCell(
                number = numStr,
                totalBet = totalBet,
                limit = limit,
                retainedAmount = retained,
                overAmount = over,
                isOverLimit = isOver,
                isNearLimit = isNear,
                betCount = numItems.size
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Overall Session Settlement (Turnover, Commission, Winning Payout, Net Profit)
    val sessionSettlement: StateFlow<SessionSettlement> = combine(
        slips,
        matrixCells,
        settlementEntity,
        settings
    ) { currentSlips, cells, settlement, currentSettings ->
        val date = _selectedDate.value
        val session = _selectedSession.value
        val totalTurnover = currentSlips.sumOf { it.totalAmount }
        val totalOverLimit = cells.sumOf { it.overAmount }
        val netRetained = totalTurnover - totalOverLimit

        val commissionRate = settlement?.commissionRate ?: currentSettings.commissionPercent
        val commissionIncome = netRetained * (commissionRate / 100.0)

        val winningNumber = settlement?.winningNumber
        val payoutMultiplier = settlement?.payoutMultiplier ?: currentSettings.payoutMultiplier

        // Payout is calculated on the dealer-retained amount of winning number
        val winningCell = if (!winningNumber.isNullOrBlank()) {
            cells.find { it.number == winningNumber.trim() }
        } else null

        val winningRetainedBet = winningCell?.retainedAmount ?: 0.0
        val totalPayout = winningRetainedBet * payoutMultiplier

        // Net Dealer Profit = Net Retained Inflow - Commission - Total Winning Payout
        val netAfterCommission = netRetained - commissionIncome
        val netProfitLoss = netAfterCommission - totalPayout

        SessionSettlement(
            date = date,
            sessionType = session,
            totalTurnover = totalTurnover,
            totalOverLimit = totalOverLimit,
            netRetained = netRetained,
            commissionRate = commissionRate,
            commissionIncome = commissionIncome,
            winningNumber = winningNumber,
            payoutMultiplier = payoutMultiplier,
            totalPayout = totalPayout,
            netProfitLoss = netProfitLoss,
            totalSlips = currentSlips.size,
            totalBetsCount = currentSlips.sumOf { it.betCount }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SessionSettlement(
            date = todayDateString,
            sessionType = SessionType.MORNING,
            totalTurnover = 0.0,
            totalOverLimit = 0.0,
            netRetained = 0.0,
            commissionRate = 10.0,
            commissionIncome = 0.0,
            winningNumber = null,
            payoutMultiplier = 80.0,
            totalPayout = 0.0,
            netProfitLoss = 0.0,
            totalSlips = 0,
            totalBetsCount = 0
        )
    )

    // UI Input State for Adding Slips
    private val _rawTextInput = MutableStateFlow("")
    val rawTextInput: StateFlow<String> = _rawTextInput.asStateFlow()

    private val _customerNameInput = MutableStateFlow("")
    val customerNameInput: StateFlow<String> = _customerNameInput.asStateFlow()

    private val _parseResult = MutableStateFlow<ParseResult?>(null)
    val parseResult: StateFlow<ParseResult?> = _parseResult.asStateFlow()

    private val _isOcrLoading = MutableStateFlow(false)
    val isOcrLoading: StateFlow<Boolean> = _isOcrLoading.asStateFlow()

    private val _isVoiceListening = MutableStateFlow(false)
    val isVoiceListening: StateFlow<Boolean> = _isVoiceListening.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    private val _overLimitAlertDetails = MutableStateFlow<List<String>?>(null)
    val overLimitAlertDetails: StateFlow<List<String>?> = _overLimitAlertDetails.asStateFlow()

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    fun selectSession(session: SessionType) {
        _selectedSession.value = session
    }

    fun onRawTextChanged(text: String) {
        _rawTextInput.value = text
        if (text.isNotBlank()) {
            _parseResult.value = TwoDParser.parseBetText(text, settings.value)
        } else {
            _parseResult.value = null
        }
    }

    fun onCustomerNameChanged(name: String) {
        _customerNameInput.value = name
    }

    fun appendQuickFormula(formula: String) {
        val current = _rawTextInput.value
        val updated = if (current.isBlank()) formula else "$current\n$formula"
        onRawTextChanged(updated)
    }

    fun clearInputForm() {
        _rawTextInput.value = ""
        _customerNameInput.value = ""
        _parseResult.value = null
    }

    fun saveCurrentSlip(onSaved: () -> Unit) {
        val result = _parseResult.value
        if (result == null || result.items.isEmpty()) {
            viewModelScope.launch {
                _toastMessage.emit("ကျေးဇူးပြု၍ မှန်ကန်သော 2D ထိုးဂဏန်းများ ထည့်သွင်းပါ")
            }
            return
        }

        viewModelScope.launch {
            val date = _selectedDate.value
            val session = _selectedSession.value.name
            val customer = _customerNameInput.value.ifBlank { "အမည်မသိ မိတ်ဆွေ" }

            // Check if any numbers in this slip cause new or higher over-limit state
            val existingCells = matrixCells.value
            val limit = settings.value.maxBetAmount
            val overLimitNotifications = mutableListOf<String>()

            val slipNumMap = result.items.groupBy { it.number }
            for ((num, items) in slipNumMap) {
                val currentTotal = existingCells.find { it.number == num }?.totalBet ?: 0.0
                val newSlipAmount = items.sumOf { it.amount }
                val combinedTotal = currentTotal + newSlipAmount
                if (combinedTotal > limit) {
                    val excess = combinedTotal - limit
                    overLimitNotifications.add("ဂဏန်း [$num] : စုစုပေါင်း (${combinedTotal.toInt()}) ဖြစ်သွားသဖြင့် သတ်မှတ် Limit (${limit.toInt()}) ထက် (${excess.toInt()} ${settings.value.currency}) ကျော်လွန်နေပါသည် (ဒိုင်မကိုင် / ပြန်ချရန် လိုအပ်)!")
                }
            }

            repository.saveSlip(
                customerName = customer,
                rawText = _rawTextInput.value,
                date = date,
                sessionType = session,
                items = result.items
            )

            clearInputForm()
            _toastMessage.emit("ဘောင်ချာ သိမ်းဆည်းပြီးပါပြီ (စုစုပေါင်း ${result.totalAmount.toInt()} ${settings.value.currency})")

            if (overLimitNotifications.isNotEmpty()) {
                _overLimitAlertDetails.value = overLimitNotifications
            }

            onSaved()
        }
    }

    fun dismissOverLimitAlert() {
        _overLimitAlertDetails.value = null
    }

    fun deleteSlip(slipId: Long) {
        viewModelScope.launch {
            repository.deleteSlip(slipId)
            _toastMessage.emit("ဘောင်ချာ ပယ်ဖျက်ပြီးပါပြီ")
        }
    }

    fun clearCurrentSession() {
        viewModelScope.launch {
            repository.clearSession(_selectedDate.value, _selectedSession.value.name)
            _toastMessage.emit("လက်ရှိ အချိန်ပိုင်း စာရင်းအားလုံး ရှင်းလင်းပြီးပါပြီ")
        }
    }

    fun setWinningNumber(winningNum: String) {
        val clean = winningNum.trim()
        if (clean.length == 2 && clean.all { it.isDigit() }) {
            viewModelScope.launch {
                val sett = settings.value
                repository.saveWinningNumber(
                    date = _selectedDate.value,
                    sessionType = _selectedSession.value.name,
                    winningNumber = clean,
                    multiplier = sett.payoutMultiplier,
                    commission = sett.commissionPercent
                )
                _toastMessage.emit("ပေါက်ဂဏန်း [$clean] ထည့်သွင်းပြီး အလျော်အစား တွက်ချက်ပြီးပါပြီ")
            }
        } else {
            viewModelScope.launch {
                _toastMessage.emit("ပေါက်ဂဏန်းသည် ၂ လုံး ဂဏန်း (၀၀-၉၉) ဖြစ်ရပါမည်")
            }
        }
    }

    fun updateSettings(newSettings: DealerSettings) {
        viewModelScope.launch {
            repository.updateSettings(newSettings)
            _toastMessage.emit("သတ်မှတ်ချက်များ သိမ်းဆည်းပြီးပါပြီ")
        }
    }

    fun processImageOcr(bitmap: Bitmap) {
        viewModelScope.launch {
            _isOcrLoading.value = true
            val result = GeminiOcrService.extractBetTextFromImage(bitmap)
            _isOcrLoading.value = false
            result.onSuccess { text ->
                if (text.isNotBlank()) {
                    val combined = if (_rawTextInput.value.isBlank()) text else "${_rawTextInput.value}\n$text"
                    onRawTextChanged(combined)
                    _toastMessage.emit("ပုံမှ စာသား အလိုအလျောက် ထုတ်ယူပြီးပါပြီ")
                } else {
                    _toastMessage.emit("ပုံမှ စာသား ရှာမတွေ့ပါ")
                }
            }.onFailure { error ->
                _toastMessage.emit("OCR အမှား: ${error.localizedMessage ?: "မအောင်မြင်ပါ"}")
            }
        }
    }

    fun setVoiceListeningState(isListening: Boolean) {
        _isVoiceListening.value = isListening
    }

    fun processVoiceResult(recognizedText: String) {
        val combined = if (_rawTextInput.value.isBlank()) recognizedText else "${_rawTextInput.value}\n$recognizedText"
        onRawTextChanged(combined)
        viewModelScope.launch {
            _toastMessage.emit("အသံကို စာသားအဖြစ် မှတ်တမ်းတင်ပြီးပါပြီ: $recognizedText")
        }
    }

    private fun determineCurrentSession(): SessionType {
        val now = java.util.Calendar.getInstance()
        val hour = now.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = now.get(java.util.Calendar.MINUTE)
        val timeInMinutes = hour * 60 + minute
        // 12:01 PM = 721 minutes. If before 12:15, MORNING; otherwise EVENING
        return if (timeInMinutes <= 12 * 60 + 15) {
            SessionType.MORNING
        } else {
            SessionType.EVENING
        }
    }
}
