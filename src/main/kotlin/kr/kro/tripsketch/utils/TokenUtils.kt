package kr.kro.tripsketch.utils

import kr.kro.tripsketch.services.JwtService

class TokenException(message: String): RuntimeException(message)

object TokenUtils {
    fun validateAndExtractToken(jwtService: JwtService, token: String): String {
        val actualToken = removeBearerPrefix(token)
        if (!jwtService.validateToken(actualToken)) {
            throw TokenException("토큰이 유효하지 않습니다.")
        }
        return actualToken
    }

    private fun removeBearerPrefix(token: String): String {
        if (token.startsWith("Bearer ", ignoreCase = true)) {
            return token.substring("Bearer ".length).trim()
        }
        return token.trim()
    }
}
