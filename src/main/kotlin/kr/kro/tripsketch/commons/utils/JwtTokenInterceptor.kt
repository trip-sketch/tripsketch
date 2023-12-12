package kr.kro.tripsketch.commons.utils

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.kro.tripsketch.commons.exceptions.ForbiddenException
import kr.kro.tripsketch.commons.exceptions.UnauthorizedException
import kr.kro.tripsketch.auth.services.JwtService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

/**
 * JWT 토큰을 검증하는 인터셉터입니다.
 * 이 인터셉터는 HTTP 요청이 들어올 때 JWT 토큰의 유효성을 검사하고,
 * 해당 요청이 관리자 경로에 대한 것인지 확인하여 접근 권한을 확인합니다.
 * @author Hojun Song
 */
@Component
class JwtTokenInterceptor(
    private val jwtService: JwtService,
    @Value("\${admin.ids}") private val adminIdsConfig: String,
) : HandlerInterceptor {

    /**
     * 관리자 ID 목록을 초기화합니다.
     * admin.ids 설정 값에서 ID를 추출하고 리스트로 변환합니다.
     */
    private val adminIds: List<Long> by lazy {
        adminIdsConfig.split(",").mapNotNull { it.trim().toLongOrNull() }
    }

    /**
     * 요청이 처리되기 전에 JWT 토큰 유효성을 검사하고 권한을 확인합니다.
     *
     * @param request  현재 HTTP 요청 정보
     * @param response 현재 HTTP 응답 정보
     * @param handler  선택된 핸들러 객체(예: Controller 또는 API endpoint)
     * @return 요청이 다음 단계로 진행될지 여부
     */
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val authorization = request.getHeader("Authorization") ?: throw UnauthorizedException("Authorization 헤더가 없습니다.")

        val token = authorization.removePrefix("Bearer ").trim()
        if (!jwtService.validateToken(token)) {
            throw UnauthorizedException("유효하지 않은 토큰입니다.")
        }

        val memberId: Long = jwtService.getMemberIdFromToken(token)
        request.setAttribute("memberId", memberId)

        if (request.requestURI.contains("/admin/")) {
            if (!adminIds.contains(memberId)) {
                throw ForbiddenException("관리자만 접근 가능합니다.")
            }
        }

        return true
    }
}
