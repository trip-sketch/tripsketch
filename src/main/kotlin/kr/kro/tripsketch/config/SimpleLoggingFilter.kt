package kr.kro.tripsketch.config

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class SimpleLoggingFilter : Filter {

    private val logger = LoggerFactory.getLogger(SimpleLoggingFilter::class.java)

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {

        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse

        // 요청 정보를 로그로 남깁니다.
        chain.doFilter(request, response)
        logger.info("[${httpResponse.status}] ${httpRequest.method} ${httpRequest.requestURI}")
    }

    override fun init(filterConfig: FilterConfig) {}

    override fun destroy() {}
}
