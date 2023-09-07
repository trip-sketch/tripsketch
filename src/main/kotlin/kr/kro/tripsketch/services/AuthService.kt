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

        val id = kakaoOAuthService.getIdFromKakao(accessToken) ?: return null
        val user = userService.registerOrUpdateUser(id)

        // Update last login time for user
        user.updateLastLogin()
        userService.saveOrUpdate(user)

        val tokenResponse = jwtService.createTokens(user)
        userService.updateUserRefreshToken(id, tokenResponse.refreshToken)
        userService.updateKakaoRefreshToken(id, refreshToken)

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
