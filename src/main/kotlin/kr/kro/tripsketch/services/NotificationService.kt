package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.Notification
import kr.kro.tripsketch.exceptions.UnauthorizedException
import kr.kro.tripsketch.repositories.NotificationRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val userService: UserService,
    private val notificationRepository: NotificationRepository,
) {

    private val client = OkHttpClient()

    fun getNotificationsByReceiverId(memberId: Long, page: Int, size: Int): Page<Notification> {
        val userId = userService.getUserIdByMemberId(memberId)
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"))
        return notificationRepository.findByReceiverId(userId, pageable)
    }

    fun deleteNotificationById(notificationId: String, memberId: Long) {
        val userId = userService.getUserIdByMemberId(memberId)
        val notification = notificationRepository.findById(notificationId).orElse(null)
            ?: throw IllegalArgumentException("해당 알림을 찾을 수 없습니다.")

        if (notification.receiverId == userId) { // 수정된 부분
            notificationRepository.deleteById(notificationId)
        } else {
            throw UnauthorizedException("이 알림을 삭제할 권한이 없습니다.")
        }
    }

    fun sendPushNotification(
        ids: List<String>,
        title: String,
        body: String,
        commentId: String? = null,
        parentId: String? = null,
        tripId: String? = null,
        nickname: String? = null,
        profileUrl: String? = null,
    ): String {
        val tokens = ids.mapNotNull { getUserToken(it) }.toSet()

        // 알림 객체 생성
        ids.forEach { receiverId ->
            val notification = Notification(
                receiverId = receiverId, // 수정된 부분
                title = title,
                body = body,
                commentId = commentId,
                parentId = parentId,
                tripId = tripId,
                nickname = nickname,
                profileUrl = profileUrl,
            )
            notificationRepository.save(notification)
        }

        val response = if (tokens.isNotEmpty()) {
            sendExpoPushNotification(tokens, title, body, commentId, parentId, tripId, nickname, profileUrl)
        } else {
            return "No valid push tokens found for the given emails."
        }

        return response.body?.string() ?: "No response body from Expo"
    }

    private fun getUserToken(id: String): String? {
        val user = userService.findUserById(id)
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
        profileUrl: String? = null,
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
