package kr.kro.tripsketch.services

import kr.kro.tripsketch.repositories.FollowRepository
import kr.kro.tripsketch.domain.Follow
import org.springframework.stereotype.Service

@Service
class FollowService(private val followRepository: FollowRepository) {

    fun follow(follower: String, following: String) {
        if (!followRepository.existsByFollowerAndFollowing(follower, following)) {
            followRepository.save(Follow(follower = follower, following = following))
        }
    }

    fun unfollow(follower: String, following: String) {
        followRepository.deleteByFollowerAndFollowing(follower, following)
    }

    fun getFollowings(follower: String): List<Follow> {
        return followRepository.findByFollower(follower)
    }

    fun getFollowers(following: String): List<Follow> {
        return followRepository.findByFollowing(following)
    }
}