package com.example.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

class VoiceRecognitionHelper(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onListeningStateChanged: (Boolean) -> Unit
) {

    private var speechRecognizer: SpeechRecognizer? = null

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("စက်တွင် အသံဖမ်း Speech Recognizer စနစ် မပါရှိပါ")
            return
        }

        stopListening()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    onListeningStateChanged(true)
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    onListeningStateChanged(false)
                }

                override fun onError(error: Int) {
                    onListeningStateChanged(false)
                    val message = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "အသံဖမ်းယူရာတွင် ချို့ယွင်းချက်ဖြစ်ပေါ်ပါသည်"
                        SpeechRecognizer.ERROR_CLIENT -> "ချိတ်ဆက်မှု မအောင်မြင်ပါ"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "မိုက်ခရိုဖုန်း ခွင့်ပြုချက် လိုအပ်ပါသည်"
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "အင်တာနက် လိုင်း မကောင်းပါ"
                        SpeechRecognizer.ERROR_NO_MATCH -> "အသံ မသဲကွဲပါ၊ ထပ်မံ ပြောဆိုပေးပါ"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "လုပ်ဆောင်နေဆဲ ဖြစ်ပါသည်"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "အသံ မကြားရပါ"
                        else -> "အသံဖမ်းယူမှု အမှား ($error)"
                    }
                    onError(message)
                }

                override fun onResults(results: Bundle?) {
                    onListeningStateChanged(false)
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        onResult(matches[0])
                    } else {
                        onError("အသံကို စာသားအဖြစ် မပြောင်းနိုင်ပါ")
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        onResult(matches[0])
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "my-MM")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "my-MM")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "2D ထိုးဂဏန်းနှင့် ထိုးကြေးကို ပြောပါ (ဥပမာ- ၁၂ ၅၀၀၊ ၃၄ R ၁၀၀၀၊ အပူး ၂၀၀)...")
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            onListeningStateChanged(false)
            onError("အသံဖမ်းခြင်း မစတင်နိုင်ပါ: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            // Ignore clean up errors
        } finally {
            speechRecognizer = null
            onListeningStateChanged(false)
        }
    }
}
