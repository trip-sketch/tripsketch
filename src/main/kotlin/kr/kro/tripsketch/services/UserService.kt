package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.User
import kr.kro.tripsketch.dto.ProfileDto
import kr.kro.tripsketch.repositories.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val nicknameService: NickNameService,
) {

    fun registerOrUpdateUser(email: String): User {
        var user = userRepository.findByEmail(email)
        if (user == null) {
            var nickname: String
            do {
                nickname = nicknameService.generateRandomNickname()
            } while (isNicknameExist(nickname))

            user = User(
                email = email,
                nickname = nickname,
                profileImageUrl = "https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png",
                introduction = "안녕하세요! 만나서 반갑습니다!"
            )
            user = userRepository.save(user)
        }
        return user
    }


    fun findUserByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    fun findUserByNickname(nickname: String): User? {
        return userRepository.findByNickname(nickname)
    }

    fun getAllUsers(pageable: Pageable): Page<User> {
        return userRepository.findAll(pageable)
    }

    // 사용자 업데이트
    fun updateUser(token: String, profileDto: ProfileDto): User {
        val email = jwtService.getEmailFromToken(token)
        val user = userRepository.findByEmail(email) ?: throw IllegalArgumentException("해당 이메일을 가진 사용자가 존재하지 않습니다.")

        if (profileDto.nickname != null && profileDto.nickname != user.nickname) {
            val existingUser = userRepository.findByNickname(profileDto.nickname)
            if (existingUser != null) {
                throw IllegalArgumentException("이미 사용중인 닉네임입니다.")
            }
            user.nickname = profileDto.nickname
        }
        if (profileDto.profileImageUrl != null) {
            user.profileImageUrl = profileDto.profileImageUrl
        }
        if (profileDto.introduction != null) {
            user.introduction = profileDto.introduction
        }

        return userRepository.save(user)
    }

    fun isNicknameExist(nickname: String): Boolean {
        return userRepository.existsByNickname(nickname)
    }

    fun updateUserRefreshToken(email: String, ourRefreshToken: String): User {
        val user = userRepository.findByEmail(email) ?: throw IllegalArgumentException("해당 이메일을 가진 사용자가 존재하지 않습니다.")
        user.ourRefreshToken = ourRefreshToken
        return userRepository.save(user)
    }

    fun findByOurRefreshToken(ourRefreshToken: String): User? {
        return userRepository.findByOurRefreshToken(ourRefreshToken)
    }

    fun updateKakaoRefreshToken(email: String, kakaoRefreshToken: String): User {
        val user = userRepository.findByEmail(email) ?: throw IllegalArgumentException("해당 이메일을 가진 사용자가 존재하지 않습니다.")
        user.kakaoRefreshToken = kakaoRefreshToken
        return userRepository.save(user)
    }

}
