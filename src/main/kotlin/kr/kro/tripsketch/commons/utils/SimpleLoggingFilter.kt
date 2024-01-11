package kr.kro.tripsketch.commons.utils

import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import io.highlight.sdk.Highlight
import io.highlight.sdk.common.HighlightOptions
import org.springframework.beans.factory.annotation.Value


@Component
class SimpleLoggingFilter : Filter {

    @Value("\${project.id}")
    lateinit var projectId: String

    private val logger: Logger = LoggerFactory.getLogger(SimpleLoggingFilter::class.java)

    override fun init(filterConfig: FilterConfig) {
        val options = HighlightOptions.builder(projectId).build()
        if (!Highlight.isInitialized()) {
            Highlight.init(options)
        }
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse

        try {
            chain.doFilter(request, response)
        } catch (e: Exception) {
            logger.error("Error occurred while filtering the request/response", e)
            Highlight.captureException(e)
        } finally {
            val logMessage = "[${httpResponse.status}] ${httpRequest.method} ${httpRequest.requestURI}"
            logger.info(logMessage)
            Highlight.captureLog(io.highlight.sdk.common.Severity.INFO, logMessage)
        }
    }

    override fun destroy() {}
}
