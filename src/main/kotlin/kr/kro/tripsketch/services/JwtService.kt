package kr.kro.tripsketch.services

import org.springframework.stereotype.Service
import kr.kro.tripsketch.domain.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.util.Date

@Service
class JwtService {
    private val secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256)
    private val tokenValidityInMilliseconds: Long = 3600000  // 1 hour for example

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
}
