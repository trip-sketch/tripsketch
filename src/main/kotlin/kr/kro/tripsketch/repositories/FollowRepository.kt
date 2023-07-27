package kr.kro.tripsketch.repositories

import kr.kro.tripsketch.domain.Follow
import org.springframework.data.mongodb.repository.MongoRepository

interface FollowRepository : MongoRepository<Follow, String> {
    fun findByFollower(follower: String): List<Follow>
    fun findByFollowing(following: String): List<Follow>
    fun existsByFollowerAndFollowing(follower: String, following: String): Boolean
    fun deleteByFollowerAndFollowing(follower: String, following: String)
}