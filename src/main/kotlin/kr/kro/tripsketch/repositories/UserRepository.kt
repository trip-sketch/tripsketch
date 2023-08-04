package kr.kro.tripsketch.repositories

import kr.kro.tripsketch.domain.User
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : MongoRepository<User, String> {
    fun findByEmail(email: String): User?

    fun findByNickName(nickname: String): User?
}
