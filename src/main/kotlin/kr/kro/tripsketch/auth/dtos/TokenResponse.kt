package kr.kro.tripsketch.auth.dtos

/**
 * @author Hojun Song
 */
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val refreshTokenExpiryDate: Long,
)
