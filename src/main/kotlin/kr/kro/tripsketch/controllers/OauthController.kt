package kr.kro.tripsketch.controllers

import io.swagger.v3.oas.annotations.responses.ApiResponse
import kr.kro.tripsketch.dto.KakaoLoginRequest
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
    @ApiResponse(responseCode = "200", description = "카카오 코드를 성공적으로 반환합니다.")
    fun kakaoCode(@RequestParam code: String): ResponseEntity<Void> {
        val encryptedCode = EncryptionUtils.encryptAES(code)
        return ResponseEntity.ok()
            .header("X-Encrypted-Code", encryptedCode)  // encryptedCode를 'X-Encrypted-Code'라는 헤더로 설정
            .build()
    }


    @PostMapping("/kakao/login")
    @ApiResponse(responseCode = "200", description = "카카오 로그인이 성공적으로 완료되었습니다.")
    @ApiResponse(responseCode = "400", description = "인증에 실패했습니다.")
    fun kakaoLogin(@RequestBody request: KakaoLoginRequest): ResponseEntity<Any> {
        val decryptedCode = EncryptionUtils.decryptAES(request.code) // 복호화 로직 추가
        val tokenResponse = authService.authenticateViaKakao(decryptedCode, request.pushToken)
            ?: return ResponseEntity.status(400).body("Authentication failed.")
        return ResponseEntity.ok().body(tokenResponse)
    }

    @PostMapping("/kakao/refreshToken")
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
