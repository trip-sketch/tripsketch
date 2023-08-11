package kr.kro.tripsketch.dto

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val refreshTokenExpiryDate: Long,
    val message: String
)