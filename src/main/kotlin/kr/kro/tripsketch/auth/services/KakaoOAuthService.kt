package kr.kro.tripsketch.auth.services

import kr.kro.tripsketch.commons.configs.KakaoOAuthConfig
import org.apache.logging.log4j.LogManager
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
    private val logger = LogManager.getLogger(KakaoOAuthService::class.java)

    /**
     * Kotlin의 `inline`, `reified`, 그리고 `object : Something()`에 대한 설명:
     *
     * 1. `inline`: 함수가 호출되는 지점에서 인라인으로 확장됩니다. 이는 성능 최적화를 위해 주로 사용되며, 함수 본문이 호출 지점에서 직접 삽입됩니다.
     *
     * 2. `reified`: 제네릭 유형 파라미터에만 사용되며, 실행 시간에 실제 유형 정보를 유지합니다. 일반적인 제네릭은 자바의 타입 소거로 인해 실행 시간에 실제 유형 정보를 잃어버리지만, `reified`를 사용하면 유형 정보를 유지할 수 있습니다.
     *
     * 3. `object : Something()`: Kotlin에서는 'object'를 사용하여 익명의 내부 클래스 인스턴스를 생성합니다. 여기서 `Something`은 상속받거나 구현하는 클래스 또는 인터페이스 이름입니다.
     *
     * 이 함수는 ParameterizedTypeReference의 익명 서브클래스의 인스턴스를 생성하여 제네릭 유형 T를 포함하는 ParameterizedTypeReference를 반환합니다. 주로 Spring Framework에서 제네릭 유형 정보를 유지하기 위해 사용됩니다.
     * @author Hojun Song
     */
    private inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> =
        object : ParameterizedTypeReference<T>() {}

    /** 카카오로부터 인증 토큰을 요청하는 데 사용되는 기본 함수 */

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

    /** 제공된 코드를 사용하여 카카오로부터 액세스 토큰과 리프레시 토큰을 가져옵니다. */
    fun getKakaoAccessToken(code: String): Pair<String?, String?> {

        logger.info("getKakaoAccessToken 시작, code: $code")

        val params = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", kakaoConfig.clientId)
            add("redirect_uri", kakaoConfig.redirectUri)
            add("code", code)
        }

        try {
            val response = requestKakaoToken(params)
            val accessToken = response?.get("access_token") as? String
            val refreshToken = response?.get("refresh_token") as? String

            logger.info("Access Token: $accessToken, Refresh Token: $refreshToken")
            return Pair(accessToken, refreshToken)
        } catch (e: Exception) {
            logger.error("getKakaoAccessToken 에서 에러 발생", e)
            throw e
        }
    }

    /** 카카오 리프레시 토큰을 사용하여 카카오로부터 새로운 액세스 토큰을 가져옵니다. */
    fun refreshAccessToken(refreshToken: String): Pair<String?, String?>? {
        val params = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "refresh_token")
            add("client_id", kakaoConfig.clientId)
            add("refresh_token", refreshToken)
        }

        val response = requestKakaoToken(params)
        val newAccessToken = response?.get("access_token") as? String
        val newRefreshToken = response?.get("refresh_token") as? String

        if (newAccessToken == null) return null
        return Pair(newAccessToken, newRefreshToken)
    }

    /** 액세스 토큰을 사용하여 카카오 사용자의 멤버 ID를 가져옵니다. */
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

    /** 액세스 토큰을 사용하여 카카오 사용자의 멤버 ID를 가져옵니다. */
    fun getMemberIdFromKakao(accessToken: String): Long? {
        val userInfo = getUserInfo(accessToken)
        return userInfo?.get("id") as? Long
    }

    /** 카카오 서버에서 사용자의 계정 연동을 해제합니다. */
    fun unlinkUser(accessToken: String?): Boolean {
        if (accessToken == null) {
            return false
        }

        val url = "https://kapi.kakao.com/v1/user/unlink"
        val headers = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            add("Content-Type", "application/x-www-form-urlencoded")
        }

        val request = HttpEntity<String>("", headers)

        return try {
            val response = restTemplate.exchange(url, HttpMethod.POST, request, typeRef<Map<String, Any>>())
            response.statusCode.is2xxSuccessful
        } catch (e: RestClientException) {
            false
        }
    }
}
