package kr.kro.tripsketch.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kr.kro.tripsketch.dto.UserDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class KakaoOAuthService {

    @Value("\${kakao.client-id}")
    private lateinit var kakaoClientId: String

    @Value("\${kakao.redirect-uri}")
    private lateinit var kakaoRedirectUri: String

    fun getKakaoUserInfo(code: String): UserDto {
        val restTemplate = RestTemplate()

        // Access Token Request
        val headers = HttpHeaders()
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
        val body = "grant_type=authorization_code&client_id=$kakaoClientId&redirect_uri=$kakaoRedirectUri&code=$code"
        val request = HttpEntity(body, headers)

        val responseEntity = restTemplate.exchange("https://kauth.kakao.com/oauth/token", HttpMethod.POST, request, String::class.java)

        // parse the JSON response
        val mapper = jacksonObjectMapper()
        val parsedResponse: Map<String, Any> = mapper.readValue(responseEntity.body.toString())

        val accessToken = parsedResponse["access_token"] as String
        val tokenType = parsedResponse["token_type"] as String

        // User Info Request
        val userHeaders = HttpHeaders()
        userHeaders.add("Authorization", "$tokenType $accessToken")
        userHeaders.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
        val userRequest = HttpEntity("parameters", userHeaders)

        val userResponseEntity = restTemplate.exchange("https://kapi.kakao.com/v2/user/me", HttpMethod.POST, userRequest, String::class.java)

        // parse the user JSON response
        val parsedUserResponse: Map<String, Any> = mapper.readValue(userResponseEntity.body.toString())

        val email = parsedUserResponse["email"] as String
        val nickname = parsedUserResponse["nickname"] as String
        val introduction = parsedUserResponse["introduction"] as String
        val profileImageUrl = parsedUserResponse["profile_image_url"] as String

        return UserDto(
            id = "",
            email = email,
            nickname = nickname,
            introduction = introduction,
            profileImageUrl = profileImageUrl
        )
    }
}
