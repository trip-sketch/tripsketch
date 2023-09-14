package kr.kro.tripsketch.repositories

import kr.kro.tripsketch.domain.Comment
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CommentRepository : MongoRepository<Comment, String> {
    // 특정 유저가 작성한 모든 댓글 검색
    fun findAllByUserId(userId: String): List<Comment>

    // 특정 게시물에 대한 모든 댓글 검색
    fun findAllByTripId(tripId: String): List<Comment>

    // 삭제되지 않은 댓글만 검색
    fun findAllByIsDeletedFalse(): List<Comment> // 변경된 부분

    // 특정 사용자가 좋아요를 토글한 댓글 검색
    fun findAllByLikedByContains(userId: String): List<Comment>

    // 특정 부모 댓글에 속한 모든 자식 댓글 검색
    fun findAllByParentId(parentId: String): List<Comment>

    @Query("{'tripId': ?0}")
    fun countCommentsByTripId(tripId: String): Int? = 0
}
