package kr.kro.tripsketch.utils

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.kro.tripsketch.services.JwtService
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

class JwtTokenInterceptor(private val jwtService: JwtService) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val handlerMethod = handler as? HandlerMethod ?: return true
        val authorization = request.getHeader("Authorization") ?: return false
        val token = authorization.removePrefix("Bearer ").trim()

        return jwtService.validateToken(token)
    }
}
