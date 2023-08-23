package kr.kro.tripsketch.controllers

import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import kr.kro.tripsketch.dto.CommentDto
import kr.kro.tripsketch.dto.CommentUpdateDto
import kr.kro.tripsketch.dto.CommentCreateDto
import kr.kro.tripsketch.services.CommentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kr.kro.tripsketch.services.JwtService

@RestController
@RequestMapping("api/comment")
class CommentController(private val commentService: CommentService, private val jwtService: JwtService) {

    @GetMapping("/comments")
    fun getAllComments(): List<CommentDto> {
        return commentService.getAllComments()
    }

    @GetMapping("/{tripId}")
    fun getCommentByTripId(@PathVariable tripId: String): List<CommentDto> {
        return commentService.getCommentByTripId(tripId)
    }

    @GetMapping("/{tripId}/liked")
    fun getIsLikedByTokenForTrip(
        req: HttpServletRequest,
        @PathVariable tripId: String
    ): List<CommentDto> {
        val email = req.getAttribute("userEmail") as String
        return commentService.getIsLikedByTokenForTrip(email, tripId)
    }


    @PostMapping("")
    fun createComment(
        req: HttpServletRequest, @RequestBody commentCreateDto: CommentCreateDto
    ): CommentDto {
        val email = req.getAttribute("userEmail") as String
        return commentService.createComment(email, commentCreateDto)
    }


    @PatchMapping("/{id}")
    fun updateCommentById(
        req: HttpServletRequest,
        @PathVariable id: String,
        @RequestBody updatedComment: CommentUpdateDto
    ): CommentDto {
        val email = req.getAttribute("userEmail") as String
        return commentService.updateComment(id, updatedComment)
    }

    @PatchMapping("/{parentId}/{id}")
    fun updateChildrenCommentById(
        req: HttpServletRequest,
        @PathVariable parentId: String,
        @PathVariable id: String,
        @RequestBody updatedComment: CommentUpdateDto
    ): CommentDto {
        val email = req.getAttribute("userEmail") as String
        return commentService.updateChildrenComment(parentId, id, updatedComment)
    }

    @DeleteMapping("/{id}")
    fun deleteComment(req: HttpServletRequest, @PathVariable id: String): ResponseEntity<Any> {
        val email = req.getAttribute("userEmail") as String
        commentService.deleteComment(id)
        return ResponseEntity.status(200).body("성공적으로 삭제 되었습니다.")
    }

    @DeleteMapping("/{parentId}/{id}")
    fun deleteChildrenComment(
        req: HttpServletRequest,
        @PathVariable parentId: String,
        @PathVariable id: String
    ): ResponseEntity<Any> {
        val email = req.getAttribute("userEmail") as String
        commentService.deleteChildrenComment(parentId, id)
        return ResponseEntity.status(200).body("성공적으로 삭제 되었습니다.")
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
