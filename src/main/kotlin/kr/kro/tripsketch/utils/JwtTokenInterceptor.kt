package kr.kro.tripsketch.utils

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.kro.tripsketch.exceptions.UnauthorizedException
import kr.kro.tripsketch.exceptions.ForbiddenException
import kr.kro.tripsketch.services.JwtService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class JwtTokenInterceptor(
    private val jwtService: JwtService,
    @Value("\${admin.emails}") private val adminEmailsConfig: String
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val authorization = request.getHeader("Authorization") ?: throw UnauthorizedException("Authorization 헤더가 없습니다.")

        val token = authorization.removePrefix("Bearer ").trim()
        if (!jwtService.validateToken(token)) {
            throw UnauthorizedException("유효하지 않은 토큰입니다.")
        }

        val email = jwtService.getEmailFromToken(token)
        request.setAttribute("userEmail", email)

        // 관리자만 접근이 필요한 경로를 확인합니다. (예: /admin/이 포함된 경우)
        if (request.requestURI.contains("/admin/")) {
            if (!adminEmailsConfig.split(",").contains(email)) {
                throw ForbiddenException("관리자만 접근 가능합니다.")
            }
        }

        return true
    }
}
