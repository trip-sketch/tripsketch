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

@Service
class KakaoOAuthService(private val kakaoConfig: KakaoOAuthConfig) {

    private val restTemplate = RestTemplate()

    private inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> =
        object : ParameterizedTypeReference<T>() {}

    private fun requestKakaoToken(params: LinkedMultiValueMap<String, String>): Map<String, Any>? {
        val url = "https://kauth.kakao.com/oauth/token"
        val headers = HttpHeaders().apply {
            add("Content-Type", "application/x-www-form-urlencoded")
        }

        val request = HttpEntity(params, headers)

        return try {
            val response = restTemplate.exchange(url, HttpMethod.POST, request, typeRef<Map<String, Any>>())
            response.body
        } catch (e: RestClientException) {
            println("Error while requesting Kakao token: ${e.message}")
            null
        }
    }

    fun getKakaoAccessToken(code: String): Pair<String?, String?> {
        val params = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", kakaoConfig.clientId)
            add("redirect_uri", kakaoConfig.redirectUri)
            add("code", code)
        }

        val response = requestKakaoToken(params)
        val accessToken = response?.get("access_token") as? String
        val refreshToken = response?.get("refresh_token") as? String
        return Pair(accessToken, refreshToken)
    }

    fun refreshAccessToken(refreshToken: String): String? {
        val params = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "refresh_token")
            add("client_id", kakaoConfig.clientId)
            add("refresh_token", refreshToken)
        }

        val response = requestKakaoToken(params)
        return response?.get("access_token") as? String
    }

    fun getUserInfo(accessToken: String?): Map<String, Any>? {
        if (accessToken == null) return null

        val url = "https://kapi.kakao.com/v2/user/me"
        val headers = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            add("Content-Type", "application/json")
        }

        val request = HttpEntity<String>("", headers)

        return try {
            val response = restTemplate.exchange(url, HttpMethod.POST, request, typeRef<Map<String, Any>>())
            response.body
        } catch (e: RestClientException) {
            null
        }
    }

    fun getEmailFromKakao(accessToken: String): String? {
        val userInfo = getUserInfo(accessToken)
        val kakaoAccountInfo = userInfo?.get("kakao_account") as? Map<*, *>
        return kakaoAccountInfo?.get("sub") as? String
    }

}
