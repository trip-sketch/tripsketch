package kr.kro.tripsketch.services

import kr.kro.tripsketch.repositories.FollowRepository
import kr.kro.tripsketch.domain.Follow
import kr.kro.tripsketch.dto.UserProfileDto
import kr.kro.tripsketch.repositories.UserRepository
import org.springframework.stereotype.Service

@Service
class FollowService(
    private val followRepository: FollowRepository,
    private val userService: UserService,
    private val jwtService: JwtService,
    private val userRepository: UserRepository
) {

    fun follow(token: String, following: String) {
        val follower = jwtService.getEmailFromToken(token)
        if (follower == following) {
            throw IllegalArgumentException("자신을 구독할 수 없습니다.")
        }
        userService.findUserByEmail(following) ?: throw IllegalArgumentException("사용자가 존재하지 않습니다.")
        if (!followRepository.existsByFollowerAndFollowing(follower, following)) {
            followRepository.save(Follow(follower = follower, following = following))
        } else {
            throw IllegalArgumentException("이미 구독 중입니다.")
        }
    }

    fun unfollow(token: String, following: String) {
        val follower = jwtService.getEmailFromToken(token)
        if (follower == following) {
            throw IllegalArgumentException("자신을 구독 취소할 수 없습니다.")
        }
        userService.findUserByEmail(following) ?: throw IllegalArgumentException("사용자가 존재하지 않습니다.")
        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            followRepository.deleteByFollowerAndFollowing(follower, following)
        } else {
            throw IllegalArgumentException("구독 하지 않은 사용자를 취소 할 수 없습니다.")
        }
    }

    fun unfollowMe(token: String, follower: String) {
        val following = jwtService.getEmailFromToken(token)
        if (follower == following) {
            throw IllegalArgumentException("자신을 구독 취소를 할 수 없습니다.")
        }
        userService.findUserByEmail(follower) ?: throw IllegalArgumentException("사용자가 존재하지 않습니다.")
        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            followRepository.deleteByFollowerAndFollowing(follower, following)
        } else {
            throw IllegalArgumentException("구독 하지 않은 사용자를 취소할 수 없습니다.")
        }
    }

    fun getFollowings(followerEmail: String): List<UserProfileDto> {
        val follower = userRepository.findByEmail(followerEmail)
        val followerProfile = follower?.let {
            UserProfileDto(
                email = it.email,
                nickname = it.nickname,
                introduction = it.introduction,
                profileImageUrl = it.profileImageUrl
            )
        }

        val followings = followRepository.findByFollower(followerEmail).map { follow ->
            val user = userRepository.findByEmail(follow.following)
            UserProfileDto(
                email = user?.email,
                nickname = user?.nickname,
                introduction = user?.introduction,
                profileImageUrl = user?.profileImageUrl
            )
        }

        return listOfNotNull(followerProfile) + followings
    }


    fun getFollowers(followingEmail: String): List<UserProfileDto> {
        val following = userRepository.findByEmail(followingEmail)
        val followingProfile = following?.let {
            UserProfileDto(
                email = it.email,
                nickname = it.nickname,
                introduction = it.introduction,
                profileImageUrl = it.profileImageUrl
            )
        }

        val followers = followRepository.findByFollowing(followingEmail).map { follow ->
            val user = userRepository.findByEmail(follow.follower)
            UserProfileDto(
                email = user?.email,
                nickname = user?.nickname,
                introduction = user?.introduction,
                profileImageUrl = user?.profileImageUrl
            )
        }

        return listOfNotNull(followingProfile) + followers
    }
}