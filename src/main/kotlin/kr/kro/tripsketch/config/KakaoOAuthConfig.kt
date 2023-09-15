package kr.kro.tripsketch.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

/**
 * 카카오 Oauth 설정을 위한 값 설정
 *
 * @author Hojun Song
 */
@Configuration
class KakaoOAuthConfig {
    @Value("\${kakao.client-id}")
    lateinit var clientId: String

    @Value("\${kakao.redirect-uri}")
    lateinit var redirectUri: String
}
