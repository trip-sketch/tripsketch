package kr.kro.tripsketch.dto

import java.time.LocalDateTime

data class TripCardWithKeywordDto(
    var id: String? = null,
    var nickname: String?,
    val profileImageUrl: String? = "",
    var title: String,
    var content: String? = "",
    var likes: Int?,
    var views: Int?,
    var comments: Int?,
    var countryCode: String? = "", // HashtagInfo
    var country: String? = "", // HashtagInfo
    val createdAt: LocalDateTime? = null,
    var image: String? = "",
    var isLiked: Boolean,
)
