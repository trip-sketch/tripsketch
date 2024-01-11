package kr.kro.tripsketch.auth.services

import kr.kro.tripsketch.auth.OauthController
import kr.kro.tripsketch.auth.dtos.KakaoRefreshRequest
import kr.kro.tripsketch.auth.dtos.TokenResponse
import kr.kro.tripsketch.user.services.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
    private val logger: Logger = LoggerFactory.getLogger(AuthService::class.java)
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
        val timestamp = System.currentTimeMillis()
        logger.error("[$timestamp] 2.1 리프레시 토큰으로 유저 찾기 시도: ${request.ourRefreshToken}")
        val user = userService.findByOurRefreshToken(request.ourRefreshToken) ?: return null.also {
            logger.error("[$timestamp] 2.2 유저 찾기 실패: 해당 리프레시 토큰 ${request.ourRefreshToken}")
        }

        logger.error("[$timestamp] 3.1 카카오 토큰 갱신 시도: KakaoRefreshToken=${user.kakaoRefreshToken}")
        val (newAccessToken, newKakaoRefreshToken) = kakaoOAuthService.refreshAccessToken(user.kakaoRefreshToken!!) ?: return null.also {
            logger.error("[$timestamp] 3.2 카카오 토큰 갱신 실패: KakaoRefreshToken=${user.kakaoRefreshToken}")
        }

        if (newKakaoRefreshToken != null) {
            logger.error("[$timestamp] 4.1 카카오 리프레시 토큰 업데이트")
            userService.updateKakaoRefreshToken(user.memberId, newKakaoRefreshToken)
        }

        logger.error("[$timestamp] 5.1 유저 마지막 로그인 업데이트")
        user.updateLastLogin()

        logger.error("[$timestamp] 6.1 JWT 토큰 생성 중")
        val tokenResponse = jwtService.createTokens(user)

        user.ourRefreshToken = tokenResponse.refreshToken
        userService.saveOrUpdate(user)
        logger.error("[$timestamp] 7.1 유저 정보 업데이트 완료: memberId=${user.memberId}")

        return tokenResponse
    }


}
