package kr.kro.tripsketch.auth

import io.swagger.v3.oas.annotations.responses.ApiResponse
import kr.kro.tripsketch.auth.dtos.KakaoRefreshRequest
import kr.kro.tripsketch.auth.services.AuthService
import kr.kro.tripsketch.config.KakaoOAuthConfig
import kr.kro.tripsketch.services.UserService
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.view.RedirectView

/**
 * Kakao OAuth와 관련된 요청을 처리하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("api/oauth/kakao")
class OauthController(
    private val authService: AuthService,
    private val resourceLoader: ResourceLoader,
    private val userService: UserService,
    private val kakaoOAuthConfig: KakaoOAuthConfig,
) {

    /**
     * Kakao OAuth 콜백을 처리하는 메서드입니다.
     */
    @GetMapping("/callback")
    fun kakaoCallback(@RequestParam code: String): ResponseEntity<String> {
        val tokenResponse = authService.authenticateViaKakao(code)
            ?: return ResponseEntity.status(400).build()

        val headers = HttpHeaders().apply {
            set("AccessToken", tokenResponse.accessToken)
            set("RefreshToken", tokenResponse.refreshToken)
            set("RefreshTokenExpiryDate", tokenResponse.refreshTokenExpiryDate.toString())
        }

        val resource = resourceLoader.getResource("classpath:/static/index.html")
        val responseBody = resource.inputStream.bufferedReader().readText()

        return ResponseEntity.ok().headers(headers).body(responseBody)
    }

    /**
     * Kakao 토큰을 갱신하는 메서드입니다.
     */
    @PostMapping("/refresh-token")
    @ApiResponse(responseCode = "200", description = "카카오 토큰 갱신이 성공적으로 완료되었습니다.")
    @ApiResponse(responseCode = "400", description = "카카오 토큰을 갱신할 수 없습니다. 제공된 REFRESH 토큰을 확인하세요.")
    fun refreshKakaoToken(@RequestBody request: KakaoRefreshRequest): ResponseEntity<Void> {
        // 프론트로부터 받는 토큰 출력
        println("Received RefreshToken from frontend: ${request.ourRefreshToken}")

        val tokenResponse = authService.refreshUserToken(request)

        // 갱신된 refreshToken을 콘솔에 출력
        println("RefreshToken after refreshing: ${tokenResponse?.refreshToken}")

        return if (tokenResponse != null) {
            val headers = HttpHeaders().apply {
                set("AccessToken", tokenResponse.accessToken)
                set("RefreshToken", tokenResponse.refreshToken)
                set("RefreshTokenExpiryDate", tokenResponse.refreshTokenExpiryDate.toString())
            }
            ResponseEntity.ok().headers(headers).build()
        } else {
            ResponseEntity.status(400).build()
        }
    }

    /**
     * Kakao 사용자를 연동 해제하는 메서드입니다.
     */
    @GetMapping("/unlink")
    fun unlinkKakaoUser(@RequestParam user_id: Long, @RequestParam referrer_type: String): ResponseEntity<String> {
        if (referrer_type != "UNLINK_FROM_APPS") {
            return ResponseEntity.badRequest().body("Invalid referrer_type.")
        }

        return try {
            userService.softDeleteUserByMemberId(user_id)
            ResponseEntity.ok("User with memberId $user_id soft deleted successfully.")
        } catch (ex: Exception) {
            ResponseEntity.badRequest().body(ex.message)
        }
    }

    /**
     * 클라이언트 요청을 Kakao OAuth URL로 리다이렉트하는 메서드입니다.
     */
    @GetMapping("/redirect")
    fun redirectToKakaoOauth(): RedirectView {
        val baseUrl = "https://kauth.kakao.com/oauth/authorize"
        val clientId = kakaoOAuthConfig.clientId
        val redirectUri = kakaoOAuthConfig.redirectUri
        val responseType = "code"

        val redirectUrl = "$baseUrl?client_id=$clientId&redirect_uri=$redirectUri&response_type=$responseType"
        return RedirectView(redirectUrl)
    }
}
