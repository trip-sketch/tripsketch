package kr.kro.tripsketch.controllers

import kr.kro.tripsketch.domain.toDto
import kr.kro.tripsketch.dto.UserDto
import kr.kro.tripsketch.dto.UserUpdateDto
import kr.kro.tripsketch.services.JwtService
import kr.kro.tripsketch.services.UserService
import kr.kro.tripsketch.utils.TokenUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/user")
class UserController(
    private val userService: UserService,
    private val jwtService: JwtService
) {

    @GetMapping
    fun getUser(@RequestHeader("Authorization") token: String): ResponseEntity<Any> {
        val actualToken = TokenUtils.validateAndExtractToken(jwtService, token)
        val email = jwtService.getEmailFromToken(actualToken)
        val user = userService.findUserByEmail(email)
        return if (user != null) {
            ResponseEntity.ok(toDto(user))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/nickname")
    fun getUserByNickname(@RequestParam nickname: String): ResponseEntity<UserDto> {
        val user = userService.findUserByNickname(nickname)
        return if (user != null) {
            ResponseEntity.ok(toDto(user))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PatchMapping
    fun updateUser(@RequestHeader("Authorization") token: String, @RequestBody userUpdateDto: UserUpdateDto): ResponseEntity<Any> {
        val actualToken = token.removePrefix("Bearer ").trim()

        if (!jwtService.validateToken(actualToken)) {
            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.")
        }

        return try {
            val updatedUser = userService.updateUser(actualToken, userUpdateDto)
            ResponseEntity.ok(toDto(updatedUser))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(e.message)
        }
    }

    @GetMapping("/users")
    fun getAllUsers(pageable: Pageable): ResponseEntity<Page<UserDto>> {
        val users = userService.getAllUsers(pageable)
        return ResponseEntity.ok(users.map { toDto(it) })
    }
}
