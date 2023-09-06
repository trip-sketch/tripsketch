package kr.kro.tripsketch.controllers

import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import kr.kro.tripsketch.dto.CommentChildrenCreateDto
import kr.kro.tripsketch.dto.CommentDto
import kr.kro.tripsketch.dto.CommentUpdateDto
import kr.kro.tripsketch.dto.CommentCreateDto
import kr.kro.tripsketch.services.CommentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.validation.annotation.Validated

@RestController
@RequestMapping("api/comment")
class CommentController(private val commentService: CommentService) {

    @GetMapping("/admin/comments")
    fun getAllComments(req: HttpServletRequest, pageable: Pageable): Page<CommentDto>{
        return commentService.getAllComments(pageable)
    }

    @GetMapping("/guest/{tripId}")
    fun getCommentsByTripId(@PathVariable tripId: String): List<CommentDto> {
        return commentService.getCommentsByTripId(tripId)
    }

    @GetMapping("/user/{tripId}")
    fun getIsLikedByTokenForTrip(
        req: HttpServletRequest,
        @PathVariable tripId: String
    ): List<CommentDto> {
        val email = req.getAttribute("userEmail") as String
        return commentService.getIsLikedByTokenForTrip(email, tripId)
    }


    @PostMapping("")
    fun createComment(
        req: HttpServletRequest, @Validated @RequestBody commentCreateDto: CommentCreateDto
    ): CommentDto {
        val email = req.getAttribute("userEmail") as String
        return commentService.createComment(email, commentCreateDto)
    }

    @PostMapping("/{parentId}")
    fun createChildrenComment(
        req: HttpServletRequest, @PathVariable parentId: String, @Validated @RequestBody commentChildrenCreateDto: CommentChildrenCreateDto
    ): CommentDto {
        val email = req.getAttribute("userEmail") as String
        return commentService.createChildrenComment(email, parentId, commentChildrenCreateDto)
    }

    @PatchMapping("/{id}")
    fun updateCommentById(
        req: HttpServletRequest,
        @PathVariable id: String,
        @Validated @RequestBody updatedComment: CommentUpdateDto
    ): CommentDto {
        val email = req.getAttribute("userEmail") as String
        return commentService.updateComment(email, id, updatedComment)
    }

    @PatchMapping("/{parentId}/{id}")
    fun updateChildrenCommentById(
        req: HttpServletRequest,
        @PathVariable parentId: String,
        @PathVariable id: String,
        @Validated @RequestBody updatedComment: CommentUpdateDto
    ): CommentDto {
        val email = req.getAttribute("userEmail") as String
        return commentService.updateChildrenComment(email, parentId, id, updatedComment)
    }

    @DeleteMapping("/{id}")
    fun deleteComment(req: HttpServletRequest, @PathVariable id: String): ResponseEntity<Any> {
        val email = req.getAttribute("userEmail") as String
        commentService.deleteComment(email, id)
        return ResponseEntity.status(200).body("성공적으로 댓글이 삭제 되었습니다.")
    }

    @DeleteMapping("/{parentId}/{id}")
    fun deleteChildrenComment(
        req: HttpServletRequest,
        @PathVariable parentId: String,
        @PathVariable id: String
    ): ResponseEntity<Any> {
        val email = req.getAttribute("userEmail") as String
        commentService.deleteChildrenComment(email, parentId, id)
        return ResponseEntity.status(200).body("성공적으로 댓글이 삭제 되었습니다.")
    }

    @PatchMapping("/{id}/like")
    fun toggleLikeComment(
        req: HttpServletRequest,
        @PathVariable id: String
    ): ResponseEntity<Any> {

        return try {
            val email = req.getAttribute("userEmail") as String
            val updatedComment = commentService.toggleLikeComment(email, id)
            ResponseEntity.ok(updatedComment)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(e.message)
        }
    }

    @PatchMapping("/{parentId}/{id}/like")
    fun toggleLikeChildrenComment(
        req: HttpServletRequest,
        @PathVariable parentId: String,
        @PathVariable id: String
    ): ResponseEntity<Any> {

        return try {
            val email = req.getAttribute("userEmail") as String
            val updatedChildrenComment = commentService.toggleLikeChildrenComment(email, parentId, id)
            ResponseEntity.ok(updatedChildrenComment)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(e.message)
        }
    }
}
