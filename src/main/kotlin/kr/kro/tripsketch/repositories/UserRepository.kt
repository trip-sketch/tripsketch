package kr.kro.tripsketch.repositories

import kr.kro.tripsketch.domain.User
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository : MongoRepository<User, String> {

    fun findByEmail(email: String): User?
}
