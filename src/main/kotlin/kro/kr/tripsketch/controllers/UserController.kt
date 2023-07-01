package kr.kro.tripsketch.controllers

import kr.kro.tripsketch.dto.UserLoginDto
import kr.kro.tripsketch.dto.UserRegistrationDto
import kr.kro.tripsketch.services.KakaoOAuthService
import kr.kro.tripsketch.services.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/oauth")
class UserController(
    private val kakaoOAuthService: KakaoOAuthService,
    private val userService: UserService
) {

    @GetMapping("/kakao/signup")
    fun kakaoSignUp(@RequestParam code: String): ResponseEntity<String> {
        val accessToken = kakaoOAuthService.getKakaoAccessToken(code)
        val userInfo = kakaoOAuthService.getUserInfo(accessToken)

        if (userInfo == null) {
            return ResponseEntity.status(400).body("유효하지 않은 요청입니다.")
        }

        val email = userInfo["email"] as String?
        val nickname = userInfo["nickname"] as String?
        val profileImageUrl = (userInfo["profile_image"] as Map<String, Any>?)?.get("url") as String?
        val introduction = userInfo["introduction"] as String?

        if (email != null && nickname != null) {
            val userRegistrationDto = UserRegistrationDto(email, nickname, profileImageUrl, introduction)
            val user = userService.registerUser(userRegistrationDto)
            return ResponseEntity.ok("회원가입 성공: ${user.nickname}")
        }

        return ResponseEntity.status(400).body("이메일 또는 닉네임이 없습니다.")
    }

    @GetMapping("/kakao/login")
    fun kakaoLogin(@RequestParam code: String): ResponseEntity<String> {
        val accessToken = kakaoOAuthService.getKakaoAccessToken(code)
        val userInfo = kakaoOAuthService.getUserInfo(accessToken)

        if (userInfo == null) {
            return ResponseEntity.status(400).body("유효하지 않은 요청입니다.")
        }

        val email = userInfo["email"] as String?
        if (email != null) {
            val userLoginDto = UserLoginDto(email)
            val user = userService.loginUser(userLoginDto)
            if (user != null) {
                return ResponseEntity.ok("로그인 성공: ${user.nickname}")
            } else {
                return ResponseEntity.status(404).body("회원 정보가 없습니다. 먼저 회원가입을 해주세요.")
            }
        }

        return ResponseEntity.status(400).body("이메일 정보가 없습니다.")
    }
}
