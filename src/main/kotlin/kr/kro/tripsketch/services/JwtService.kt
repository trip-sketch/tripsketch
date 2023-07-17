package kr.kro.tripsketch.services

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import kr.kro.tripsketch.domain.User
import kr.kro.tripsketch.utils.EnvLoader
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.spec.SecretKeySpec
import javax.servlet.http.HttpServletRequest

@Service
class JwtService {
    private val secretKeyString = EnvLoader.getProperty("SECRET_KEY") ?: ""
    private val secretKey = SecretKeySpec(secretKeyString.toByteArray(), SignatureAlgorithm.HS256.jcaName)
    private val tokenValidityInMilliseconds: Long = EnvLoader.getProperty("TOKEN_VALIDITY")?.toLong() ?: 3600000 // 1 hour

    fun createToken(user: User): String {
        val claims = Jwts.claims().setSubject(user.email)
        claims["nickname"] = user.nickname

        val now = Date()
        val validity = Date(now.time + tokenValidityInMilliseconds)

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(secretKey)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token)
            return true
        } catch (e: Exception) {
            // 토큰 파싱에 실패하면 false를 반환합니다.
            return false
        }
    }

    fun getEmailFromToken(token: String): String {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).body.subject
    }

    fun getNicknameFromToken(token: String): String {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).body["nickname"].toString()
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
