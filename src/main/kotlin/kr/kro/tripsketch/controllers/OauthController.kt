package kr.kro.tripsketch.controllers

import kr.kro.tripsketch.dto.AdditionalUserInfo
import kr.kro.tripsketch.dto.UserRegistrationDto
import kr.kro.tripsketch.services.JwtService
import kr.kro.tripsketch.services.KakaoOAuthService
import kr.kro.tripsketch.services.NickNameService
import kr.kro.tripsketch.services.UserService
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/oauth")
class OauthController(
    private val kakaoOAuthService: KakaoOAuthService,
    private val userService: UserService,
    private val jwtService: JwtService,
    private val nicknameService: NickNameService,
) {

    @GetMapping("/kakao/code")
    fun kakaoCode(@RequestParam code: String): ResponseEntity<Any> {
        return ResponseEntity.ok().body(mapOf("code" to code))
    }

    // 카카오 Oauth2.0을 이용한 사용자 로그인/회원가입 콜백함수
    @GetMapping("/kakao/login")
    fun kakaoLogin(@RequestParam code: String): ResponseEntity<Any> {
        val accessToken = kakaoOAuthService.getKakaoAccessToken(code)
        val userInfo = kakaoOAuthService.getUserInfo(accessToken) ?: return ResponseEntity.status(400).body("유효하지 않은 요청입니다.")

        val kakaoAccountInfo = userInfo["kakao_account"] as? Map<*, *>
        val email = kakaoAccountInfo?.get("email")?.toString() ?: return ResponseEntity.status(400).body("이메일 정보가 없습니다.")

        var user = userService.findUserByEmail(email)
        if (user == null) {
            var nickname: String
            do {
                nickname = nicknameService.generateRandomNickname()
            } while (userService.isNicknameExist(nickname)) // 닉네임 중복 체크

            val additionalUserInfo = AdditionalUserInfo(
                nickname = nickname,
                profileImageUrl = "https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png",
                introduction = "안녕하세요! 만나서 반갑습니다!",
            )
            val userRegistrationDto = UserRegistrationDto(
                email,
                additionalUserInfo.nickname,
                additionalUserInfo.profileImageUrl,
                additionalUserInfo.introduction,
            )
            user = userService.registerUser(userRegistrationDto)
        }

        val jwt = jwtService.createToken(user)
        val headers = HttpHeaders()
        headers.add("Authorization", "Bearer $jwt")
        return ResponseEntity.ok().headers(headers).body(mapOf("message" to "Success"))
    }

}
