package kr.kro.tripsketch.dto

import java.time.LocalDateTime

data class TripDto(
    var id: String? = null,
    var userId: String,
    var scheduleId: String,
    var title: String,
    var content: String,
    var likes: Int,
    var views: Int,
    var location: String? = null,
    var startedAt: LocalDateTime = LocalDateTime.now(),
    var endAt: LocalDateTime = LocalDateTime.now(),
    var hashtag: String,
    var hidden: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime? = null,
    var deletedAt: LocalDateTime? = null,
    var likeFlg: Int = 0,
    var tripViews: Set<String> = setOf(),
)
