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
        userService.findUserByEmail(following) ?: throw IllegalArgumentException("사용자가 존재하지 않습니다.")
        if (!followRepository.existsByFollowerAndFollowing(follower, following)) {
            followRepository.save(Follow(follower = follower, following = following))
        }
    }

    fun unfollow(token: String, following: String) {
        val follower = jwtService.getEmailFromToken(token)
        userService.findUserByEmail(following) ?: throw IllegalArgumentException("사용자가 존재하지 않습니다.")
        followRepository.deleteByFollowerAndFollowing(follower, following)
    }

    fun unfollowMe(token: String, follower: String) {
        val following = jwtService.getEmailFromToken(token)
        userService.findUserByEmail(follower) ?: throw IllegalArgumentException("사용자가 존재하지 않습니다.")
        followRepository.deleteByFollowerAndFollowing(follower, following)
    }

    fun getFollowings(follower: String): List<UserProfileDto> {
        return followRepository.findByFollower(follower).map { follow ->
            val user = userRepository.findByNickname(follow.following)
            UserProfileDto(
                nickname = user?.nickname,
                introduction = user?.introduction,
                profileImageUrl = user?.profileImageUrl
            )
        }
    }

    fun getFollowers(following: String): List<UserProfileDto> {
        return followRepository.findByFollowing(following).map { follow ->
            val user = userRepository.findByEmail(follow.follower)
            UserProfileDto(
                nickname = user?.nickname,
                introduction = user?.introduction,
                profileImageUrl = user?.profileImageUrl
            )
        }
    }
}