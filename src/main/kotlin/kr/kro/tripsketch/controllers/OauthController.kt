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
@RequestMapping("api/oauth")
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

    // 카카오 Oauth2.0을 이용한 사용자 로그인/회원가입 기능
    @GetMapping("/kakao/login")
    fun kakaoLogin(@RequestParam code: String): ResponseEntity<Any> {
        val (accessToken, kakaoRefreshToken) = kakaoOAuthService.getKakaoAccessToken(code)

        if (kakaoRefreshToken == null) {
            return ResponseEntity.status(400).body("refreshToken을 얻지 못했습니다.")
        }

        val userInfo = kakaoOAuthService.getUserInfo(accessToken)
        val kakaoAccountInfo = userInfo?.get("kakao_account") as? Map<*, *>
        val email =
            kakaoAccountInfo?.get("email")?.toString() ?: return ResponseEntity.status(400).body("이메일 정보가 없습니다.")

        var user = userService.findUserByEmail(email)
        if (user == null) {
            var nickname: String
            do {
                nickname = nicknameService.generateRandomNickname()
            } while (userService.isNicknameExist(nickname))

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

        // 카카오 refreshToken 저장
        userService.updateKakaoRefreshToken(email, kakaoRefreshToken)

        val tokenResponse = jwtService.createTokens(user)

        userService.updateUserRefreshToken(email, tokenResponse.refreshToken)

        val headers = HttpHeaders()
        headers.add("Authorization", "Bearer ${tokenResponse.accessToken}")

        val responseBody = mapOf(
            "accessToken" to tokenResponse.accessToken,
            "refreshToken" to tokenResponse.refreshToken,
            "accessTokenExpiryDate" to tokenResponse.accessTokenExpiryDate,
            "refreshTokenExpiryDate" to tokenResponse.refreshTokenExpiryDate,
            "message" to "Success"
        )

        return ResponseEntity.ok().headers(headers).body(responseBody)
    }

    @PostMapping("/kakao/refresh")
    fun refreshKakaoToken(@RequestBody ourRefreshToken: String): ResponseEntity<Any> {
        // ourRefreshToken을 사용하여 사용자 찾기
        val user = userService.findByOurRefreshToken(ourRefreshToken) ?: return ResponseEntity.status(400)
            .body("Invalid refreshToken")

        // 사용자 정보에서 카카오의 refreshToken 가져와서 카카오 API 호출
        if (kakaoOAuthService.refreshAccessToken(user.kakaoRefreshToken!!) == null) {
            return ResponseEntity.status(400).body("Failed to refresh the accessToken")
        }

        val tokenResponse = jwtService.createTokens(user)

        val headers = HttpHeaders()
        headers.add("Authorization", "Bearer ${tokenResponse.accessToken}")

        val responseBody = mapOf(
            "accessToken" to tokenResponse.accessToken,
            "refreshToken" to tokenResponse.refreshToken,
            "accessTokenExpiryDate" to tokenResponse.accessTokenExpiryDate,
            "refreshTokenExpiryDate" to tokenResponse.refreshTokenExpiryDate,
            "message" to "Success"
        )

        return ResponseEntity.ok().headers(headers).body(responseBody)
    }
}

