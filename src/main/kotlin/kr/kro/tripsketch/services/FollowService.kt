package kr.kro.tripsketch.services

import kr.kro.tripsketch.repositories.FollowRepository
import kr.kro.tripsketch.domain.Follow
import org.springframework.stereotype.Service

@Service
class FollowService(
    private val followRepository: FollowRepository,
    private val userService: UserService,
    private val jwtService: JwtService
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

    fun getFollowings(follower: String): List<Follow> {
        return followRepository.findByFollower(follower)
    }

    fun getFollowers(following: String): List<Follow> {
        return followRepository.findByFollowing(following)
    }
}