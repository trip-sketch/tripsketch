package kr.kro.refbook.config

import jakarta.servlet.Filter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ServletFilterConfig {

    @Bean
    fun loggingFilter(simpleLoggingFilter: SimpleLoggingFilter): FilterRegistrationBean<*> {
        val registrationBean = FilterRegistrationBean<Filter>()

        registrationBean.setFilter(simpleLoggingFilter)
        registrationBean.addUrlPatterns("/*")

        return registrationBean
    }
}
