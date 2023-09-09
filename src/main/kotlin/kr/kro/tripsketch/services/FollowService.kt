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

    fun follow(followerMemberId: Long, followingNickname: String): String {
        val followingId = userService.getUserIdByMemberId(
            userService.findUserByNickname(followingNickname)?.memberId
                ?: throw IllegalArgumentException("사용자가 존재하지 않습니다.")
        )
        val followerId = userService.getUserIdByMemberId(followerMemberId)

        if (followerId == followingId) {
            throw IllegalArgumentException("자신을 구독할 수 없습니다.")
        }

        if (!followRepository.existsByFollowerAndFollowing(followerId, followingId)) {
            followRepository.save(Follow(follower = followerId, following = followingId))
            val follower = userService.findUserByMemberId(followerMemberId)
            val followerNickname = follower?.nickname ?: "알 수 없는 사용자"
            val followerProfileUrl = follower?.profileImageUrl
            return notificationService.sendPushNotification(
                listOf(followingId),
                "새로운 여행의 시작, 트립스케치",
                "$followerNickname 님이 당신을 구독했습니다. ",
                nickname = followerNickname,
                profileUrl = followerProfileUrl
            )
        } else {
            throw IllegalArgumentException("이미 구독 중입니다.")
        }
    }

    fun unfollow(followerMemberId: Long, followingNickname: String) {
        val followingId = userService.getUserIdByMemberId(
            userService.findUserByNickname(followingNickname)?.memberId
                ?: throw IllegalArgumentException("사용자가 존재하지 않습니다.")
        )
        val followerId = userService.getUserIdByMemberId(followerMemberId)

        if (followerId == followingId) {
            throw IllegalArgumentException("자신을 구독 취소할 수 없습니다.")
        }

        if (followRepository.existsByFollowerAndFollowing(followerId, followingId)) {
            followRepository.deleteByFollowerAndFollowing(followerId, followingId)
        } else {
            throw IllegalArgumentException("구독 하지 않은 사용자를 취소 할 수 없습니다.")
        }
    }

    fun unfollowMe(followingMemberId: Long, followerNickname: String) {
        val followerId = userService.getUserIdByMemberId(
            userService.findUserByNickname(followerNickname)?.memberId
                ?: throw IllegalArgumentException("사용자가 존재하지 않습니다.")
        )
        val followingId = userService.getUserIdByMemberId(followingMemberId)

        if (followerId == followingId) {
            throw IllegalArgumentException("자신을 구독 취소할 수 없습니다.")
        }

        if (followRepository.existsByFollowerAndFollowing(followerId, followingId)) {
            followRepository.deleteByFollowerAndFollowing(followerId, followingId)
        } else {
            throw IllegalArgumentException("구독 하지 않은 사용자를 취소할 수 없습니다.")
        }
    }

    fun getFollowings(followerNickname: String, currentUserMemberId: Long?): List<ProfileDto> {
        val followerId = userService.getUserIdByMemberId(
            userService.findUserByNickname(followerNickname)?.memberId
                ?: throw IllegalArgumentException("사용자가 존재하지 않습니다.")
        )

        return followRepository.findByFollower(followerId).map { follow ->
            val user = userRepository.findById(follow.following).orElse(null)
            val isCurrentUserFollowing: Boolean? = currentUserMemberId?.let { memberId ->
                val currentUserId = userService.getUserIdByMemberId(memberId)
                followRepository.existsByFollowerAndFollowing(currentUserId, user?.id ?: "")
            }

            ProfileDto(
                nickname = user?.nickname,
                introduction = user?.introduction,
                profileImageUrl = user?.profileImageUrl,
                isFollowing = isCurrentUserFollowing
            )
        }
    }

    fun getFollowers(followingNickname: String, currentUserMemberId: Long?): List<ProfileDto> {
        val followingId = userService.getUserIdByMemberId(
            userService.findUserByNickname(followingNickname)?.memberId
                ?: throw IllegalArgumentException("사용자가 존재하지 않습니다.")
        )

        return followRepository.findByFollowing(followingId).map { follow ->
            val user = userRepository.findById(follow.follower).orElse(null)
            val isCurrentUserFollowing: Boolean? = currentUserMemberId?.let { memberId ->
                val currentUserId = userService.getUserIdByMemberId(memberId)
                followRepository.existsByFollowerAndFollowing(currentUserId, user?.id ?: "")
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
