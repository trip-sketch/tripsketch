package kr.kro.tripsketch.controllers

import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletResponse
import kr.kro.tripsketch.dto.KakaoLoginRequest
import kr.kro.tripsketch.dto.KakaoRefreshRequest
import kr.kro.tripsketch.dto.TokenResponse
import kr.kro.tripsketch.services.AuthService
import kr.kro.tripsketch.services.KakaoOAuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/oauth/kakao")
class OauthController(
    private val authService: AuthService,
    private val kakaoOAuthService: KakaoOAuthService,
) {

    @GetMapping("/startLogin")
    fun startKakaoLogin(response: HttpServletResponse): ResponseEntity<Void> {
        val kakaoLoginUrl = kakaoOAuthService.getKakaoLoginUrl()
        response.sendRedirect(kakaoLoginUrl)
        return ResponseEntity.status(302).build()
    }


    @PostMapping("/callback")
    fun kakaoCallback(@RequestParam code: String): ResponseEntity<Any> {
        val tokenResponse = authService.authenticateViaKakao(code)
            ?: return ResponseEntity.status(400).body("Authentication failed.")

        val oneTimeCode = authService.generateOneTimeCodeForToken(tokenResponse)
        return ResponseEntity.ok().body(mapOf("oneTimeCode" to oneTimeCode))
    }


    @GetMapping("/retrieveToken")
    fun retrieveToken(@RequestParam oneTimeCode: String, @RequestParam pushToken: String): ResponseEntity<TokenResponse> {
        val tokenResponse = authService.retrieveTokenByOneTimeCode(oneTimeCode, pushToken)
            ?: return ResponseEntity.status(404).build() // 코드가 없을 경우 404 반환

        return ResponseEntity.ok(tokenResponse)
    }

    @GetMapping("/code")
    @ApiResponse(responseCode = "200", description = "카카오 코드를 성공적으로 반환합니다.")
    fun kakaoCode(@RequestParam code: String): ResponseEntity<Void> {
        val encryptedCode = EncryptionUtils.encryptAES(code)
        return ResponseEntity.ok()
            .header("X-Encrypted-Code", encryptedCode)  // encryptedCode 'X-Encrypted-Code'라는 헤더로 설정
            .build()
    }


    @PostMapping("/login")
    @ApiResponse(responseCode = "200", description = "카카오 로그인이 성공적으로 완료되었습니다.")
    @ApiResponse(responseCode = "400", description = "인증에 실패했습니다.")
    fun kakaoLogin(@RequestBody request: KakaoLoginRequest): ResponseEntity<Any> {
        val decryptedCode = EncryptionUtils.decryptAES(request.code) // 복호화 로직 추가
        val tokenResponse = authService.authenticateViaKakao(decryptedCode)
            ?: return ResponseEntity.status(400).body("Authentication failed.")
        return ResponseEntity.ok().body(tokenResponse)
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
