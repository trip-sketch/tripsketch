package kr.kro.tripsketch.config

import kr.kro.tripsketch.services.JwtService
import kr.kro.tripsketch.utils.JwtTokenInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(private val jwtService: JwtService) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(JwtTokenInterceptor(jwtService))
            .addPathPatterns("/hello") // 인증이 필요한 라우터 설정
    }
}
