package kr.kro.tripsketch.comment

import jakarta.servlet.http.HttpServletRequest
import kr.kro.tripsketch.comment.dtos.CommentChildrenCreateDto
import kr.kro.tripsketch.comment.dtos.CommentCreateDto
import kr.kro.tripsketch.comment.dtos.CommentDto
import kr.kro.tripsketch.comment.dtos.CommentUpdateDto
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

/**
 * 댓글(Comment) 기능을 처리하는 컨트롤러
 *
 * @author BYEONGUK KO
 */
@RestController
@RequestMapping("api/comment")
class CommentController(private val commentService: CommentService) {

    /**
     * 페이지네이션된 모든 댓글 목록을 가져옵니다.
     *
     * @param page 페이지 번호 (기본값: 1)
     * @param pageSize 페이지당 댓글 수 (기본값: 10)
     * @return 페이지네이션된 댓글 목록 및 페이징 정보
     */
    @GetMapping("/admin/comments-pagination")
    fun getAllCommentsWIthPagination(
        @RequestParam("page", required = false, defaultValue = "1") page: Int,
        @RequestParam("pageSize", required = false, defaultValue = "10") pageSize: Int,
    ): ResponseEntity<Map<String, Any>> {
        val paginatedComments = commentService.getAllCommentsWithPagination(page, pageSize)
        return ResponseEntity.ok(paginatedComments)
    }

    /**
     * 비회원으로 특정 여행 게시물에 대한 댓글 목록을 가져옵니다.
     *
     * @param tripId 특정 여행의 ID
     * @return List<CommentDto> 댓글 목록
     */
    @GetMapping("/guest/{tripId}")
    fun getCommentsGuestByTripId(@PathVariable tripId: String): List<CommentDto> {
        return commentService.getCommentsGuestByTripId(tripId)
    }

    /**
     * 특정 회원이 특정 여행에 대해 좋아요를 누른 댓글 목록을 가져옵니다.
     *
     * @param req HttpServletRequest 객체
     * @param tripId 특정 여행의 ID
     * @return List<CommentDto> 댓글 목록
     */
    @GetMapping("/user/{tripId}")
    fun getIsLikedByMemberIdForTrip(
        req: HttpServletRequest,
        @PathVariable tripId: String,
    ): List<CommentDto> {
        val memberId = req.getAttribute("memberId") as Long
        return commentService.getIsLikedByMemberIdForTrip(memberId, tripId)
    }

    /**
     * 새로운 댓글을 생성합니다.
     *
     * @param req HttpServletRequest 객체
     * @param commentCreateDto 생성할 댓글 정보
     * @return CommentDto 생성된 댓글 정보
     */
    @PostMapping("")
    fun createComment(
        req: HttpServletRequest,
        @Validated @RequestBody commentCreateDto: CommentCreateDto,
    ): CommentDto {
        val memberId = req.getAttribute("memberId") as Long
        return commentService.createComment(memberId, commentCreateDto)
    }

    /**
     * 특정 부모 댓글에 대한 대댓글을 생성합니다.
     *
     * @param req HttpServletRequest 객체
     * @param parentId 부모 댓글의 ID
     * @param commentChildrenCreateDto 생성할 대댓글 정보
     * @return CommentDto 생성된 대댓글 정보를 담은 부모 댓글 Dto
     */
    @PostMapping("/{parentId}")
    fun createChildrenComment(
        req: HttpServletRequest,
        @PathVariable parentId: String,
        @Validated @RequestBody commentChildrenCreateDto: CommentChildrenCreateDto,
    ): CommentDto {
        val memberId = req.getAttribute("memberId") as Long
        return commentService.createChildrenComment(memberId, parentId, commentChildrenCreateDto)
    }

    /**
     * 특정 댓글을 업데이트합니다.
     *
     * @param req HttpServletRequest 객체
     * @param id 업데이트할 댓글의 ID
     * @param updatedComment 업데이트된 댓글 정보
     * @return CommentDto 업데이트된 댓글 정보
     */
    @PatchMapping("/{id}")
    fun updateCommentById(
        req: HttpServletRequest,
        @PathVariable id: String,
        @Validated @RequestBody updatedComment: CommentUpdateDto,
    ): CommentDto {
        val memberId = req.getAttribute("memberId") as Long
        return commentService.updateComment(memberId, id, updatedComment)
    }

    /**
     * 특정 부모 댓글에 대한 대댓글을 업데이트합니다.
     *
     * @param req HttpServletRequest 객체
     * @param parentId 부모 댓글의 ID
     * @param id 업데이트할 대댓글의 ID
     * @param updatedComment 업데이트된 대댓글 정보
     * @return CommentDto 업데이트된 대댓글 정보를 담은 부모 댓글 Dto
     */
    @PatchMapping("/{parentId}/{id}")
    fun updateChildrenCommentById(
        req: HttpServletRequest,
        @PathVariable parentId: String,
        @PathVariable id: String,
        @Validated @RequestBody updatedComment: CommentUpdateDto,
    ): CommentDto {
        val memberId = req.getAttribute("memberId") as Long
        return commentService.updateChildrenComment(memberId, parentId, id, updatedComment)
    }

    /**
     * 특정 댓글을 삭제합니다.
     *
     * @param req HttpServletRequest 객체
     * @param id 삭제할 댓글의 ID
     * @return ResponseEntity
     */
    @DeleteMapping("/{id}")
    fun deleteComment(req: HttpServletRequest, @PathVariable id: String): ResponseEntity<Any> {
        val memberId = req.getAttribute("memberId") as Long
        commentService.deleteComment(memberId, id)
        return ResponseEntity.status(200).body("성공적으로 댓글이 삭제 되었습니다.")
    }

    /**
     * 특정 부모 댓글에 대한 대댓글을 삭제합니다.
     *
     * @param req HttpServletRequest 객체
     * @param parentId 부모 댓글의 ID
     * @param id 삭제할 대댓글의 ID
     * @return ResponseEntity
     */
    @DeleteMapping("/{parentId}/{id}")
    fun deleteChildrenComment(
        req: HttpServletRequest,
        @PathVariable parentId: String,
        @PathVariable id: String,
    ): ResponseEntity<Any> {
        val memberId = req.getAttribute("memberId") as Long
        commentService.deleteChildrenComment(memberId, parentId, id)
        return ResponseEntity.status(200).body("성공적으로 댓글이 삭제 되었습니다.")
    }

    /**
     * 댓글에 좋아요를 토글합니다.
     *
     * @param req HttpServletRequest 객체
     * @param id 좋아요를 토글할 댓글의 ID
     * @return ResponseEntity<Any> 좋아요가 토글된 댓글의 정보 또는 오류 응답
     */
    @PatchMapping("/{id}/like")
    fun toggleLikeComment(
        req: HttpServletRequest,
        @PathVariable id: String,
    ): ResponseEntity<Any> {
        return try {
            val memberId = req.getAttribute("memberId") as Long
            val updatedComment = commentService.toggleLikeComment(memberId, id)
            ResponseEntity.ok(updatedComment)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(e.message)
        }
    }

    /**
     * 대댓글에 좋아요를 토글합니다.
     *
     * @param req HttpServletRequest 객체
     * @param parentId 부모 댓글의 ID
     * @param id 좋아요를 토글할 대댓글의 ID
     * @return ResponseEntity<Any> 좋아요가 토글된 대댓글의 정보 또는 오류 응답
     */
    @PatchMapping("/{parentId}/{id}/like")
    fun toggleLikeChildrenComment(
        req: HttpServletRequest,
        @PathVariable parentId: String,
        @PathVariable id: String,
    ): ResponseEntity<Any> {
        return try {
            val memberId = req.getAttribute("memberId") as Long
            val updatedChildrenComment = commentService.toggleLikeChildrenComment(memberId, parentId, id)
            ResponseEntity.ok(updatedChildrenComment)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(e.message)
        }
    }
}
