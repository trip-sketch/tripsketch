package kr.kro.tripsketch.dto

import java.time.LocalDateTime

data class TripDto(
    var id: String? = null,
    var email: String?,
    var nickname: String?,
    var title: String,
    var content: String,
    var likes: Int?,
    var views: Int?,
    var location: String? = null,
    var startedAt: LocalDateTime,
    var endAt: LocalDateTime,
    var hashtag: Set<String>? = setOf(),
    var latitude: Double? = null, // 위도
    var longitude: Double? = null, // 경도
    var isPublic: Boolean,
    var isHidden: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime? = null,
    var deletedAt: LocalDateTime? = null,
    var tripLikes: MutableSet<String> = mutableSetOf(),
//    var tripViews: MutableSet<String> = mutableSetOf(),
    var images: List<String>? = emptyList(),
    var isLiked: Boolean
)
