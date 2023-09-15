package kr.kro.tripsketch.dto

/**
 * @author Hojun Song
 */
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val refreshTokenExpiryDate: Long,
)
