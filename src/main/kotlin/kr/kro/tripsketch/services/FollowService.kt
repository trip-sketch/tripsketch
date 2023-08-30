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

    fun follow(followerEmail: String, followingNickname: String) {
        val followingEmail = userService.findUserByNickname(followingNickname)?.email
            ?: throw IllegalArgumentException("사용자가 존재하지 않습니다.")
        if (followerEmail == followingEmail) {
            throw IllegalArgumentException("자신을 구독할 수 없습니다.")
        }
        if (!followRepository.existsByFollowerAndFollowing(followerEmail, followingEmail)) {
            followRepository.save(Follow(follower = followerEmail, following = followingEmail))
            val followerNickname = userService.findUserByEmail(followerEmail)?.nickname ?: "Unknown user"
            notificationService.sendPushNotification(
                listOf(followingEmail), // 리스트 형태로 전달
                "트립스케치",
                "$followerNickname 님이 당신을 구독했습니다. "
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

    fun getFollowings(followerNickname: String): List<ProfileDto> {
        val followerEmail = userService.findUserByNickname(followerNickname)?.email
            ?: throw IllegalArgumentException("사용자가 존재하지 않습니다.")
        val followings = followRepository.findByFollower(followerEmail).map { follow ->
            val user = userRepository.findByEmail(follow.following)
            ProfileDto(
                nickname = user?.nickname,
                introduction = user?.introduction,
                profileImageUrl = user?.profileImageUrl,
            )
        }

        return followings
    }

    fun getFollowers(followingNickname: String): List<ProfileDto> {
        val followingEmail = userService.findUserByNickname(followingNickname)?.email
            ?: throw IllegalArgumentException("사용자가 존재하지 않습니다.")
        val followers = followRepository.findByFollowing(followingEmail).map { follow ->
            val user = userRepository.findByEmail(follow.follower)
            ProfileDto(
                nickname = user?.nickname,
                introduction = user?.introduction,
                profileImageUrl = user?.profileImageUrl,
            )
        }

        return followers
    }

}
