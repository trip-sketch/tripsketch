package kr.kro.tripsketch.notification

import jakarta.servlet.http.HttpServletRequest
import kr.kro.tripsketch.commons.exceptions.UnauthorizedException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 알림 관련 요청을 처리하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService,
) {

    /**
     * 특정 사용자에게 전송된 알림을 페이지별로 가져오는 메서드입니다.
     */
    @GetMapping
    fun getNotificationsByReceiverId(
        req: HttpServletRequest,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<ResponseFormat> {
        val memberId = req.getAttribute("memberId") as Long? ?: throw UnauthorizedException("인증되지 않은 사용자입니다.")

        val originalResponse = notificationService.getNotificationsByReceiverId(memberId, page, size)

        val response = ResponseFormat(
            currentPage = originalResponse.number + 1,
            notifications = originalResponse.content,
            notificationsPerPage = originalResponse.size,
            totalPage = originalResponse.totalPages,
        )

        return ResponseEntity.ok(response)
    }

    /**
     * 알림을 삭제하는 메서드입니다.
     */
    @DeleteMapping("/{notificationId}")
    fun deleteNotificationById(req: HttpServletRequest, @PathVariable notificationId: String): ResponseEntity<Void> {
        val memberId = req.getAttribute("memberId") as Long? ?: throw UnauthorizedException("인증되지 않은 사용자입니다.")
        notificationService.deleteNotificationById(notificationId, memberId)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
