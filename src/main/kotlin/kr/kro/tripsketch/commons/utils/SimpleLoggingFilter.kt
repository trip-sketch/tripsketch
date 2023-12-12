package kr.kro.tripsketch.commons.utils

import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component

/**
 * `SimpleLoggingFilter`는 요청과 응답에 대한 기본적인 로깅을 수행하는 필터입니다.
 * 이 필터는 각 요청의 메서드, URI 및 응답 상태 코드를 로그로 기록합니다.
 * 또한, 요청 또는 응답 처리 중 발생하는 예외도 로그로 기록됩니다.
 * @author Hojun Song
 */
@Component
class SimpleLoggingFilter : Filter {

    private val logger = LogManager.getLogger(SimpleLoggingFilter::class.java)

    /**
     * 실제 필터링 로직을 수행하는 메서드입니다.
     * 요청과 응답을 처리한 후, 해당 정보를 로그로 기록합니다.
     */
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse

        try {
            chain.doFilter(request, response)
        } catch (e: Exception) {
            logger.error("Error occurred while filtering the request/response", e)
        } finally {
            logger.info("[${httpResponse.status}] ${httpRequest.method} ${httpRequest.requestURI}")
        }
    }

    override fun init(filterConfig: FilterConfig) {}

    override fun destroy() {}
}
