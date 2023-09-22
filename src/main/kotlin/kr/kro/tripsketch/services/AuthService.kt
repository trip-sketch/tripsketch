package kr.kro.tripsketch.services

import kr.kro.tripsketch.dto.KakaoRefreshRequest
import kr.kro.tripsketch.dto.TokenResponse
import org.springframework.stereotype.Service

/**
 * AuthService: 사용자 인증 관련 서비스입니다.
 *
 * - Kakao OAuth를 통해 인증 및 리프레시 토큰을 가져오는 로직 포함
 * - Kakao OAuth를 통해 얻은 정보로 사용자 정보를 등록 및 업데이트 하는 로직 포함
 * @author Hojun Song
 */
@Service
class AuthService(
    private val kakaoOAuthService: KakaoOAuthService,
    private val userService: UserService,
    private val jwtService: JwtService,
) {

    /**
     * 카카오를 통해 인증 후 JWT 토큰 반환
     * @param code Kakao OAuth 인증 코드
     * @return TokenResponse JWT 토큰 응답
     * @author Hojun Song
     */
    fun authenticateViaKakao(code: String): TokenResponse? {
        val (accessToken, refreshToken) = kakaoOAuthService.getKakaoAccessToken(code)

        if (accessToken == null || refreshToken == null) {
            return null
        }

        val memberId = kakaoOAuthService.getMemberIdFromKakao(accessToken) ?: return null
        val user = userService.registerOrUpdateUser(memberId)

        user.updateLastLogin()
        userService.saveOrUpdate(user)

        val tokenResponse = jwtService.createTokens(user)
        userService.updateUserRefreshToken(memberId, tokenResponse.refreshToken)
        userService.updateKakaoRefreshToken(memberId, refreshToken)

        return tokenResponse
    }

    /**
     * 사용자의 우리 서비스의 리프레시 토큰을 통해 새로운 JWT 토큰 반환
     * @param request KakaoRefreshRequest 카카오 리프레시 토큰 요청 객체
     * @return TokenResponse JWT 토큰 응답
     * @author Hojun Song
     */

    fun refreshUserToken(request: KakaoRefreshRequest): TokenResponse? {
        val user = userService.findByOurRefreshToken(request.ourRefreshToken) ?: return null

        val (newAccessToken, newKakaoRefreshToken) = kakaoOAuthService.refreshAccessToken(user.kakaoRefreshToken!!) ?: return null
        if (newAccessToken == null) return null

        if (newKakaoRefreshToken != null) {
            userService.updateKakaoRefreshToken(user.memberId, newKakaoRefreshToken)
        }

        user.updateLastLogin()

        val tokenResponse = jwtService.createTokens(user)

        user.ourRefreshToken = tokenResponse.refreshToken
        userService.saveOrUpdate(user)

        return tokenResponse
    }
}
