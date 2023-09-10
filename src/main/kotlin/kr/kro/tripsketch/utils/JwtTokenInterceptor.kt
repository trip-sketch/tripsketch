package kr.kro.tripsketch.utils

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.kro.tripsketch.exceptions.ForbiddenException
import kr.kro.tripsketch.exceptions.UnauthorizedException
import kr.kro.tripsketch.services.JwtService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class JwtTokenInterceptor(
    private val jwtService: JwtService,
    @Value("\${admin.ids}") private val adminIdsConfig: String,
) : HandlerInterceptor {

    private val adminIds: List<Long> by lazy {
        adminIdsConfig.split(",").mapNotNull { it.trim().toLongOrNull() }
    }

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (request.requestURI.startsWith("/api/oauth/kakao/callback")) {
            return true
        }

        // Kakao OAuth 링크에 대한 요청인 경우 client_id 파라미터가 존재하는지만 확인합니다.
        if (request.requestURI.startsWith("/oauth/authorize") && request.getParameter("client_id") != null) {
            return true
        }

        val authorization = request.getHeader("Authorization") ?: throw UnauthorizedException("Authorization 헤더가 없습니다.")

        val token = authorization.removePrefix("Bearer ").trim()
        if (!jwtService.validateToken(token)) {
            throw UnauthorizedException("유효하지 않은 토큰입니다.")
        }

        val memberId: Long = jwtService.getMemberIdFromToken(token)
        request.setAttribute("memberId", memberId)

        // 관리자만 접근이 필요한 경로를 확인합니다. (예: /admin/이 포함된 경우)
        if (request.requestURI.contains("/admin/")) {
            if (!adminIds.contains(memberId)) {
                throw ForbiddenException("관리자만 접근 가능합니다.")
            }
        }

        return true
    }
}
