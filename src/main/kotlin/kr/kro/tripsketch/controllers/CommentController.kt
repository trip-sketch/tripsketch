package kr.kro.tripsketch.controllers

import jakarta.servlet.http.HttpServletRequest
import kr.kro.tripsketch.dto.CommentChildrenCreateDto
import kr.kro.tripsketch.dto.CommentCreateDto
import kr.kro.tripsketch.dto.CommentDto
import kr.kro.tripsketch.dto.CommentUpdateDto
import kr.kro.tripsketch.services.CommentService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/comment")
class CommentController(private val commentService: CommentService) {

    @GetMapping("/admin/comments")
    fun getAllComments(req: HttpServletRequest, pageable: Pageable): Page<CommentDto> {
        return commentService.getAllComments(pageable)
    }

    @GetMapping("/admin/commentsWithPagination")
    fun getAllComments(
        @RequestParam("page", required = false, defaultValue = "1") page: Int,
        @RequestParam("pageSize", required = false, defaultValue = "10") pageSize: Int
    ): ResponseEntity<Map<String, Any>> {
        val paginatedComments = commentService.getAllCommentsWithPagination(page, pageSize)
        return ResponseEntity.ok(paginatedComments)
    }

    @GetMapping("/guest/{tripId}")
    fun getCommentsByTripId(@PathVariable tripId: String): List<CommentDto> {
        return commentService.getCommentsByTripId(tripId)
    }

    @GetMapping("/user/{tripId}")
    fun getIsLikedByTokenForTrip(
        req: HttpServletRequest,
        @PathVariable tripId: String,
    ): List<CommentDto> {
        val memberId = req.getAttribute("memberId") as Long
        return commentService.getIsLikedByTokenForTrip(memberId, tripId)
    }

    @PostMapping("")
    fun createComment(
        req: HttpServletRequest,
        @Validated @RequestBody commentCreateDto: CommentCreateDto,
    ): CommentDto {
        val memberId = req.getAttribute("memberId") as Long
        return commentService.createComment(memberId, commentCreateDto)
    }

    @PostMapping("/{parentId}")
    fun createChildrenComment(
        req: HttpServletRequest,
        @PathVariable parentId: String,
        @Validated @RequestBody commentChildrenCreateDto: CommentChildrenCreateDto,
    ): CommentDto {
        val memberId = req.getAttribute("memberId") as Long
        return commentService.createChildrenComment(memberId, parentId, commentChildrenCreateDto)
    }

    @PatchMapping("/{id}")
    fun updateCommentById(
        req: HttpServletRequest,
        @PathVariable id: String,
        @Validated @RequestBody updatedComment: CommentUpdateDto,
    ): CommentDto {
        val memberId = req.getAttribute("memberId") as Long
        return commentService.updateComment(memberId, id, updatedComment)
    }

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

    @DeleteMapping("/{id}")
    fun deleteComment(req: HttpServletRequest, @PathVariable id: String): ResponseEntity<Any> {
        val memberId = req.getAttribute("memberId") as Long
        commentService.deleteComment(memberId, id)
        return ResponseEntity.status(200).body("성공적으로 댓글이 삭제 되었습니다.")
    }

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
