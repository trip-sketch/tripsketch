package kr.kro.tripsketch.controllers

import kr.kro.tripsketch.dto.CommentDto
import kr.kro.tripsketch.dto.CommentUpdateDto
import kr.kro.tripsketch.services.CommentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/comment")
class CommentController(private val commentService: CommentService) {

    @GetMapping("/comments")
    fun getAllComments(): List<CommentDto> {
        return commentService.getAllComments()
    }

    @GetMapping("/{tripId}")
    fun getCommentByTripId(@PathVariable tripId: String): List<CommentDto> {
        return commentService.getCommentByTripId(tripId)
    }

    @PostMapping("")
    fun createComment(@RequestBody commentDto: CommentDto): CommentDto {
        val comment = commentService.createComment(commentDto)
        return CommentDto.fromComment(comment)
    }

    @PatchMapping("/{id}")
    fun updateCommentById(@PathVariable id: String, @RequestBody updatedComment: CommentUpdateDto): CommentDto {
        return commentService.updateComment(id, updatedComment)
    }

    @PatchMapping("/{parentId}/{id}")
    fun updateChildrenCommentById(@PathVariable parentId: String, @PathVariable id: String, @RequestBody updatedComment: CommentUpdateDto): CommentDto {
        return commentService.updateChildrenComment(parentId, id, updatedComment)
    }

    @DeleteMapping("/{id}")
    fun deleteComment(@PathVariable id: String): ResponseEntity<Any> {
        commentService.deleteComment(id)
        return ResponseEntity.status(200).body("성공적으로 삭제 되었습니다.")
    }

    @DeleteMapping("/{parentId}/{id}")
    fun deleteChildrenComment(@PathVariable parentId: String,@PathVariable id: String): ResponseEntity<Any> {
        commentService.deleteChildrenComment(parentId,id)
        return ResponseEntity.status(200).body("성공적으로 삭제 되었습니다.")
    }

    // (이메일 바디에 넣음) 로그인 상태면 알아서 들어가도록 바꿔야함
    @PatchMapping("/{id}/like")
    fun toggleLikeComment(@PathVariable id: String, @RequestParam userEmail: String): CommentDto {
        return commentService.toggleLikeComment(id, userEmail)
    }

    @PatchMapping("/{parentId}/{id}/like")
    fun toggleLikeChildrenComment(@PathVariable parentId: String, @PathVariable id: String, @RequestParam userEmail: String): CommentDto {
        return commentService.toggleLikeChildrenComment(parentId, id, userEmail)
    }
}
