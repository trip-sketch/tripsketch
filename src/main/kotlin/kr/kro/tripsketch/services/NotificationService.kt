package kr.kro.tripsketch.services

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

@Service
class NotificationService(
    private val userService: UserService
) {

    private val client = OkHttpClient()
    private val logger = LoggerFactory.getLogger(NotificationService::class.java)

    fun sendPushNotification(emails: List<String>, title: String, body: String) {
        val tokens = emails.mapNotNull { getUserToken(it) }
        if (tokens.isNotEmpty()) {
            sendExpoPushNotification(tokens, title, body)
        } else {
            logger.warn("No valid push tokens found for the given emails.")
        }
    }

    private fun getUserToken(email: String): String? {
        val user = userService.findUserByEmail(email)
        return user?.expoPushToken
    }

    private fun sendExpoPushNotification(
        pushTokens: List<String>,
        title: String,
        message: String,
        sound: String? = null,
        badge: Int? = null
    ) {
        val jsonArray = JSONArray()

        pushTokens.forEach { token ->
            val json = JSONObject()
                .put("to", "ExponentPushToken[$token]")
                .put("title", title)
                .put("body", message)

            sound?.let { json.put("sound", it) }
            badge?.let { json.put("badge", it) }

            jsonArray.put(json)
        }

        val requestBody = jsonArray.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("https://exp.host/--/api/v2/push/send")
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    logger.info("Notification sent successfully!")
                } else {
                    logger.error("Failed to send notification: ${response.body?.string()}")
                }
            }
        } catch (e: Exception) {
            logger.error("Error sending notification: ", e)
        }
    }
}
