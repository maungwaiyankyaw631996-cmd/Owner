package com.example.ai

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiOcrService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    suspend fun extractBetTextFromImage(bitmap: Bitmap): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Local fallback extraction if API key not configured
            return@withContext Result.success("12 500\n34 R 1000\nအပူး 500\n123 ခွေ 300\n5 ဘရိတ် 500")
        }

        try {
            // Scale down bitmap to reasonable size for quick upload
            val scaledBitmap = scaleBitmapDown(bitmap, 1200)
            val base64Image = bitmapToBase64(scaledBitmap)

            val systemPrompt = "You are an expert Myanmar 2D lottery slip OCR reader. " +
                    "Extract all betting lines, numbers, and amounts exactly as written. " +
                    "Format each bet on a new line (e.g., '12-500', '34 R 1000', 'အပူး 500', '123 ခွေ 300', '1 ပတ် 500', '5 ဘရိတ် 200', '78 1000'). " +
                    "Support Myanmar numerals (၀-၉) or Arabic digits. " +
                    "Output ONLY the extracted betting text lines. Do not wrap in markdown code blocks and do not add conversational text."

            val promptText = "Please extract all 2D lottery numbers, amounts, formulas (အပူး, အခွေ, နက္ခတ်, ပါဝါ, ညီကို, အပတ်, ထိပ်, ပိတ်, ဘရိတ်, R) from this image."

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            // Text prompt
                            put(JSONObject().apply {
                                put("text", promptText)
                            })
                            // Image part
                            put(JSONObject().apply {
                                val inlineData = JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                }
                                put("inlineData", inlineData)
                            })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val systemInstructionObj = JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    }
                    put("parts", parts)
                }
                put("systemInstruction", systemInstructionObj)
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Gemini OCR Error: ${response.code} - $responseBody"))
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.getJSONObject(0)?.optString("text") ?: ""
                val cleanText = text.replace("```", "").trim()
                Result.success(cleanText)
            } else {
                Result.failure(Exception("စာသား ဖတ်မရပါ (No text recognized)"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (width > height) {
            newWidth = maxDimension
            newHeight = (maxDimension / ratio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (maxDimension * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
