package kr.kro.tripsketch.utils

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.kro.tripsketch.services.JwtService
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

class JwtTokenInterceptor(private val jwtService: JwtService) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        // GET 메소드는 인증을 체크하지 않고 통과
        if (request.method.equals("GET", ignoreCase = true)) return true

        val authorization = request.getHeader("Authorization") ?: run {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return false
        }

        val token = authorization.removePrefix("Bearer ").trim()
        if (!jwtService.validateToken(token)) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return false
        }

        return true
    }
}
