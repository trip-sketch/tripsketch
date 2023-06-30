package kr.kro.tripsketch.controllers

import kr.kro.tripsketch.dto.UserDto
import kr.kro.tripsketch.services.KakaoOAuthService
import kr.kro.tripsketch.services.UserService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val kakaoOAuthService: KakaoOAuthService,
    private val userService: UserService
) {

    @PostMapping("/signup")
    fun signup(@RequestParam accessToken: String) {
        val userDTO: UserDto = kakaoOAuthService.getKakaoUserInfo(accessToken) ?: return
        userService.saveUser(userDTO)
    }
}
