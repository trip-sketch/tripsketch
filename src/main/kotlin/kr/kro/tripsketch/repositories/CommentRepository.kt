package kr.kro.tripsketch.repositories

import kr.kro.tripsketch.domain.Comment
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CommentRepository : MongoRepository<Comment, String> {
    // 특정 게시물에 대한 모든 댓글 검색
    fun findAllByTripId(tripId: String): List<Comment>

    fun countCommentsByTripId(tripId: String): Int
}
