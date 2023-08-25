package kr.kro.tripsketch.services

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import kr.kro.tripsketch.domain.TokenCode
import kr.kro.tripsketch.dto.KakaoRefreshRequest
import kr.kro.tripsketch.dto.TokenResponse
import kr.kro.tripsketch.repositories.TokenCodeRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthService(
    private val kakaoOAuthService: KakaoOAuthService,
    private val userService: UserService,
    private val jwtService: JwtService,
    private val tokenCodeRepository: TokenCodeRepository,
) {

    fun authenticateViaKakao(code: String): TokenResponse? {
        val (accessToken, refreshToken) = kakaoOAuthService.getKakaoAccessToken(code)

        if (accessToken == null || refreshToken == null) {
            return null
        }

        val email = kakaoOAuthService.getEmailFromKakao(accessToken) ?: return null
        val user = userService.registerOrUpdateUser(email)
        val tokenResponse = jwtService.createTokens(user)
        userService.updateUserRefreshToken(email, tokenResponse.refreshToken)
        userService.updateKakaoRefreshToken(email, refreshToken)

        return tokenResponse
    }

    fun refreshUserToken(request: KakaoRefreshRequest): TokenResponse? {
        val user = userService.findByOurRefreshToken(request.ourRefreshToken) ?: return null
        if (kakaoOAuthService.refreshAccessToken(user.kakaoRefreshToken!!) == null) return null

        return jwtService.createTokens(user)
    }

    fun generateOneTimeCodeForToken(tokenResponse: TokenResponse): String {
        val oneTimeCode = UUID.randomUUID().toString()
        val tokenCode = TokenCode(
            oneTimeCode = oneTimeCode,
            accessToken = tokenResponse.accessToken,
            refreshToken = tokenResponse.refreshToken,
            refreshTokenExpiryDate = tokenResponse.refreshTokenExpiryDate
        )
        tokenCodeRepository.save(tokenCode)
        return oneTimeCode
    }

    fun retrieveTokenByOneTimeCode(oneTimeCode: String, pushToken: String): TokenResponse? {
        val tokenCodeEntity = tokenCodeRepository.findByOneTimeCode(oneTimeCode) ?: return null

        val email = kakaoOAuthService.getEmailFromKakao(tokenCodeEntity.accessToken)
        email?.let {
            userService.storeUserPushToken(it, pushToken)
        }

        tokenCodeRepository.deleteById(oneTimeCode) // 일회용 코드 삭제

        return TokenResponse(
            accessToken = tokenCodeEntity.accessToken,
            refreshToken = tokenCodeEntity.refreshToken,
            refreshTokenExpiryDate = tokenCodeEntity.refreshTokenExpiryDate
        )
    }

    fun setAuthenticationCookies(response: HttpServletResponse, tokenResponse: TokenResponse) {
        setCookie(response, "accessToken", tokenResponse.accessToken)
        setCookie(response, "refreshToken", tokenResponse.refreshToken)
        setCookie(response, "refreshTokenExpiryDate", tokenResponse.refreshTokenExpiryDate.toString())
    }

    private fun setCookie(response: HttpServletResponse, name: String, value: String) {
        val cookie = Cookie(name, value)
        configureCookie(cookie)
        response.addCookie(cookie)
    }

    private fun configureCookie(cookie: Cookie) {
        cookie.path = "/"
        cookie.secure = true
    }

}
