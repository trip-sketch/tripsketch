package kr.kro.tripsketch.notification.dtos

import kr.kro.tripsketch.notification.model.Notification

/**
 * @author Hojun Song
 */
data class ResponseFormat(
    val currentPage: Int,
    val notifications: List<Notification>,
    val notificationsPerPage: Int,
    val totalPage: Int,
)
