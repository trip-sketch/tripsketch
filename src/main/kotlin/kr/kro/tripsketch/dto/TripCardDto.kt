package kr.kro.tripsketch.dto

import java.time.LocalDateTime

data class TripCardDto(
    var id: String? = null,
    var nickname: String?,
    val profileImageUrl: String? = "",
    var title: String,
    var likes: Int?,
    var views: Int?,
    var comments: Int?,
    var countryCode: String? = "",
    var country: String? = "",
    val createdAt: LocalDateTime? = null,
    var image: String? = "",
    var isLiked: Boolean,
)
