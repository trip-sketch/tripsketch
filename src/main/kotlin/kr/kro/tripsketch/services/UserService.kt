package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.User
import kr.kro.tripsketch.dto.ProfileDto
import kr.kro.tripsketch.dto.UserDto
import kr.kro.tripsketch.dto.UserUpdateDto
import kr.kro.tripsketch.exceptions.BadRequestException
import kr.kro.tripsketch.repositories.FollowRepository
import kr.kro.tripsketch.repositories.UserRepository
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val followRepository: FollowRepository,
    private val nicknameService: NickNameService,
    private val imageService: ImageService,
) {

    fun registerOrUpdateUser(memberId: Long): User {
        var user = userRepository.findByMemberId(memberId)
        if (user == null) {
            var nickname: String
            do {
                nickname = nicknameService.generateRandomNickname()
            } while (isNicknameExist(nickname))

            user = User(
                memberId = memberId,
                nickname = nickname,
                profileImageUrl = "https://objectstorage.ap-osaka-1.oraclecloud.com/p/_EncCFAsYOUIwlJqRN7blRAETL9_l-fpCH-D07N4qig261ob7VHU8VIgtZaP-Thz/n/ax6izwmsuv9c/b/image-tripsketch/o/default-02.png",
                introduction = "안녕하세요! 만나서 반갑습니다!"
            )
            user = userRepository.save(user)
        }
        return user
    }

    fun getUserIdByMemberId(memberId: Long): String {
        val user = findUserByMemberId(memberId)
        return user?.id ?: throw IllegalArgumentException("사용자가 존재하지 않습니다.")
    }

    fun findUserByMemberId(memberId: Long): User? {
        return userRepository.findByMemberId(memberId)
    }


    fun findUserByNickname(nickname: String): User? {
        return userRepository.findByNickname(nickname)
    }

    fun findUserById(id: String): User? {
        return userRepository.findById(id).orElse(null)
    }

    fun getAllUsers(pageable: Pageable): Page<User> {
        return userRepository.findAll(pageable)
    }


    fun updateUser(memberId: Long, userUpdateDto: UserUpdateDto): User {
        val user = userRepository.findByMemberId(memberId) ?: throw BadRequestException("해당 이메일을 가진 사용자가 존재하지 않습니다.")

        userUpdateDto.nickname?.let {
            if (it != user.nickname && isNicknameExist(it)) {
                throw BadRequestException("이미 사용중인 닉네임입니다.")
            }
            user.nickname = it
        }

        userUpdateDto.profileImageUrl?.let { newImageFile ->
            val defaultImageUrl = "https://objectstorage.ap-osaka-1.oraclecloud.com/p/_EncCFAsYOUIwlJqRN7blRAETL9_l-fpCH-D07N4qig261ob7VHU8VIgtZaP-Thz/n/ax6izwmsuv9c/b/image-tripsketch/o/default-02.png"

            if (user.profileImageUrl != defaultImageUrl) {
                user.profileImageUrl?.let { oldImageUrl ->
                    try {
                        imageService.deleteImage(oldImageUrl)  // `ImageService`의 `deleteImage` 함수를 사용하여 URL을 삭제합니다.
                    } catch (e: Exception) {
                        // 오류 로깅
                        println("이미지 삭제에 실패했습니다. URL: $oldImageUrl, 오류: ${e.message}")
                    }
                }
            }

            val newImageUrl = imageService.uploadImage("tripsketch/trip-user", newImageFile)
            user.profileImageUrl = newImageUrl
        }

        userUpdateDto.introduction?.let {
            user.introduction = it
        }

        return userRepository.save(user)
    }


    fun saveOrUpdate(user: User): User {
        return userRepository.save(user)
    }

    fun isNicknameExist(nickname: String): Boolean {
        return userRepository.existsByNickname(nickname)
    }

    fun updateUserRefreshToken(memberId: Long, ourRefreshToken: String): User {
        val user = userRepository.findByMemberId(memberId) ?: throw IllegalArgumentException("해당 이메일을 가진 사용자가 존재하지 않습니다.")
        user.ourRefreshToken = ourRefreshToken
        return userRepository.save(user)
    }

    fun findByOurRefreshToken(ourRefreshToken: String): User? {
        return userRepository.findByOurRefreshToken(ourRefreshToken)
    }

    fun updateKakaoRefreshToken(memberId: Long, kakaoRefreshToken: String): User {
        val user = userRepository.findByMemberId(memberId) ?: throw IllegalArgumentException("해당 이메일을 가진 사용자가 존재하지 않습니다.")
        user.kakaoRefreshToken = kakaoRefreshToken
        return userRepository.save(user)
    }

    fun storeUserPushToken(memberId: Long, pushToken: String) {
        val user = userRepository.findByMemberId(memberId)
            ?: throw IllegalArgumentException("해당 유저의 멤버 아이디를 찾을 수 없습니다.")
        user.expoPushToken = pushToken
        userRepository.save(user)
    }

    fun getUserFollowInfo(id: String): Pair<Long, Long> {
        val followingCount = followRepository.countByFollower(id)
        val followersCount = followRepository.countByFollowing(id)

        return Pair(followersCount, followingCount)
    }

    @Scheduled(cron = "0 30 15 * * ?")
    fun softDeleteInactiveUsers() {
        val cutoffDateForDeletion = LocalDateTime.now().minusMonths(12)
        val usersToDelete = userRepository.findUsersByUpdatedAtBefore(cutoffDateForDeletion)

        val defaultImageUrl = "https://objectstorage.ap-osaka-1.oraclecloud.com/p/_EncCFAsYOUIwlJqRN7blRAETL9_l-fpCH-D07N4qig261ob7VHU8VIgtZaP-Thz/n/ax6izwmsuv9c/b/image-tripsketch/o/default-02.png"

        usersToDelete.forEach { user ->
            softDeleteUser(user, defaultImageUrl)
        }
    }

    fun softDeleteUser(user: User, defaultImageUrl: String) {
        user.profileImageUrl = defaultImageUrl
        user.kakaoRefreshToken = "DELETED"
        user.ourRefreshToken = "DELETED"
        user.expoPushToken = "DELETED"

        userRepository.save(user)

        println("User with ID ${user.id} has been soft deleted.")  // 출력 코드 추가
    }


    fun toDto(user: User, includeMemberID: Boolean = true, isAdmin: Boolean? = null): UserDto {
        val (followersCount, followingCount) = getUserFollowInfo(user.id.toString())

        return if (includeMemberID) {
            UserDto(
                nickname = user.nickname,
                introduction = user.introduction,
                profileImageUrl = user.profileImageUrl,
                followersCount = followersCount,
                followingCount = followingCount,
                isAdmin = isAdmin
            )
        } else {
            UserDto(
                nickname = user.nickname,
                introduction = user.introduction,
                profileImageUrl = user.profileImageUrl,
                followersCount = followersCount,
                followingCount = followingCount,
                isAdmin = null // 관리자 여부를 노출하지 않음
            )
        }
    }

}


