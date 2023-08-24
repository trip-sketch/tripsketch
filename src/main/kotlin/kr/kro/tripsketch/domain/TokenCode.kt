package kr.kro.tripsketch.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "token_codes")
data class TokenCode(
    @Id
    val oneTimeCode: String,
    val accessToken: String,
    val refreshToken: String,
    val refreshTokenExpiryDate: Long
)