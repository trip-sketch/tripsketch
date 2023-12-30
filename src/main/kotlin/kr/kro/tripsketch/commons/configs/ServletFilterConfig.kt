package kr.kro.tripsketch.commons.configs

import jakarta.servlet.Filter
import kr.kro.tripsketch.commons.utils.SimpleLoggingFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 서블릿 필터를 설정하기 위한 설정 클래스입니다.
 *
 * 이 클래스는 @Configuration으로 주석 처리되어 있어, 서비스 레이어에서 스프링의 애플리케이션 컨텍스트를
 * 설정하는 데 사용될 것임을 나타냅니다.
 *
 * @author Hojun Song
 */

@Configuration
class ServletFilterConfig {

    /**
     * 로깅 필터를 등록하기 위한 Bean입니다.
     *
     * 이 메서드는 FilterRegistrationBean을 생성하고, SimpleLoggingFilter로 설정하며, 필터가 적용될
     * URL 패턴을 설정합니다. 필터는 URL 패턴 때문에 모든 요청에 적용됩니다.
     *
     * @param simpleLoggingFilter 미리 설정된 SimpleLoggingFilter
     * @return 필터를 등록할 준비가 된 설정된 FilterRegistrationBean
     * @author Hojun Song
     *
     */

    @Bean
    fun loggingFilter(simpleLoggingFilter: SimpleLoggingFilter): FilterRegistrationBean<*> {
        val registrationBean = FilterRegistrationBean<Filter>()

        registrationBean.filter = simpleLoggingFilter
        registrationBean.addUrlPatterns("/*")

        return registrationBean
    }
}
