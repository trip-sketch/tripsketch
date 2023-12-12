package kr.kro.tripsketch.notification

import kr.kro.tripsketch.commons.exceptions.UnauthorizedException
import kr.kro.tripsketch.user.services.UserService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val userService: UserService,
    private val notificationRepository: NotificationRepository,
) {

    private val client = OkHttpClient()

    /**
     * 특정 회원(receiver)에게 온 알림을 페이지별로 조회합니다.
     * @author Hojun Song
     */

    fun getNotificationsByReceiverId(memberId: Long, page: Int, size: Int): Page<Notification> {
        val userId = userService.getUserIdByMemberId(memberId)
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val notificationsPage = notificationRepository.findByReceiverId(userId, pageable)

        val updatedNotifications = notificationsPage.content.map { notification ->
            notification.senderId?.let { senderId ->
                val senderUser = userService.findUserById(senderId)
                val currentNickname = senderUser?.nickname
                val currentProfileUrl = senderUser?.profileImageUrl
                notification.nickname = currentNickname
                notification.profileUrl = currentProfileUrl
            }
            notification
        }

        return PageImpl(updatedNotifications, pageable, notificationsPage.totalElements)
    }

    /**
     * 회원이 받은 특정 알림을 삭제합니다. 해당 회원이 알림의 수신자가 아닌 경우 예외가 발생합니다.
     */
    fun deleteNotificationById(notificationId: String, memberId: Long) {
        val userId = userService.getUserIdByMemberId(memberId)
        val notification = notificationRepository.findById(notificationId).orElse(null)
            ?: throw IllegalArgumentException("해당 알림을 찾을 수 없습니다.")

        if (notification.receiverId == userId) {
            notificationRepository.deleteById(notificationId)
        } else {
            throw UnauthorizedException("이 알림을 삭제할 권한이 없습니다.")
        }
    }

    /**
     * 특정 회원들에게 푸시 알림을 전송합니다. 해당 알림은 또한 데이터베이스에 저장됩니다.
     */
    fun sendPushNotification(
        ids: List<String>,
        title: String,
        body: String,
        senderId: String? = null,
        commentId: String? = null,
        parentId: String? = null,
        tripId: String? = null,
        nickname: String? = null,
        profileUrl: String? = null,
        content: String? = null,
    ): String {
        val tokens = ids.mapNotNull { getUserToken(it) }.toSet()

        // 알림 객체 생성
        ids.forEach { receiverId ->
            val notification = Notification(
                receiverId = receiverId,
                title = title,
                body = body,
                senderId = senderId,
                commentId = commentId,
                parentId = parentId,
                tripId = tripId,
                nickname = nickname,
                profileUrl = profileUrl,
                content = content,
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

    /**
     * 사용자의 푸시 토큰을 가져옵니다.
     */
    private fun getUserToken(id: String): String? {
        val user = userService.findUserById(id)
        return user?.expoPushToken
    }

    /**
     * Expo 서비스를 사용하여 푸시 알림을 전송합니다.
     */
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
