package kr.kro.tripsketch.notification

/**
 * @author Hojun Song
 */
data class ResponseFormat(
    val currentPage: Int,
    val notifications: List<Notification>,
    val notificationsPerPage: Int,
    val totalPage: Int,
)
