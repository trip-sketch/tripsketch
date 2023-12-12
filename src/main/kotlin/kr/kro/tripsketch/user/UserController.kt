package kr.kro.tripsketch.user

import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import kr.kro.tripsketch.user.dtos.UserDto
import kr.kro.tripsketch.user.dtos.UserUpdateDto
import kr.kro.tripsketch.commons.exceptions.BadRequestException
import kr.kro.tripsketch.commons.exceptions.DataNotFoundException
import kr.kro.tripsketch.commons.exceptions.InternalServerException
import kr.kro.tripsketch.commons.exceptions.UnauthorizedException
import kr.kro.tripsketch.auth.services.KakaoOAuthService
import kr.kro.tripsketch.user.services.UserService
import kr.kro.tripsketch.commons.utils.EnvLoader
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

/**
 * 사용자와 관련된 요청을 처리하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("api/user")
class UserController(
    private val userService: UserService,
    private val kakaoOAuthService: KakaoOAuthService,
) {

    /**
     * 사용자 정보를 가져오는 메서드입니다.
     */
    @GetMapping
    @ApiResponse(responseCode = "200", description = "사용자 정보를 성공적으로 반환합니다.")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 또는 유효하지 않은 토큰.")
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    fun getUser(
        req: HttpServletRequest,
        @RequestParam token: String,
    ): ResponseEntity<Any> {
        val memberId = req.getAttribute("memberId") as Long
        val user = userService.findUserByMemberId(memberId)

        userService.storeUserPushToken(memberId, token)

        val adminIdsStrings = EnvLoader.getProperty("ADMIN_IDS")?.split(",") ?: listOf()
        val adminIds = adminIdsStrings.mapNotNull { it.toLongOrNull() }

        val isAdmin = memberId in adminIds

        return if (user != null) {
            ResponseEntity.ok(userService.toDto(user, includeAdmin = true, isAdmin = isAdmin, currentUserId = memberId))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * 회원이 닉네임으로 사용자 정보를 가져오는 메서드입니다.
     */
    @GetMapping("/nickname")
    fun getUserByNicknameForUser(req: HttpServletRequest, @RequestParam nickname: String): ResponseEntity<UserDto> {
        val currentUserMemberId = req.getAttribute("memberId") as Long?
        val user = userService.findUserByNickname(nickname)
        return if (user != null) {
            ResponseEntity.ok(userService.toDto(user, includeAdmin = false, currentUserId = currentUserMemberId))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * 비회원 사용자가 닉네임으로 사용자 정보를 가져오는 메서드입니다.
     */
    @GetMapping("/nickname/guest")
    @ApiResponse(responseCode = "200", description = "사용자의 닉네임으로 정보를 성공적으로 반환합니다.")
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    fun getUserByNicknameForGuest(@RequestParam nickname: String): ResponseEntity<UserDto> {
        val user = userService.findUserByNickname(nickname)
        return if (user != null) {
            ResponseEntity.ok(userService.toDto(user, includeAdmin = false))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * 사용자 정보를 업데이트하는 메서드입니다.
     */
    @PatchMapping(consumes = ["multipart/form-data"])
    @ApiResponse(responseCode = "200", description = "사용자 정보 업데이트가 성공적으로 완료되었습니다.")
    @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    @ApiResponse(responseCode = "401", description = "이메일이 존재하지 않습니다.")
    fun updateUser(
        req: HttpServletRequest,
        @Validated @ModelAttribute userUpdateDto: UserUpdateDto,
    ): ResponseEntity<UserDto> {
        val memberId = req.getAttribute("memberId") as Long? ?: throw UnauthorizedException("해당 사용자가 존재하지 않습니다.")

        try {
            val updatedUser = userService.updateUser(memberId, userUpdateDto)
            return ResponseEntity.ok(userService.toDto(updatedUser))
        } catch (e: IllegalArgumentException) {
            throw BadRequestException("요청이 잘못되었습니다: ${e.message}")
        }
    }

    /**
     * 모든 사용자의 정보를 가져오는 메서드입니다.
     */
    @GetMapping("/admin/users")
    @ApiResponse(responseCode = "200", description = "모든 사용자의 정보를 성공적으로 반환합니다.")
    fun getAllUsers(req: HttpServletRequest, pageable: Pageable): ResponseEntity<Page<UserDto>> {
        val users = userService.getAllUsers(pageable)
        return ResponseEntity.ok(users.map { userService.toDto(it) })
    }

    /**
     * 카카오 연동을 해제하고 사용자를 탈퇴하는 메서드입니다.
     */
    @DeleteMapping("/unlink")
    @ApiResponse(responseCode = "200", description = "카카오 연동 해제 및 회원 탈퇴 처리가 성공적으로 이루어졌습니다.")
    @ApiResponse(responseCode = "401", description = "해당 사용자가 존재하지 않습니다.")
    @ApiResponse(responseCode = "404", description = "회원 정보를 찾을 수 없습니다.")
    @ApiResponse(responseCode = "400", description = "카카오 Refresh Token이 없습니다.")
    @ApiResponse(responseCode = "500", description = "카카오 연동 해제에 실패했습니다.")
    fun unlinkKakaoUser(req: HttpServletRequest): ResponseEntity<Any> {
        val memberId = req.getAttribute("memberId") as Long?
            ?: throw UnauthorizedException("해당 사용자가 존재하지 않습니다.")

        val user = userService.findUserByMemberId(memberId)
            ?: throw DataNotFoundException("회원 정보를 찾을 수 없습니다.")

        val (newAccessToken, _) = kakaoOAuthService.refreshAccessToken(user.kakaoRefreshToken!!)
            ?: throw BadRequestException("카카오 Refresh Token이 없습니다.")

        if (newAccessToken == null || !kakaoOAuthService.unlinkUser(newAccessToken)) {
            throw InternalServerException("카카오 연동 해제에 실패했습니다")
        }

        userService.softDeleteUserByMemberId(memberId)
        return ResponseEntity.ok(mapOf("message" to "회원 탈퇴가 성공적으로 처리되었습니다."))
    }
}
