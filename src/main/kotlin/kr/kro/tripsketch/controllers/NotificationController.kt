package kr.kro.tripsketch.controllers

import jakarta.servlet.http.HttpServletRequest
import kr.kro.tripsketch.domain.Notification
import kr.kro.tripsketch.dto.NotificationRequest
import kr.kro.tripsketch.exceptions.UnauthorizedException
import kr.kro.tripsketch.services.NotificationService
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService,
) {
    @GetMapping
    fun getNotificationsByReceiverId(
        req: HttpServletRequest,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<Page<Notification>> {
        val memberId = req.getAttribute("memberId") as Long? ?: throw UnauthorizedException("인증되지 않은 사용자입니다.")

        return ResponseEntity.ok(notificationService.getNotificationsByReceiverId(memberId, page, size))
    }

    @DeleteMapping("/{notificationId}")
    fun deleteNotificationById(req: HttpServletRequest, @PathVariable notificationId: String): ResponseEntity<Void> {
        val memberId = req.getAttribute("memberId") as Long? ?: throw UnauthorizedException("인증되지 않은 사용자입니다.")
        notificationService.deleteNotificationById(notificationId, memberId)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @PostMapping("/send")
    fun sendNotification(@RequestBody notificationRequest: NotificationRequest): ResponseEntity<String> {
        val expoResponseMessage = notificationService.sendPushNotification(
            listOf(notificationRequest.userId),
            notificationRequest.title,
            notificationRequest.body,
            notificationRequest.commentId,
            notificationRequest.parentId,
            notificationRequest.tripId,
            notificationRequest.nickname,
        )

        return if (expoResponseMessage == "Notification sent successfully!") {
            ResponseEntity.ok(expoResponseMessage)
        } else {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(expoResponseMessage)
        }
    }
}
