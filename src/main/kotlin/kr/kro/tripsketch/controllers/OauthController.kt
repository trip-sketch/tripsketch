package kr.kro.tripsketch.controllers

import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletResponse
import kr.kro.tripsketch.dto.KakaoRefreshRequest
import kr.kro.tripsketch.dto.TokenResponse
import kr.kro.tripsketch.services.AuthService
import kr.kro.tripsketch.services.KakaoOAuthService
import kr.kro.tripsketch.utils.EncryptionUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/oauth/kakao")
class OauthController(
    private val authService: AuthService,
    private val kakaoOAuthService: KakaoOAuthService,
) {

    @GetMapping("/callback")
    fun kakaoCallback(@RequestParam code: String): ResponseEntity<Void> {
        val tokenResponse = authService.authenticateViaKakao(code)
            ?: return ResponseEntity.status(400).build() // Authentication failed

        val headers = HttpHeaders().apply {
            set("AccessToken", tokenResponse.accessToken)
            set("RefreshToken", tokenResponse.refreshToken)
            set("RefreshTokenExpiryDate", tokenResponse.refreshTokenExpiryDate.toString())
        }

        return ResponseEntity.ok().headers(headers).build()
    }

    @PostMapping("/refreshToken")
    @ApiResponse(responseCode = "200", description = "카카오 토큰 갱신이 성공적으로 완료되었습니다.")
    @ApiResponse(responseCode = "400", description = "카카오 토큰을 갱신할 수 없습니다. 제공된 REFRESH 토큰을 확인하세요.")
    fun refreshKakaoToken(@RequestBody request: KakaoRefreshRequest): ResponseEntity<Any> {
        val tokenResponse = authService.refreshUserToken(request)
        return if (tokenResponse != null) {
            ResponseEntity.ok().body(tokenResponse)
        } else {
            ResponseEntity.status(400)
                .body("Unable to refresh the Kakao token. Please check the provided refresh token.")
        }
    }

}
