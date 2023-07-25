package kr.kro.tripsketch.services

import kr.kro.tripsketch.config.KakaoOAuthConfig
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

@Service
class KakaoOAuthService(private val kakaoConfig: KakaoOAuthConfig) {


    fun getKakaoAccessToken(code: String): String? {
        val restTemplate = RestTemplate()

        val url = "https://kauth.kakao.com/oauth/token"

        val params = LinkedMultiValueMap<String, String>()
        params.add("grant_type", "authorization_code")
        params.add("client_id", kakaoConfig.clientId)
        params.add("redirect_uri", kakaoConfig.redirectUri)
        params.add("code", code)

        val headers = HttpHeaders()
        headers.add("Content-Type", "application/x-www-form-urlencoded")

        val request = HttpEntity(params, headers)

        return try {
            val response = restTemplate.exchange(url, HttpMethod.POST, request, typeRef<Map<String, Any>>())
            val body = response.body
            body?.get("access_token") as? String
        } catch (e: RestClientException) {
            println("Error while requesting access token: ${e.message}")
            null
        }
    }

    fun getUserInfo(accessToken: String?): Map<String, Any>? {
        if (accessToken == null) return null

        val restTemplate = RestTemplate()

        val url = "https://kapi.kakao.com/v2/user/me"

        val headers = HttpHeaders()
        headers.add("Authorization", "Bearer $accessToken")
        headers.add("Content-Type", "application/json")

        val request = HttpEntity<String>("", headers)

        return try {
            val response = restTemplate.exchange(url, HttpMethod.POST, request, typeRef<Map<String, Any>>())
            response.body
        } catch (e: RestClientException) {
            // 오류 처리
            null
        }
    }
}
