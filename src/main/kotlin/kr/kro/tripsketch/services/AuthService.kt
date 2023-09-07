package kr.kro.tripsketch.services

import kr.kro.tripsketch.dto.KakaoRefreshRequest
import kr.kro.tripsketch.dto.TokenResponse
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val kakaoOAuthService: KakaoOAuthService,
    private val userService: UserService,
    private val jwtService: JwtService,
) {

    fun authenticateViaKakao(code: String): TokenResponse? {
        val (accessToken, refreshToken) = kakaoOAuthService.getKakaoAccessToken(code)

        if (accessToken == null || refreshToken == null) {
            return null
        }

        val email = kakaoOAuthService.getEmailFromKakao(accessToken) ?: return null

        val kakaoId = kakaoOAuthService.getIdFromKakao(accessToken)
        println("Kakao User ID: $kakaoId")

        val user = userService.registerOrUpdateUser(email)

        user.updateLastLogin()
        userService.saveOrUpdate(user)

        val tokenResponse = jwtService.createTokens(user)
        userService.updateUserRefreshToken(email, tokenResponse.refreshToken)
        userService.updateKakaoRefreshToken(email, refreshToken)

        return tokenResponse
    }

    fun refreshUserToken(request: KakaoRefreshRequest): TokenResponse? {
        val user = userService.findByOurRefreshToken(request.ourRefreshToken) ?: return null
        if (kakaoOAuthService.refreshAccessToken(user.kakaoRefreshToken!!) == null) return null

        // Update last login time for user
        user.updateLastLogin()
        userService.saveOrUpdate(user)

        return jwtService.createTokens(user)
    }
}
