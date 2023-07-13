package kr.kro.refbook.config

import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component
import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

@Component
class SimpleLoggingFilter : Filter {

    private val logger = LogManager.getLogger(SimpleLoggingFilter::class.java)

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
