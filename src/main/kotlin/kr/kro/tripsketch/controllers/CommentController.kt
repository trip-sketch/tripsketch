package kr.kro.tripsketch.controllers

import kr.kro.tripsketch.dto.CommentDto
import kr.kro.tripsketch.dto.CommentUpdateDto
import kr.kro.tripsketch.dto.CommentCreateDto
import kr.kro.tripsketch.services.CommentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kr.kro.tripsketch.utils.TokenUtils
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
        @RequestHeader("Authorization") token: String,
        @PathVariable tripId: String
    ): List<CommentDto> {
        val actualToken = TokenUtils.validateAndExtractToken(jwtService, token)
        return commentService.getIsLikedByTokenForTrip(actualToken, tripId)
    }


    @PostMapping("")
    fun createComment(
        @RequestHeader("Authorization") token: String, @RequestBody commentCreateDto: CommentCreateDto
    ): CommentDto {
        val actualToken = TokenUtils.validateAndExtractToken(jwtService, token)
        return commentService.createComment(actualToken, commentCreateDto)
    }


    @PatchMapping("/{id}")
    fun updateCommentById(
        @RequestHeader("Authorization") token: String,
        @PathVariable id: String,
        @RequestBody updatedComment: CommentUpdateDto
    ): CommentDto {
        TokenUtils.validateAndExtractToken(jwtService, token)
        return commentService.updateComment(id, updatedComment)
    }

    @PatchMapping("/{parentId}/{id}")
    fun updateChildrenCommentById(
        @RequestHeader("Authorization") token: String,
        @PathVariable parentId: String,
        @PathVariable id: String,
        @RequestBody updatedComment: CommentUpdateDto
    ): CommentDto {
        TokenUtils.validateAndExtractToken(jwtService, token)
        return commentService.updateChildrenComment(parentId, id, updatedComment)
    }

    @DeleteMapping("/{id}")
    fun deleteComment(@RequestHeader("Authorization") token: String, @PathVariable id: String): ResponseEntity<Any> {
        TokenUtils.validateAndExtractToken(jwtService, token)
        commentService.deleteComment(id)
        return ResponseEntity.status(200).body("성공적으로 삭제 되었습니다.")
    }

    @DeleteMapping("/{parentId}/{id}")
    fun deleteChildrenComment(
        @RequestHeader("Authorization") token: String,
        @PathVariable parentId: String,
        @PathVariable id: String
    ): ResponseEntity<Any> {
        TokenUtils.validateAndExtractToken(jwtService, token)
        commentService.deleteChildrenComment(parentId, id)
        return ResponseEntity.status(200).body("성공적으로 삭제 되었습니다.")
    }

    @PatchMapping("/{id}/like")
    fun toggleLikeComment(
        @RequestHeader("Authorization") token: String,
        @PathVariable id: String
    ): ResponseEntity<Any> {
        val actualToken = TokenUtils.validateAndExtractToken(jwtService, token)

        return try {
            val updatedComment = commentService.toggleLikeComment(actualToken, id)
            ResponseEntity.ok(updatedComment)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(e.message)
        }
    }

    @PatchMapping("/{parentId}/{id}/like")
    fun toggleLikeChildrenComment(
        @RequestHeader("Authorization") token: String,
        @PathVariable parentId: String,
        @PathVariable id: String
    ): ResponseEntity<Any> {
        val actualToken = TokenUtils.validateAndExtractToken(jwtService, token)

        return try {
            val updatedChildrenComment = commentService.toggleLikeChildrenComment(actualToken, parentId, id)
            ResponseEntity.ok(updatedChildrenComment)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(e.message)
        }
    }
}
