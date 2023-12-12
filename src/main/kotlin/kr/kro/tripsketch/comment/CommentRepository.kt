package kr.kro.tripsketch.comment

import kr.kro.tripsketch.comment.Comment
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

/**
 * 댓글에 대한 데이터베이스 액세스를 위한 Repository입니다.
 *
 * @author BYEONGUK KO
 */
@Repository
interface CommentRepository : MongoRepository<Comment, String> {

    /**
     * 특정 게시물에 대한 모든 댓글을 검색합니다.
     *
     * @param tripId 게시물 아이디
     * @return List<Comment> 댓글 목록
     */
    fun findAllByTripId(tripId: String): List<Comment>

    /**
     * 특정 게시물에 대한 댓글 수를 계산합니다.
     *
     * @param tripId 게시물 아이디
     * @return Int 댓글 수
     */
    fun countCommentsByTripId(tripId: String): Int
}
