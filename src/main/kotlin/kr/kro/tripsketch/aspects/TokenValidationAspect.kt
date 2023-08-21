package kr.kro.tripsketch.aspects

import jakarta.servlet.http.HttpServletRequest
import kr.kro.tripsketch.annotations.TokenValidation
import kr.kro.tripsketch.exceptions.CustomExpiredTokenException
import kr.kro.tripsketch.exceptions.UnauthorizedException
import kr.kro.tripsketch.exceptions.ForbiddenException
import kr.kro.tripsketch.services.JwtService
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Aspect
@Component
class TokenValidationAspect(
    private val jwtService: JwtService,
    @Value("\${admin.emails}") private val adminEmailsConfig: String
) {

    @Before("@annotation(tokenValidation)")
    fun validateToken(joinPoint: JoinPoint, tokenValidation: TokenValidation) {
        val args = joinPoint.args
        val req = args.find { it is HttpServletRequest } as? HttpServletRequest
            ?: throw UnauthorizedException("Request not found")

        val token = jwtService.resolveToken(req)
            ?: throw UnauthorizedException("토큰이 제공되지 않았습니다.")

        try {
            jwtService.validateToken(token)
        } catch (e: CustomExpiredTokenException) {
            throw UnauthorizedException("토큰이 만료되었습니다.")
        } catch (e: Exception) {
            throw UnauthorizedException("유효하지 않은 토큰입니다.")
        }

        val email = jwtService.getEmailFromToken(token)

        // email을 request attribute로 저장
        req.setAttribute("userEmail", email)

        if (tokenValidation.adminOnly) {
            val adminEmails = adminEmailsConfig.split(",")
            if (!adminEmails.contains(email)) {
                throw ForbiddenException("관리자만 접근 가능합니다.")
            }
        }
    }

}
