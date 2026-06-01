package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiHelper {
    private const val TAG = "GeminiHelper"
    private const val MODEL_NAME = "gemini-3.5-flash"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val SYSTEM_INSTRUCTION = 
        "You are 'SRG Bot', the expert virtual AI assistant exclusively for 'SRG car hire'. " +
        "Your sole role is to answer questions about renting cars, vehicle bookings, " +
        "verification of electronic rental agreements, explaining dynamic pricing, locations, and how our " +
        "loyalty rewards program works. You must maintain a helpful, premium, and professional tone. " +
        "Always invite citizens/users to visit our official web booking portal at www.srgcarhire.co.ke. " +
        "Do NOT assist with unrelated topics (e.g., coding, general knowledge, other companies); " +
        "politely guide them back to renting an exquisite Tesla, Porsche, Audi, Range Rover or BMW with SRG car hire. " +
        "Keep your answers concise, engaging, and easy to read (max 3 short paragraphs)."

    suspend fun getChatResponse(prompt: String, chatHistory: List<Pair<String, Boolean>>): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        Log.d(TAG, "Getting chat response. Key is empty? ${apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY"}")

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "null") {
            return@withContext getSmartMockResponse(prompt)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"
            
            val jsonBody = JSONObject().apply {
                // Contents
                val contentsArray = JSONArray()
                
                // Add conversation history
                chatHistory.forEach { (messageText, isUser) ->
                    val contentObj = JSONObject().apply {
                        put("role", if (isUser) "user" else "model")
                        val partsArr = JSONArray().apply {
                            put(JSONObject().apply { put("text", messageText) })
                        }
                        put("parts", partsArr)
                    }
                    contentsArray.put(contentObj)
                }
                
                // Add current prompt
                val currentPromptObj = JSONObject().apply {
                    put("role", "user")
                    val partsArr = JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    }
                    put("parts", partsArr)
                }
                contentsArray.put(currentPromptObj)
                
                put("contents", contentsArray)

                // System Instruction
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", SYSTEM_INSTRUCTION) })
                    })
                })

                // Generation Config
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.95)
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .header("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val code = response.code
                    val errorMsg = response.body?.string() ?: ""
                    Log.e(TAG, "Unsuccessful response from Gemini API: Code $code - $errorMsg")
                    return@withContext "I'm having a brief connection issue with my central server. However, as an SRG Car Hire assistant, I can confirm we have premium cars like the Porsche 911 GT3 RS, Tesla Model S, and Audi R8 V10 available at Mayfair, Soho, and Kensington! Please feel free to proceed with dynamic booking!"
                }

                val bodyStr = response.body?.string() ?: return@withContext "Sorry, I received an empty response from my system. Please try again!"
                val responseJson = JSONObject(bodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "I'm ready to assist with your SRG car hire needs!")
                        }
                    }
                }
                return@withContext "I'm standing by to help you reserve your next sports vehicle with SRG car hire."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini API call", e)
            return@withContext getSmartMockResponse(prompt)
        }
    }

    private fun getSmartMockResponse(prompt: String): String {
        val query = prompt.lowercase()
        return when {
            query.contains("tesla") || query.contains("electric") || query.contains("plaid") -> {
                "Our **Tesla Model S Plaid** is an incredible choice! Under our **dynamic pricing**, it is currently available for **$95.00/hour** due to demand. It features a tri-motor setup, going from 0 to 60 in under 2 seconds. You can locate it at **Mayfair Central Hub** using the real-time map, sign the digital rental agreement, and unlock it instantly!"
            }
            query.contains("porsche") || query.contains("gt3") || query.contains("911") -> {
                "The legendary **Porsche 911 GT3 RS** is available at **Soho Luxury Suites** at **$145.00/hour**. This street-legal racing machine produces a roaring 9,000 RPM. Booking requires digital verification of your driving license, followed by secure payment in the app to issue and activate the remote GPS unlock."
            }
            query.contains("audi") || query.contains("r8") || query.contains("convertible") || query.contains("v10") -> {
                "Ah! The **Audi R8 V10 Spyder** convertible. Currently running a special **dynamic discount at $120.00/hour** (normally $125). It has an unparalleled V10 engine that sounds perfect with the top down. Located in **Kensington Boulevard**."
            }
            query.contains("pricing") || query.contains("price") || query.contains("how much") || query.contains("cost") -> {
                "At **SRG car hire**, we use custom **dynamic pricing updates** based on demand, vehicle status, and hour of the day. For example, peak weekend hours might slightly adjust rates, while mid-week slots feature exciting discounts! You can see real-time rates directly on each vehicle card."
            }
            query.contains("loyalty") || query.contains("reward") || query.contains("points") || query.contains("points") || query.contains("silver") || query.contains("gold") -> {
                "Our **SRG Loyalty Program** rewards you for every mile and hour! You immediately earn **10 points per dollar spent**. Points elevate your tier:\n" +
                "- **Silver Tier**: Standard premium access.\n" +
                "- **Gold Tier**: (At 400 pts) 10% discount on all rentals!\n" +
                "- **Platinum Tier**: (At 1000 pts) 20% discount plus VIP priority delivery.\n" +
                "You can redeem points on checkout!"
            }
            query.contains("map") || query.contains("gps") || query.contains("where") || query.contains("nearby") || query.contains("locate") -> {
                "You can see nearby cars in real-time on our **interactive map**. The map shows live positions of vehicles (with real-time GPS tracking simulation). You can tap any vehicle icon to select it, check its rating, view dynamic prices, and lock in your reservation."
            }
            query.contains("verification") || query.contains("verify") || query.contains("agreement") || query.contains("sign") || query.contains("license") -> {
                "To ensure maximum safety, SRG car hire incorporates **Automated Digital Verification**. When you book, you'll be shown our digital rental agreement. Simply enter your driving license number, sign with your initials/signature, and our automated backend verifies the documents instantly so you can drive!"
            }
            query.contains("pay") || query.contains("payment") || query.contains("card") || query.contains("checkout") -> {
                "We integrate a **secure payment gateway** within the app. Upon completing document verification, a premium credit/debit card gateway interface will process your transaction safely. An instant push notification will update your reservation status!"
            }
            query.contains("admin") || query.contains("edit") || query.contains("passcode") || query.contains("secret") || query.contains("panel") -> {
                "To access executive features like updating prices, renaming cars, or adding inventory, use our **Hidden Edit Panel** on the main sidebar/app bar! Simply enter our secure administrative passcode: **SRGADMIN**."
            }
            else -> {
                "Hello! I am **SRG Bot**, your virtual concierge. I hear you loud and clear! I can help you locate vehicles on our interactive GPS map, explain our dynamic pricing, guide you through electronic agreement signing, and assist with bookings. Feel free to explore more or reserve online at our official web portal: **www.srgcarhire.co.ke**!"
            }
        }
    }
}
