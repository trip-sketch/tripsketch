package kr.kro.tripsketch.dto

import java.time.LocalDateTime

data class TripCardDto(
    var id: String? = null,
    var email: String?,
    var nickname: String?,
    var title: String,
    var likes: Int?,
    var views: Int?,
    var location: String? = null,
    var startedAt: LocalDateTime = LocalDateTime.now(),
    var endAt: LocalDateTime = LocalDateTime.now(),
    var hashtag: Set<String>? = setOf(),
    var public: Boolean,
    var hidden: Boolean = false,
    var tripLikes: MutableSet<String> = mutableSetOf(),
    var images: List<String>? = emptyList(),
    var isLiked: Boolean
)
