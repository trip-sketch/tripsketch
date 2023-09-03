package kr.kro.tripsketch.services

import kr.kro.tripsketch.repositories.FollowRepository
import kr.kro.tripsketch.domain.Follow
import kr.kro.tripsketch.dto.ProfileDto
import kr.kro.tripsketch.repositories.UserRepository
import org.springframework.stereotype.Service

@Service
class FollowService(
    private val followRepository: FollowRepository,
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService,
) {

    fun follow(followerEmail: String, followingNickname: String): String {
        val followingEmail = userService.findUserByNickname(followingNickname)?.email
            ?: throw IllegalArgumentException("사용자가 존재하지 않습니다.")
        if (followerEmail == followingEmail) {
            throw IllegalArgumentException("자신을 구독할 수 없습니다.")
        }
        if (!followRepository.existsByFollowerAndFollowing(followerEmail, followingEmail)) {
            followRepository.save(Follow(follower = followerEmail, following = followingEmail))
            val follower = userService.findUserByEmail(followerEmail)
            val followerNickname = follower?.nickname ?: "Unknown user"
            val followerProfileUrl = follower?.profileImageUrl
            return notificationService.sendPushNotification(
                listOf(followingEmail),
                "새로운 여행의 시작, 트립스케치",
                "$followerNickname 님이 당신을 구독했습니다. ",
                nickname = followerNickname,
                profileUrl = followerProfileUrl
            )
        } else {
            throw IllegalArgumentException("이미 구독 중입니다.")
        }
    }


    fun unfollow(followerEmail: String, followingNickname: String) {
        val followingEmail = userService.findUserByNickname(followingNickname)?.email
            ?: throw IllegalArgumentException("사용자가 존재하지 않습니다.")
        if (followerEmail == followingEmail) {
            throw IllegalArgumentException("자신을 구독 취소할 수 없습니다.")
        }
        if (followRepository.existsByFollowerAndFollowing(followerEmail, followingEmail)) {
            followRepository.deleteByFollowerAndFollowing(followerEmail, followingEmail)
        } else {
            throw IllegalArgumentException("구독 하지 않은 사용자를 취소 할 수 없습니다.")
        }
    }

    fun unfollowMe(followingEmail: String, followerNickname: String) {
        val followerEmail = userService.findUserByNickname(followerNickname)?.email
            ?: throw IllegalArgumentException("사용자가 존재하지 않습니다.")
        if (followerEmail == followingEmail) {
            throw IllegalArgumentException("자신을 구독 취소를 할 수 없습니다.")
        }
        if (followRepository.existsByFollowerAndFollowing(followerEmail, followingEmail)) {
            followRepository.deleteByFollowerAndFollowing(followerEmail, followingEmail)
        } else {
            throw IllegalArgumentException("구독 하지 않은 사용자를 취소할 수 없습니다.")
        }
    }

    fun getFollowings(followerNickname: String, currentUserEmail: String?): List<ProfileDto> {
        val followerEmail = userService.findUserByNickname(followerNickname)?.email
            ?: throw IllegalArgumentException("사용자가 존재하지 않습니다.")

        return followRepository.findByFollower(followerEmail).map { follow ->
            val user = userRepository.findByEmail(follow.following)
            val isCurrentUserFollowing: Boolean? = currentUserEmail?.let {
                followRepository.existsByFollowerAndFollowing(it, user?.email ?: "")
            }

            ProfileDto(
                nickname = user?.nickname,
                introduction = user?.introduction,
                profileImageUrl = user?.profileImageUrl,
                isFollowing = isCurrentUserFollowing
            )
        }
    }

    fun getFollowers(followingNickname: String, currentUserEmail: String?): List<ProfileDto> {
        val followingEmail = userService.findUserByNickname(followingNickname)?.email
            ?: throw IllegalArgumentException("사용자가 존재하지 않습니다.")

        return followRepository.findByFollowing(followingEmail).map { follow ->
            val user = userRepository.findByEmail(follow.follower)
            val isCurrentUserFollowing: Boolean? = currentUserEmail?.let {
                followRepository.existsByFollowerAndFollowing(it, user?.email ?: "")
            }

            ProfileDto(
                nickname = user?.nickname,
                introduction = user?.introduction,
                profileImageUrl = user?.profileImageUrl,
                isFollowing = isCurrentUserFollowing
            )
        }
    }

}
