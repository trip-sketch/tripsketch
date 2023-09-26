package kr.kro.tripsketch.dto

import jakarta.validation.constraints.Size

data class TripLikesDto(
    var id: String? = null,
    var tripLikesInfo: MutableSet<TripLikesUserInfo> = mutableSetOf(),
)

data class TripLikesUserInfo(
    var nickname: String?,
    val profileImageUrl: String? = "",
)
