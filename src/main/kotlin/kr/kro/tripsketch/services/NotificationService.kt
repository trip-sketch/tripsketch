package kr.kro.tripsketch.services

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
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

    fun sendPushNotification(emails: List<String>, title: String, body: String): Response {
        val tokens = emails.mapNotNull { getUserToken(it) }
        return if (tokens.isNotEmpty()) {
            sendExpoPushNotification(tokens, title, body)
        } else {
            Response.Builder()
                .code(400)  // 예: 400번 코드로 설정
                .message("No valid push tokens found for the given emails.")
                .build()
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
    ): Response {
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

        return client.newCall(request).execute()
    }
}