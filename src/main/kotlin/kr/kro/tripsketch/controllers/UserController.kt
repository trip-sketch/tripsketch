package kr.kro.tripsketch.controllers

import kr.kro.tripsketch.domain.User
import kr.kro.tripsketch.dto.AdditionalUserInfo
import kr.kro.tripsketch.dto.UserRegistrationDto
import kr.kro.tripsketch.dto.UserUpdateDto
import kr.kro.tripsketch.services.JwtService
import kr.kro.tripsketch.services.KakaoOAuthService
import kr.kro.tripsketch.services.NickNameService
import kr.kro.tripsketch.services.UserService
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class UserController(
    private val userService: UserService,
    private val jwtService: JwtService
) {

    @GetMapping
    fun getUser(@PathVariable email: String): ResponseEntity<User> {
        val user = userService.findUserByEmail(email)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PatchMapping
    fun updateUser(@RequestHeader("Authorization") token: String, @RequestBody userUpdateDto: UserUpdateDto): ResponseEntity<Any> {
        val actualToken = token.removePrefix("Bearer ").trim() // "Bearer " 제거

        if (!jwtService.validateToken(actualToken)) { // 토큰 유효성 검증
            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.")
        }

        return try {
            val updatedUser = userService.updateUser(actualToken, userUpdateDto)
            ResponseEntity.ok(updatedUser)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(e.message)
        }
    }

    @GetMapping("/users")
    fun getAllUsers(): ResponseEntity<List<User>> {
        val users = userService.getAllUsers()
        return ResponseEntity.ok(users)
    }
}
