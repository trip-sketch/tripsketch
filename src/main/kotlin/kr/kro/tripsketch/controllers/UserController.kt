package kr.kro.tripsketch.controllers

import jakarta.servlet.http.HttpServletRequest
import kr.kro.tripsketch.annotations.TokenValidation
import kr.kro.tripsketch.dto.ProfileDto
import kr.kro.tripsketch.dto.UserDto
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
    fun getUser(req: HttpServletRequest): ResponseEntity<Any> {
        val email = req.getAttribute("userEmail") as String
        val user = userService.findUserByEmail(email)
        return if (user != null) {
            ResponseEntity.ok(userService.toDto(user, true)) // 이메일 포함
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/nickname")
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
    fun updateUser(req: HttpServletRequest, @RequestBody profileDto: ProfileDto): ResponseEntity<Any> {
        val email = req.getAttribute("userEmail") as String?
            ?: throw UnauthorizedException("이메일이 존재하지 않습니다.")
        return try {
            val updatedUser = userService.updateUserByEmail(email, profileDto)
            ResponseEntity.ok(userService.toDto(updatedUser))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
    }


    @TokenValidation(adminOnly = true)
    @GetMapping("/users")
    fun getAllUsers(req: HttpServletRequest, pageable: Pageable): ResponseEntity<Any> {
        val users = userService.getAllUsers(pageable)
        return ResponseEntity.ok(users.map { userService.toDto(it) })
    }
}
