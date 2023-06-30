package kr.kro.tripsketch.services

import kr.kro.tripsketch.config.KakaoOAuthConfig
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@Service
class KakaoOAuthService(private val kakaoConfig: KakaoOAuthConfig) {

    fun getKakaoAccessToken(code: String): String? {
        val restTemplate = RestTemplate()

        val url = "https://kauth.kakao.com/oauth/token" // Kakao 토큰 발급 엔드포인트

        // 파라미터 설정
        val params = mutableMapOf<String, String>()
        params["grant_type"] = "authorization_code"
        params["client_id"] = kakaoConfig.clientId
        params["redirect_uri"] = kakaoConfig.redirectUri
        params["code"] = code

        return try {
            val response = restTemplate.postForEntity(url, params, Map::class.java)
            response.body?.get("access_token") as String?
        } catch (e: HttpClientErrorException) {
            if (e.statusCode == HttpStatus.BAD_REQUEST) {
                // 오류 처리 (예: 잘못된 인증 코드)
            }
            null
        }
    }

    fun getUserInfo(accessToken: String?): Map<String, Any>? {
        if (accessToken == null) return null

        val restTemplate = RestTemplate()

        val url = "https://kapi.kakao.com/v2/user/me" // Kakao 사용자 정보 엔드포인트

        val headers = mutableMapOf<String, String>()
        headers["Authorization"] = "Bearer $accessToken"

        return try {
            val response = restTemplate.postForEntity(url, headers, Map::class.java)
            response.body as Map<String, Any>?
        } catch (e: HttpClientErrorException) {
            if (e.statusCode == HttpStatus.BAD_REQUEST) {
                // 오류 처리 (예: 잘못된 액세스 토큰)
            }
            null
        }
    }
}
