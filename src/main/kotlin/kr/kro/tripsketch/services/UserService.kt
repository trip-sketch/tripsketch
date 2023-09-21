package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.User
import kr.kro.tripsketch.dto.UserDto
import kr.kro.tripsketch.dto.UserUpdateDto
import kr.kro.tripsketch.exceptions.BadRequestException
import kr.kro.tripsketch.repositories.FollowRepository
import kr.kro.tripsketch.repositories.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val followRepository: FollowRepository,
    private val nicknameService: NickNameService,
    private val imageService: ImageService,
) {

    companion object {
        const val DEFAULT_IMAGE_URL = "https://ax6izwmsuv9c.objectstorage.ap-osaka-1.oci.customer-oci.com/n/ax6izwmsuv9c/b/tripsketch/o/profileLogo.png"
    }


    /**
     * 사용자를 등록하거나 업데이트하는 메소드.
     * 사용자가 존재하지 않으면 새로운 사용자를 생성하고 저장.
     */
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
                profileImageUrl = DEFAULT_IMAGE_URL,
                introduction = "안녕하세요! 만나서 반갑습니다!",
            )
            user = userRepository.save(user)
        }
        return user
    }

    /** 사용자의 ID를 memberId로 조회하는 메소드. */
    fun getUserIdByMemberId(memberId: Long): String {
        val user = findUserByMemberId(memberId)
        return user?.id ?: throw IllegalArgumentException("사용자가 존재하지 않습니다.")
    }

    /** memberId로 사용자를 조회하는 메소드. */
    fun findUserByMemberId(memberId: Long): User? {
        return userRepository.findByMemberId(memberId)
    }

    /** 닉네임으로 사용자를 조회하는 메소드. */
    fun findUserByNickname(nickname: String): User? {
        return userRepository.findByNickname(nickname)
    }

    /** ID로 사용자를 조회하는 메소드. */
    fun findUserById(id: String): User? {
        return userRepository.findById(id).orElse(null)
    }

    /** 모든 사용자를 페이지별로 조회하는 메소드. */
    fun getAllUsers(pageable: Pageable): Page<User> {
        return userRepository.findAll(pageable)
    }

    /**
     * 사용자 정보를 업데이트 합니다.
     *
     * 1. memberId를 기반으로 사용자를 찾습니다.
     * 2. 새로운 닉네임이 제공되고, 기존 닉네임과 다르며, 이미 사용 중인 닉네임이 아닌 경우에 닉네임을 업데이트 합니다.
     * 3. 새로운 프로필 이미지 URL이 제공되면:
     *    a. 기존 프로필 이미지가 기본 이미지가 아니면, 기존 이미지를 삭제합니다.
     *    b. 새로운 이미지를 업로드하고, 그 URL을 사용자의 프로필 이미지 URL로 설정합니다.
     * 4. 새로운 소개 메시지가 제공되면, 해당 메시지로 업데이트 합니다.
     * 5. 변경된 사용자 정보를 저장합니다.
     *
     * @param memberId 업데이트할 사용자의 memberId
     * @param userUpdateDto 업데이트할 사용자 정보를 포함하는 DTO
     * @return 업데이트된 사용자 객체
     * @throws BadRequestException 사용자를 찾을 수 없거나, 이미 사용 중인 닉네임을 사용하려 할 때 발생
     * @author Hojun Song
     */
    fun updateUser(memberId: Long, userUpdateDto: UserUpdateDto): User {
        val user = userRepository.findByMemberId(memberId) ?: throw BadRequestException("해당 이메일을 가진 사용자가 존재하지 않습니다.")

        userUpdateDto.nickname?.let {
            if (it != user.nickname && isNicknameExist(it)) {
                throw BadRequestException("이미 사용중인 닉네임입니다.")
            }
            user.nickname = it
        }

        userUpdateDto.profileImageUrl?.let { newImageFile ->
            val defaultImageUrl = DEFAULT_IMAGE_URL

            if (user.profileImageUrl != defaultImageUrl) {
                user.profileImageUrl?.let { oldImageUrl ->
                    try {
                        imageService.deleteImage(oldImageUrl)
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

    /** 사용자 정보를 저장 또는 업데이트하는 메소드. */
    fun saveOrUpdate(user: User): User {
        return userRepository.save(user)
    }

    /** 닉네임의 중복 여부를 확인하는 메소드. */
    fun isNicknameExist(nickname: String): Boolean {
        return userRepository.existsByNickname(nickname)
    }

    /** 사용자의 RefreshToken을 업데이트하는 메소드. */
    fun updateUserRefreshToken(memberId: Long, ourRefreshToken: String): User {
        val user = userRepository.findByMemberId(memberId) ?: throw IllegalArgumentException("해당 이메일을 가진 사용자가 존재하지 않습니다.")
        user.ourRefreshToken = ourRefreshToken
        return userRepository.save(user)
    }

    /** 우리 서비스의 RefreshToken으로 사용자를 조회하는 메소드. */
    fun findByOurRefreshToken(ourRefreshToken: String): User? {
        return userRepository.findByOurRefreshToken(ourRefreshToken)
    }

    /** KakaoRefreshToken을 업데이트하는 메소드. */
    fun updateKakaoRefreshToken(memberId: Long, kakaoRefreshToken: String): User {
        val user = userRepository.findByMemberId(memberId) ?: throw IllegalArgumentException("해당 이메일을 가진 사용자가 존재하지 않습니다.")
        user.kakaoRefreshToken = kakaoRefreshToken
        return userRepository.save(user)
    }

    /** 사용자의 expo push 토큰을 저장하는 메소드. */
    fun storeUserPushToken(memberId: Long, pushToken: String) {
        val user = userRepository.findByMemberId(memberId)
            ?: throw IllegalArgumentException("해당 유저의 멤버 아이디를 찾을 수 없습니다.")
        user.expoPushToken = pushToken
        userRepository.save(user)
    }

    /** 활동하지 않는 사용자들을 삭제하는 스케쥴 함수. */
    @Scheduled(cron = "0 30 15 * * ?")
    fun softDeleteInactiveUsers() {
        val cutoffDateForDeletion = LocalDateTime.now().minusMonths(24)
        val usersToDelete = userRepository.findUsersByUpdatedAtBefore(cutoffDateForDeletion)

        usersToDelete.forEach { user ->
            softDeleteUser(user)
        }
    }

    /** 사용자 정보를 소프트 딜리트하는 메소드. */
    fun softDeleteUser(user: User) {
        user.profileImageUrl = DEFAULT_IMAGE_URL
        user.kakaoRefreshToken = "DELETED"
        user.ourRefreshToken = "DELETED"
        user.expoPushToken = "DELETED"
        user.introduction = ""

        /** 랜덤닉네임 생성 */
        var newNickname: String
        do {
            newNickname = nicknameService.generateRandomNickname()
        } while (userRepository.existsByNickname(newNickname))
        user.nickname = newNickname

        /** 음수로 memberId 난수 생성 및 중복 검증 */
        var newMemberId: Long
        do {
            newMemberId = -1L * kotlin.random.Random.nextLong(Long.MAX_VALUE)
        } while (userRepository.existsByMemberId(newMemberId))
        user.memberId = newMemberId

        userRepository.save(user)
    }

    /** memberId를 이용하여 사용자 정보를 소프트 딜리트하는 메소드. */
    fun softDeleteUserByMemberId(memberId: Long) {
        val user = userRepository.findByMemberId(memberId)
            ?: throw IllegalArgumentException("해당 멤버 아이디를 가진 사용자가 존재하지 않습니다.")

        softDeleteUser(user)
    }

    /**
     * 현재 사용자가 특정 닉네임을 팔로우하는지 확인한다.
     *
     * @param currentUserMemberId 현재 사용자의 멤버 ID.
     * @param nickname 팔로우 여부를 확인하고자 하는 사용자의 닉네임.
     * @return 현재 사용자가 해당 닉네임의 사용자를 팔로우하는지 여부.
     */
    fun isFollowing(currentUserMemberId: Long?, nickname: String): Boolean {
        if (currentUserMemberId == null) return false

        val followingId = getUserIdByMemberId(
            findUserByNickname(nickname)?.memberId
                ?: return false,
        )
        val currentUserId = getUserIdByMemberId(currentUserMemberId)

        return followRepository.existsByFollowerAndFollowing(currentUserId, followingId)
    }

    /** 사용자의 팔로우 정보 (팔로워 수, 팔로잉 수)를 조회하는 메소드. */
    fun getUserFollowInfo(id: String): Pair<Long, Long> {
        val followingCount = followRepository.countByFollower(id)
        val followersCount = followRepository.countByFollowing(id)

        return Pair(followersCount, followingCount)
    }

    /** 사용자 정보를 DTO로 변환하는 메소드. */
    fun toDto(user: User, includeAdmin: Boolean = true, isAdmin: Boolean? = null, currentUserId: Long? = null): UserDto {
        val (followersCount, followingCount) = getUserFollowInfo(user.id.toString())
        val isCurrentUserFollowing = if (currentUserId != null) isFollowing(currentUserId, user.nickname) else null

        return if (includeAdmin) {
            UserDto(
                nickname = user.nickname,
                introduction = user.introduction,
                profileImageUrl = user.profileImageUrl,
                followersCount = followersCount,
                followingCount = followingCount,
                isAdmin = isAdmin,
                isFollowing = null,
            )
        } else {
            UserDto(
                nickname = user.nickname,
                introduction = user.introduction,
                profileImageUrl = user.profileImageUrl,
                followersCount = followersCount,
                followingCount = followingCount,
                isFollowing = isCurrentUserFollowing,
                isAdmin = null,
            )
        }
    }

}
