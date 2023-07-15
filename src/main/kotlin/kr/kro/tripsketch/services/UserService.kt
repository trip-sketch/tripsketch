package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.User
import kr.kro.tripsketch.dto.UserLoginDto
import kr.kro.tripsketch.dto.UserRegistrationDto
import kr.kro.tripsketch.dto.UserUpdateDto
import kr.kro.tripsketch.repositories.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
) {

    fun registerUser(userRegistrationDto: UserRegistrationDto): User {
        if (userRepository.findByEmail(userRegistrationDto.email) != null) {
            throw IllegalArgumentException("이미 존재하는 이메일입니다.")
        }
        val email = userRegistrationDto.email
        val nickname = userRegistrationDto.nickname
        val user = User(
            email = email,
            nickname = nickname,
            profileImageUrl = userRegistrationDto.profileImageUrl,
            introduction = userRegistrationDto.introduction,
        )
        return userRepository.save(user)
    }

    fun findUserByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    fun loginUser(userLoginDto: UserLoginDto): User? {
        return userRepository.findByEmail(userLoginDto.email)
    }

    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }

    // 사용자 업데이트
    fun updateUser(token: String, userUpdateDto: UserUpdateDto): User {
        val email = jwtService.getEmailFromToken(token)
        val user = userRepository.findByEmail(email) ?: throw IllegalArgumentException("해당 이메일을 가진 사용자가 존재하지 않습니다.")
        if (userUpdateDto.nickname != null) {
            user.nickname = userUpdateDto.nickname
        }
        if (userUpdateDto.profileImageUrl != null) {
            user.profileImageUrl = userUpdateDto.profileImageUrl
        }
        if (userUpdateDto.introduction != null) {
            user.introduction = userUpdateDto.introduction
        }
        return userRepository.save(user)
    }
}
