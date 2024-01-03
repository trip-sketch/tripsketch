package kr.kro.tripsketch.auth.services

import kr.kro.tripsketch.auth.dtos.KakaoRefreshRequest
import kr.kro.tripsketch.auth.dtos.TokenResponse
import kr.kro.tripsketch.user.services.UserService
import org.apache.logging.log4j.LogManager
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
    private val logger = LogManager.getLogger(AuthService::class.java)
    /**
     * 카카오를 통해 인증 후 JWT 토큰 반환
     * @param code Kakao OAuth 인증 코드
     * @return TokenResponse JWT 토큰 응답
     * @author Hojun Song
     */
    fun authenticateViaKakao(code: String): TokenResponse? {

        logger.info("authenticateViaKakao 시작, code: $code")

        val (accessToken, refreshToken) = try {
            kakaoOAuthService.getKakaoAccessToken(code)
        } catch (e: Exception) {
            logger.error("Kakao AccessToken 획득 실패", e)
            return null
        }

        if (accessToken == null || refreshToken == null) {
            logger.info("AccessToken 또는 RefreshToken이 null입니다.")
            return null
        }

        val memberId = try {
            kakaoOAuthService.getMemberIdFromKakao(accessToken)
        } catch (e: Exception) {
            logger.error("Kakao로부터 memberId 획득 실패", e)
            return null
        } ?: run {
            logger.info("Kakao로부터 받은 memberId가 null입니다.")
            return null
        }

        val user = try {
            userService.registerOrUpdateUser(memberId)
        } catch (e: Exception) {
            logger.error("사용자 등록 또는 업데이트 실패", e)
            return null
        }

        try {
            user.updateLastLogin()
            userService.saveOrUpdate(user)

            val tokenResponse = jwtService.createTokens(user)
            userService.updateUserRefreshToken(memberId, tokenResponse.refreshToken)
            userService.updateKakaoRefreshToken(memberId, refreshToken)

            logger.info("TokenResponse 성공적으로 생성됨")
            return tokenResponse
        } catch (e: Exception) {
            logger.error("Token 생성 및 저장 중 에러 발생", e)
            return null
        }
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
