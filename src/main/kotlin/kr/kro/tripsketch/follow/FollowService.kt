@file:Suppress("KDocUnresolvedReference", "KDocUnresolvedReference")

package kr.kro.tripsketch.follow

import kr.kro.tripsketch.follow.model.Follow
import kr.kro.tripsketch.notification.NotificationService
import kr.kro.tripsketch.user.UserRepository
import kr.kro.tripsketch.user.dtos.ProfileDto
import kr.kro.tripsketch.user.services.UserService
import org.springframework.stereotype.Service
@Service
class FollowService(
    private val followRepository: FollowRepository,
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService,
) {

    /**
     * 사용자를 팔로우하고, 해당 팔로우에 관한 알림을 전송한다.
     *
     * @param followerMemberId 팔로우 하는 사용자의 ID.
     * @param followingNickname 팔로우 당하는 사용자의 닉네임.
     * @return 알림 전송 결과.
     * @author Hojun Song
     */
    fun follow(followerMemberId: Long, followingNickname: String): String {
        val followingId = userService.getUserIdByMemberId(
            userService.findUserByNickname(followingNickname)?.memberId
                ?: throw IllegalArgumentException("사용자가 존재하지 않습니다."),
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
                senderId = followerId,
                nickname = followerNickname,
                profileUrl = followerProfileUrl,
            )
        } else {
            throw IllegalArgumentException("이미 구독 중입니다.")
        }
    }

    /**
     * 사용자의 팔로우를 취소한다.
     *
     * @param followerMemberId 팔로우를 취소하는 사용자의 ID.
     * @param followingNickname 팔로우 취소 대상인 사용자의 닉네임.
     */

    fun unfollow(followerMemberId: Long, followingNickname: String) {
        val followingId = userService.getUserIdByMemberId(
            userService.findUserByNickname(followingNickname)?.memberId
                ?: throw IllegalArgumentException("사용자가 존재하지 않습니다."),
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

    /**
     * 사용자의 팔로우를 취소한다.
     *
     * @param followerMemberId 팔로우를 취소하는 사용자의 ID.
     * @param followingNickname 팔로우 취소 대상인 사용자의 닉네임.
     */
    fun unfollowMe(followingMemberId: Long, followerNickname: String) {
        val followerId = userService.getUserIdByMemberId(
            userService.findUserByNickname(followerNickname)?.memberId
                ?: throw IllegalArgumentException("사용자가 존재하지 않습니다."),
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

    /**
     * 주어진 사용자가 팔로우하는 사용자 목록을 반환한다.
     *
     * @param followerNickname 팔로우하는 사용자의 닉네임.
     * @param currentUserMemberId 현재 로그인한 사용자의 ID. 팔로우 여부를 확인하기 위해 사용됨.
     * @return 프로필 정보를 담은 DTO 목록.
     */
    fun getFollowings(followerNickname: String, currentUserMemberId: Long?): List<ProfileDto> {
        val followerId = userService.getUserIdByMemberId(
            userService.findUserByNickname(followerNickname)?.memberId
                ?: throw IllegalArgumentException("사용자가 존재하지 않습니다."),
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
                isFollowing = isCurrentUserFollowing,
            )
        }
    }

    /**
     * 주어진 사용자를 팔로우하는 사용자 목록을 반환한다.
     *
     * @param followingNickname 팔로우 당하는 사용자의 닉네임.
     * @param currentUserMemberId 현재 로그인한 사용자의 ID. 팔로우 여부를 확인하기 위해 사용됨.
     * @return 프로필 정보를 담은 DTO 목록.
     */
    fun getFollowers(followingNickname: String, currentUserMemberId: Long?): List<ProfileDto> {
        val followingId = userService.getUserIdByMemberId(
            userService.findUserByNickname(followingNickname)?.memberId
                ?: throw IllegalArgumentException("사용자가 존재하지 않습니다."),
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
                isFollowing = isCurrentUserFollowing,
            )
        }
    }
}
