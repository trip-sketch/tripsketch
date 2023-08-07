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

    fun getFollowings(followerEmail: String): List<UserProfileDto> {
        val follower = userRepository.findByEmail(followerEmail)
        val followerProfile = follower?.let {
            UserProfileDto(
                nickname = it.nickname,
                introduction = it.introduction,
                profileImageUrl = it.profileImageUrl
            )
        }

        val followings = followRepository.findByFollower(followerEmail).map { follow ->
            val user = userRepository.findByEmail(follow.following)
            UserProfileDto(
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
                nickname = it.nickname,
                introduction = it.introduction,
                profileImageUrl = it.profileImageUrl
            )
        }

        val followers = followRepository.findByFollowing(followingEmail).map { follow ->
            val user = userRepository.findByEmail(follow.follower)
            UserProfileDto(
                nickname = user?.nickname,
                introduction = user?.introduction,
                profileImageUrl = user?.profileImageUrl
            )
        }

        return listOfNotNull(followingProfile) + followers
    }
}