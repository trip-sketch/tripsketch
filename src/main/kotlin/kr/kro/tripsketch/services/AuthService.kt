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

    fun authenticateViaKakao(code: String, pushToken: String? = null): TokenResponse? {
        val (accessToken, refreshToken) = kakaoOAuthService.getKakaoAccessToken(code)

        if (accessToken == null || refreshToken == null) {
            return null
        }

        val email = kakaoOAuthService.getEmailFromKakao(accessToken) ?: return null
        val user = userService.registerOrUpdateUser(email)
        val tokenResponse = jwtService.createTokens(user)
        userService.updateUserRefreshToken(email, tokenResponse.refreshToken)
        userService.updateKakaoRefreshToken(email, refreshToken)

        pushToken?.let {
            userService.storeUserPushToken(email, it)
        }

        return tokenResponse
    }

    fun refreshUserToken(request: KakaoRefreshRequest, pushToken: String? = null): TokenResponse? {
        val user = userService.findByOurRefreshToken(request.ourRefreshToken) ?: return null
        if (kakaoOAuthService.refreshAccessToken(user.kakaoRefreshToken!!) == null) return null

        val tokenResponse = jwtService.createTokens(user)

        pushToken?.let {
            userService.storeUserPushToken(user.email, it)
        }

        return tokenResponse
    }
}
