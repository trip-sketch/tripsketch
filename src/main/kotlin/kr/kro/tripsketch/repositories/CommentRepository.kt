package kr.kro.tripsketch.repositories

import kr.kro.tripsketch.domain.Comment
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import java.time.LocalDateTime

interface CommentRepository : MongoRepository<Comment, String> {

    // 특정 tripId를 가진 Comment들을 조회하는 메서드
    fun findByTripId(tripId: String): List<Comment>

    // 특정 userId가 작성한 Comment들을 조회하는 메서드
    fun findByUserId(userId: String): List<Comment>

    // 특정 시간 이후에 생성된 Comment들을 조회하는 메서드
    fun findByCreatedAtAfter(date: LocalDateTime): List<Comment>

    // 특정 시간 이전에 생성된 Comment들을 조회하는 메서드
    fun findByCreatedAtBefore(date: LocalDateTime): List<Comment>

    // parentId가 null인 Comment들을 조회하는 메서드 (즉, 대댓글이 아닌 Comment들)
    fun findByParentIdIsNull(): List<Comment>

    // 특정 parentId를 가진 Comment들을 조회하는 메서드 (즉, 대댓글들)
    fun findByParentId(parentId: String): List<Comment>

    // 좋아요(likes) 수가 특정 값 이상인 Comment들을 조회하는 메서드
    fun findByLikesGreaterThanEqual(likes: Int): List<Comment>

    // 좋아요(likes) 수 순으로 Comment들을 정렬하여 조회하는 메서드
    // 여기에서는 Query 어노테이션을 사용하여 직접 쿼리를 작성함
    @Query("{}")
    fun findAllOrderByLikesDesc(): List<Comment>

    // 사용자 정의 쿼리를 사용하여, 특정 userId가 좋아요한 Comment들을 조회하는 메서드
    @Query("{ 'likedBy': ?0 }")
    fun findCommentsLikedByUser(userId: String): List<Comment>

}
