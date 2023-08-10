package kr.kro.tripsketch.utils

import kr.kro.tripsketch.services.JwtService

object TokenUtils {
    fun validateAndExtractToken(jwtService: JwtService, token: String): String {
        val actualToken = token.removePrefix("Bearer ").trim()
        if (!jwtService.validateToken(actualToken)) {
            throw IllegalArgumentException("토큰이 유효하지 않습니다.")
        }
        return actualToken
    }
}
