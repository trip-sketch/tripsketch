package kr.kro.tripsketch.controllers

import kr.kro.tripsketch.dto.KakaoRefreshRequest
import kr.kro.tripsketch.services.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/oauth")
class OauthController(
    private val authService: AuthService
) {

    @GetMapping("/kakao/code")
    fun kakaoCode(@RequestParam code: String): ResponseEntity<Any> {
        return ResponseEntity.ok().body(mapOf("code" to code))
    }

    @GetMapping("/kakao/login")
    fun kakaoLogin(@RequestParam code: String): ResponseEntity<Any> {
        val tokenResponse = authService.authenticateViaKakao(code)
            ?: return ResponseEntity.status(400).body("Authentication failed.")
        return ResponseEntity.ok().body(tokenResponse)
    }

    @PostMapping("/kakao/refreshToken")
    fun refreshKakaoToken(@RequestBody request: KakaoRefreshRequest): ResponseEntity<Any> {
        val tokenResponse = authService.refreshUserToken(request)
        return if (tokenResponse != null) {
            ResponseEntity.ok().body(tokenResponse)
        } else {
            ResponseEntity.status(400).body("Unable to refresh the Kakao token. Please check the provided refresh token.")
        }
    }
}
