package kr.kro.tripsketch.dto

data class UserDto(
    val id: String?,
    val email: String,
    val nickname: String?,
    val introduction: String?,
    val profileImageUrl: String?
)
