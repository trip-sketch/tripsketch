package kr.kro.tripsketch.repositories

import kr.kro.tripsketch.domain.Comment
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface CommentRepository : MongoRepository<Comment, String> {
    fun findAllByTripId(tripId: String): List<Comment>
}