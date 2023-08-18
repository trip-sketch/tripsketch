package kr.kro.tripsketch.services

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val userService: UserService
) {

    private val client = OkHttpClient()

    fun sendPushNotification(email: String, title: String, body: String) {
        val userToken = getUserToken(email) ?: throw IllegalArgumentException("User token not found for $email")
        sendExpoPushNotification(userToken, title, body)
    }

    private fun getUserToken(email: String): String? {
        val user = userService.findUserByEmail(email)
        return user?.expoPushToken
    }

    private fun sendExpoPushNotification(pushToken: String, title: String, message: String) {
        val json = JSONObject()
            .put("to", pushToken)
            .put("title", title)
            .put("body", message)

        val requestBody = json.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("https://exp.host/--/api/v2/push/send")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("Failed to send notification: ${response.body?.string()}")
            }
        }
    }
}
