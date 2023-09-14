package kr.kro.tripsketch.controllers

import io.swagger.v3.oas.annotations.responses.ApiResponse
import kr.kro.tripsketch.dto.KakaoRefreshRequest
import kr.kro.tripsketch.services.AuthService
import kr.kro.tripsketch.services.KakaoOAuthService
import kr.kro.tripsketch.services.UserService
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/oauth/kakao")
class OauthController(
    private val authService: AuthService,
    private val resourceLoader: ResourceLoader,
    private val userService: UserService,
    private val kakaoOAuthService: KakaoOAuthService
) {

    @GetMapping("/callback")
    fun kakaoCallback(@RequestParam code: String): ResponseEntity<String> {
        val tokenResponse = authService.authenticateViaKakao(code)
            ?: return ResponseEntity.status(400).build()

        val headers = HttpHeaders().apply {
            set("AccessToken", tokenResponse.accessToken)
            set("RefreshToken", tokenResponse.refreshToken)
            set("RefreshTokenExpiryDate", tokenResponse.refreshTokenExpiryDate.toString())
        }

//        val resource = resourceLoader.getResource("classpath:/static/index.html")
//        val responseBody = resource.inputStream.bufferedReader().readText()

        return ResponseEntity.ok().headers(headers).build()
    }

    @PostMapping("/refreshToken")
    @ApiResponse(responseCode = "200", description = "카카오 토큰 갱신이 성공적으로 완료되었습니다.")
    @ApiResponse(responseCode = "400", description = "카카오 토큰을 갱신할 수 없습니다. 제공된 REFRESH 토큰을 확인하세요.")
    fun refreshKakaoToken(@RequestBody request: KakaoRefreshRequest): ResponseEntity<Void> {
        val tokenResponse = authService.refreshUserToken(request)
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

//    @GetMapping("/withdraw")
//    fun withdrawAndDeleteUser(req: HttpServletRequest): ResponseEntity<String> {
//
//        val accessToken = authService.getAccessTokenFromRequest(req)
//            ?: return ResponseEntity.badRequest().body("Failed to fetch accessToken from the request.")
//
//        val memberId = kakaoOAuthService.getMemberIdFromKakao(accessToken) ?: return ResponseEntity.badRequest().body("Failed to fetch memberId from Kakao.")
//
//        val user = userService.findUserByMemberId(memberId)
//        if (user == null) {
//            // If user is not registered in our system, then trigger Kakao login to register and fetch necessary details
//            val tokenResponse = authService.authenticateViaKakao(accessToken)
//                ?: return ResponseEntity.badRequest().body("Failed to authenticate user via Kakao.")
//        }
//
//        // 카카오 서비스 동의 철회
//        val isRevoked = kakaoOAuthService.revokeServiceAndWithdraw(accessToken)
//        if (!isRevoked) {
//            return ResponseEntity.badRequest().body("Failed to revoke service on Kakao side.")
//        }
//
//
//        return try {
//            userService.softDeleteUserByMemberId(memberId)
//
//            // 토큰 만료
//            authService.expireToken(accessToken)
//
//            ResponseEntity.ok("User with memberId $memberId has been successfully withdrawn and soft deleted.")
//        } catch (ex: Exception) {
//            ResponseEntity.badRequest().body(ex.message)
//        }
//    }


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
}
