package kr.kro.tripsketch.services

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import kr.kro.tripsketch.domain.User
import kr.kro.tripsketch.dto.TokenResponse
import kr.kro.tripsketch.exceptions.UnauthorizedException
import kr.kro.tripsketch.utils.EnvLoader
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.spec.SecretKeySpec

@Service
class JwtService {
    private val secretKeyString = EnvLoader.getProperty("SECRET_KEY") ?: UUID.randomUUID().toString()
    private val secretKey = SecretKeySpec(secretKeyString.toByteArray(), SignatureAlgorithm.HS256.jcaName)
    private val accessTokenValidityInMilliseconds: Long =
        EnvLoader.getPropertyOrDefault("ACCESS_TOKEN_VALIDITY", "3600000").toLong()
    private val refreshTokenValidityInMilliseconds: Long =
        EnvLoader.getPropertyOrDefault("REFRESH_TOKEN_VALIDITY", "2592000000").toLong()
    /**
     * 사용자 정보를 바탕으로 Access Token과 Refresh Token을 생성합니다.
     *
     * @param user 사용자 정보 객체.
     * @return 생성된 토큰에 대한 응답 객체 ([TokenResponse]).
     * @author Hojun Song
     *
     */
    fun createTokens(user: User): TokenResponse {
        val now = Date()

        user.memberId

        /**
         * Access Token 생성
         */
        val accessTokenValidity = Date(now.time + accessTokenValidityInMilliseconds)
        val accessToken = Jwts.builder()
            .setSubject(user.memberId.toString())
            .claim("nickname", user.nickname)
            .setIssuedAt(now)
            .setExpiration(accessTokenValidity)
            .signWith(secretKey)
            .compact()

        /** Refresh Token 생성 */
        val refreshTokenValidity = Date(now.time + refreshTokenValidityInMilliseconds)
        val refreshToken = Jwts.builder()
            .setSubject(user.memberId.toString())
            .setId(UUID.randomUUID().toString())
            .setIssuedAt(now)
            .setExpiration(refreshTokenValidity)
            .signWith(secretKey)
            .compact()

        return TokenResponse(accessToken, refreshToken, refreshTokenValidity.time)
    }

    /**
     * 제공된 토큰의 유효성을 검사합니다.
     *
     * 만료된 토큰 또는 유효하지 않은 토큰에 대해 예외를 발생시킵니다.
     *
     * @param token 검사할 토큰 문자열.
     * @return 토큰의 유효성 결과 (유효한 경우 true, 그렇지 않은 경우 false).
     * @throws UnauthorizedException 유효하지 않은 토큰의 경우 발생.
     */

    fun validateToken(token: String): Boolean {
        return try {
            val claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).body
            claims.subject ?: throw UnauthorizedException("토큰에 memberId가 없습니다.")
            true
        } catch (e: ExpiredJwtException) {
            throw UnauthorizedException("토큰이 만료되었습니다.")
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 제공된 토큰에서 사용자의 memberId를 추출합니다.
     *
     * @param token memberId를 추출할 토큰 문자열.
     * @return 토큰에서 추출된 memberId.
     * @throws UnauthorizedException 토큰에 memberId가 없는 경우 발생.
     */
    fun getMemberIdFromToken(token: String): Long {
        val subject = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).body.subject
        subject?.let {
            return it.toLong()
        } ?: throw UnauthorizedException("토큰에 memberId가 없습니다.")
    }

}
