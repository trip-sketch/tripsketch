package kr.kro.tripsketch.repositories

import kr.kro.tripsketch.domain.User
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface UserRepository : MongoRepository<User, String> {
    fun findByMemberId(memberId: Long): User?
    fun findByNickname(nickname: String): User?
    fun existsByNickname(nickname: String): Boolean
    fun findByOurRefreshToken(ourRefreshToken: String): User?
    fun findUsersByUpdatedAtBefore(cutoffDate: LocalDateTime): List<User>
}
