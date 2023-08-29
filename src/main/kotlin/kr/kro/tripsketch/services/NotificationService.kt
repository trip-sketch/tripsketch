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

    fun sendPushNotification(emails: List<String>, title: String, body: String): String {
        val tokens = emails.mapNotNull { getUserToken(it) }
        return if (tokens.isNotEmpty()) {
            sendExpoPushNotification(tokens, title, body)
        } else {
            val errorMsg = "No valid push tokens found for the given emails."
            logger.warn(errorMsg)
            errorMsg
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
    ): String {
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

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    logger.info("Notification sent successfully!")
                    "Notification sent successfully!"  // 성공 메시지 반환
                } else {
                    val errorMsg = "Failed to send notification: ${response.body?.string()}"
                    logger.error(errorMsg)
                    errorMsg  // 에러 메시지 반환
                }
            }
        } catch (e: Exception) {
            val errorMsg = "Error sending notification: ${e.message}"
            logger.error(errorMsg, e)
            errorMsg  // 예외 발생 시 에러 메시지 반환
        }
    }
}