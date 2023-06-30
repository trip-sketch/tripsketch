package kr.kro.tripsketch.dto

data class UserRegistrationDto(
    val email: String,
    val nickname: String,
    val profileImageUrl: String?,
    val introduction: String?
)
