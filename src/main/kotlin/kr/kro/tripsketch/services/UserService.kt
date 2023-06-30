package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.User
import kr.kro.tripsketch.dto.UserDto
import kr.kro.tripsketch.repositories.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {

    fun saveUser(userDTO: UserDto) {
    val user = User(
        email = userDTO.email, // Notice the lower case userDTO instead of UserDto
        nickname = userDTO.nickname,
        introduction = userDTO.introduction,
        profileImageUrl = userDTO.profileImageUrl
    )
    userRepository.save(user)
}
}
