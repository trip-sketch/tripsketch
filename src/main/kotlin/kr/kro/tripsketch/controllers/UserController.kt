package kr.kro.tripsketch.controllers

import kr.kro.tripsketch.domain.User
import kr.kro.tripsketch.dto.AdditionalUserInfo
import kr.kro.tripsketch.dto.UserRegistrationDto
import kr.kro.tripsketch.dto.UserUpdateDto
import kr.kro.tripsketch.services.JwtService
import kr.kro.tripsketch.services.KakaoOAuthService
import kr.kro.tripsketch.services.NickNameService
import kr.kro.tripsketch.services.UserService
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/oauth")
class UserController(
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
    @GetMapping("/kakao/callback")
    fun kakaoCallback(@RequestParam code: String): ResponseEntity<Any> {
        val accessToken = kakaoOAuthService.getKakaoAccessToken(code)
        val userInfo = kakaoOAuthService.getUserInfo(accessToken) ?: return ResponseEntity.status(400).body("유효하지 않은 요청입니다.")

        val kakaoAccountInfo = userInfo["kakao_account"] as? Map<*, *>
        val email = kakaoAccountInfo?.get("email")?.toString() ?: return ResponseEntity.status(400).body("이메일 정보가 없습니다.")

        var user = userService.findUserByEmail(email)
        if (user == null) {
            // 회원 가입을 위한 추가 정보가 없는 경우, 임의의 값을 설정합니다.
            val additionalUserInfo = AdditionalUserInfo(
                nickname = nicknameService.generateRandomNickname(), // 임의의 닉네임
                profileImageUrl = "https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png", // 기본 프로필 이미지 URL
                introduction = "안녕하세요! 만나서 반갑습니다!", // 기본 소개 문구
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

    // 토큰값으로 사용자를 조회하는 메소드
    @GetMapping("/user")
    fun getUser(@RequestHeader("Authorization") token: String): ResponseEntity<Any> {
        val actualToken = token.removePrefix("Bearer ").trim() // "Bearer " 제거

        if (!jwtService.validateToken(actualToken)) { // 토큰 유효성 검증
            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.")
        }
        val email = jwtService.getEmailFromToken(actualToken) // 토큰에서 이메일 추출
        val user = userService.findUserByEmail(email) ?: return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다.")

        return ResponseEntity.ok(user)
    }

    // 토큰값으로 사용자 정보를 업데이트하는 메소드
    @PatchMapping("/user")
    fun updateUser(@RequestHeader("Authorization") token: String, @RequestBody userUpdateDto: UserUpdateDto): ResponseEntity<Any> {
        println("Received token: $token") // 토큰 출력

        val actualToken = token.removePrefix("Bearer ").trim() // "Bearer " 제거

        if (!jwtService.validateToken(actualToken)) { // 토큰 유효성 검증
            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.")
        }

        return try {
            val updatedUser = userService.updateUser(actualToken, userUpdateDto)
            ResponseEntity.ok(updatedUser)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(e.message)
        }
    }

    // 전체 사용자를 조회하는 메소드
    @GetMapping("/users")
    fun getAllUsers(): ResponseEntity<List<User>> {
        val users = userService.getAllUsers()
        return ResponseEntity.ok(users)
    }
}
