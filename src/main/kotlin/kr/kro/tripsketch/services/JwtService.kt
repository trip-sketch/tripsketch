package kr.kro.tripsketch.services

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import jakarta.servlet.http.HttpServletRequest
import kr.kro.tripsketch.domain.User
import kr.kro.tripsketch.dto.TokenResponse
import kr.kro.tripsketch.exceptions.UnauthorizedException
import kr.kro.tripsketch.utils.EnvLoader
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.spec.SecretKeySpec

@Service
class JwtService {
    private val secretKeyString = EnvLoader.getProperty("SECRET_KEY") ?: ""
    private val secretKey = SecretKeySpec(secretKeyString.toByteArray(), SignatureAlgorithm.HS256.jcaName)
    private val accessTokenValidityInMilliseconds: Long =
        EnvLoader.getProperty("ACCESS_TOKEN_VALIDITY")?.toLong() ?: 3600000 // 1h
    private val refreshTokenValidityInMilliseconds: Long =
        EnvLoader.getProperty("REFRESH_TOKEN_VALIDITY")?.toLong() ?: 2592000000 // 30 days

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

    fun getMemberIdFromToken(token: String): Long {
        val subject = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).body.subject
        subject?.let {
            return it.toLong()
        } ?: throw UnauthorizedException("토큰에 memberId가 없습니다.")
    }

    fun resolveToken(req: HttpServletRequest): String? {
        val bearerToken = req.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7, bearerToken.length)
        } else {
            null
        }
    }
}
