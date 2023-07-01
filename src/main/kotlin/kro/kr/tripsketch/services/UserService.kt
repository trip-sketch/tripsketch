package kr.kro.tripsketch.services

import kr.kro.tripsketch.dto.UserLoginDto
import kr.kro.tripsketch.dto.UserRegistrationDto
import kr.kro.tripsketch.domain.User
import kr.kro.tripsketch.repositories.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {

    fun registerUser(userRegistrationDto: UserRegistrationDto): User {
        if (userRepository.findByEmail(userRegistrationDto.email) != null) {
            throw IllegalArgumentException("이미 존재하는 이메일입니다.")
        }
        val email = userRegistrationDto.email ?: throw IllegalArgumentException("이메일은 필수입니다.")
        val nickname = userRegistrationDto.nickname ?: throw IllegalArgumentException("닉네임은 필수입니다.")
        val user = User(
            email = email,
            nickname = nickname,
            profileImageUrl = userRegistrationDto.profileImageUrl,
            introduction = userRegistrationDto.introduction
        )
        return userRepository.save(user)
    }

    fun loginUser(userLoginDto: UserLoginDto): User? {
        return userRepository.findByEmail(userLoginDto.email)
    }
}
