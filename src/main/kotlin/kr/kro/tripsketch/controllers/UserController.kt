package kr.kro.tripsketch.controllers

import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import kr.kro.tripsketch.annotations.TokenValidation
import kr.kro.tripsketch.dto.ProfileDto
import kr.kro.tripsketch.dto.UserDto
import kr.kro.tripsketch.exceptions.BadRequestException
import kr.kro.tripsketch.exceptions.UnauthorizedException
import kr.kro.tripsketch.services.UserService
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/user")
class UserController(
    private val userService: UserService
) {

    @TokenValidation
    @GetMapping
    @ApiResponse(responseCode = "200", description = "사용자 정보를 성공적으로 반환합니다.")
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    fun getUser(req: HttpServletRequest): ResponseEntity<UserDto> {
        val email = req.getAttribute("userEmail") as String
        val user = userService.findUserByEmail(email)
        return if (user != null) {
            ResponseEntity.ok(userService.toDto(user, true)) // 이메일 포함
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/nickname")
    @ApiResponse(responseCode = "200", description = "사용자의 닉네임으로 정보를 성공적으로 반환합니다.")
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    fun getUserByNickname(@RequestParam nickname: String): ResponseEntity<UserDto> {
        val user = userService.findUserByNickname(nickname)
        return if (user != null) {
            ResponseEntity.ok(userService.toDto(user, false)) // 이메일 미포함
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @TokenValidation
    @PatchMapping
    @ApiResponse(responseCode = "200", description = "사용자 정보 업데이트가 성공적으로 완료되었습니다.")
    @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    @ApiResponse(responseCode = "401", description = "이메일이 존재하지 않습니다.")
    fun updateUser(req: HttpServletRequest, @RequestBody profileDto: ProfileDto): ResponseEntity<UserDto> {
        val email = req.getAttribute("userEmail") as String?
            ?: throw UnauthorizedException("이메일이 존재하지 않습니다.")
        try {
            val updatedUser = userService.updateUserByEmail(email, profileDto)
            return ResponseEntity.ok(userService.toDto(updatedUser))
        } catch (e: IllegalArgumentException) {
            throw BadRequestException("요청이 잘못되었습니다: ${e.message}")
        }
    }

    @TokenValidation(adminOnly = true)
    @GetMapping("/users")
    @ApiResponse(responseCode = "200", description = "모든 사용자의 정보를 성공적으로 반환합니다.")
    fun getAllUsers(req: HttpServletRequest, pageable: Pageable): ResponseEntity<Any> {
        val users = userService.getAllUsers(pageable)
        return ResponseEntity.ok(users.map { userService.toDto(it) })
    }
}
