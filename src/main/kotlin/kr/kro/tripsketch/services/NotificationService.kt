package kr.kro.tripsketch.services

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val userService: UserService
) {

    private val client = OkHttpClient()

    fun sendPushNotification(
        emails: Set<String>,
        title: String,
        body: String,
        commentId: String? = null,
        parentId: String? = null,
        tripId: String? = null,
        nickname: String? = null,
        profileUrl: String? = null
    ): String {
        val tokens = emails.mapNotNull { getUserToken(it) }.toSet()
        val response = if (tokens.isNotEmpty()) {
            sendExpoPushNotification(tokens, title, body, commentId, parentId, tripId, nickname, profileUrl)
        } else {
            return "No valid push tokens found for the given emails."
        }

        return response.body?.string() ?: "No response body from Expo"
    }

    private fun getUserToken(email: String): String? {
        val user = userService.findUserByEmail(email)
        return user?.expoPushToken
    }

    private fun sendExpoPushNotification(
        pushTokens: Set<String>,
        title: String,
        message: String,
        commentId: String? = null,
        parentId: String? = null,
        tripId: String? = null,
        nickname: String? = null,
        sound: String? = "default",
        badge: Int? = 1,
        profileUrl: String? = null
    ): Response {
        val jsonArray = JSONArray()

        pushTokens.forEach { token ->
            val dataJson = JSONObject().apply {
                commentId?.let { put("commentId", it) }
                parentId?.let { put("parentId", it) }
                tripId?.let { put("tripId", it) }
                nickname?.let { put("nickname", it) }
                profileUrl?.let { put("profileUrl", it) }
            }

            val json = JSONObject()
                .put("to", "ExponentPushToken[$token]")
                .put("title", title)
                .put("body", message)
                .put("data", dataJson)
                .put("badge", badge)

            if (sound != null && sound == "default") {
                json.put("sound", sound)
            }

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
