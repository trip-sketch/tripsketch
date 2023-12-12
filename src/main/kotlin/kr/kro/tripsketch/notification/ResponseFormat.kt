package kr.kro.tripsketch.notification

import kr.kro.tripsketch.notification.Notification

/**
 * @author Hojun Song
 */
data class ResponseFormat(
    val currentPage: Int,
    val notifications: List<Notification>,
    val notificationsPerPage: Int,
    val totalPage: Int,
)
