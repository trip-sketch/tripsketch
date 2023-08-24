package kr.kro.tripsketch.config

import kr.kro.tripsketch.services.JwtService
import kr.kro.tripsketch.utils.JwtTokenInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * 이 클래스는 Web MVC를 구성하는 데 사용되며 JWT (JSON Web Token) 인증을 위한 인터셉터를 추가합니다.
 *
 * <p>{@link WebMvcConfig} 클래스는 JWT 토큰 인터셉터를 설정하는데 책임이 있습니다. 이 인터셉터는 "/hello" 엔드포인트에 적용됩니다.
 * 따라서 "/hello" 엔드포인트로의 모든 요청은 JWT를 사용한 인증이 필요합니다. JWT 토큰 인터셉터는 {@link JwtService} 인스턴스를
 * 사용하여 JWT 유효성 검사와 인증을 수행합니다.
 *
 * <p>이 클래스는 {@link WebMvcConfigurer} 인터페이스를 구현하여 Web MVC 구성을 사용자 정의할 수 있게 합니다.
 * 특히 {@link WebMvcConfigurer#addInterceptors} 메서드를 오버라이드하여 JWT 토큰 인터셉터를 인터셉터 레지스트리에 추가합니다.
 *
 * <p>사용 예시:
 * <pre class="code">
 * &#64;Configuration
 * class WebMvcConfig(private val jwtService: JwtService) : WebMvcConfigurer {
 *     override fun addInterceptors(registry: InterceptorRegistry) {
 *         registry.addInterceptor(JwtTokenInterceptor(jwtService))
 *                 .addPathPatterns("/hello") // "/hello" 엔드포인트에 인증이 필요한 설정
 *     }
 * }
 * </pre>
 *
 *
 * @param jwtService JWT 토큰 유효성 검사와 인증에 사용되는 {@link JwtService} 인스턴스
 * @see JwtService
 * @see JwtTokenInterceptor
 * @see WebMvcConfigurer
 */


@Configuration
class WebMvcConfig(
    private val jwtTokenInterceptor: JwtTokenInterceptor
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(jwtTokenInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(
                "/swagger-ui/**",
                "/v3/**",
                "/hello",
                "/api/user/nickname",
                "/api/follow/followings",
                "/api/follow/followers",
                "/api/oauth/kakao/**",
                "/api/oauth/kakao/callback/**",
                "/api/comment/guest/**",
            )
    }
}

