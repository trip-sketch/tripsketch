package kr.kro.tripsketch.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class KakaoOAuthConfig {

    @Value("\${kakao.client-id}")
    lateinit var clientId: String

    @Value("\${kakao.redirect-uri}")
    lateinit var redirectUri: String
}
