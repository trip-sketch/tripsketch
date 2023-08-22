package kr.kro.tripsketch.dto

import java.time.LocalDateTime

data class TripDto(
    var id: String? = null,
    var userEmail: String,
    var nickname: String?,      // to-do: user Id 처럼 사용
    var title: String,
    var content: String,
    var likes: Int?,
    var views: Int?,
    var location: String? = null,
    var startedAt: LocalDateTime = LocalDateTime.now(),
    var endAt: LocalDateTime = LocalDateTime.now(),
    var hashtag: String? = null,
    var hidden: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime? = null,
    var deletedAt: LocalDateTime? = null,
//    var likeFlag: Int = 0,
    var tripViews: Set<String>? = setOf()
)
