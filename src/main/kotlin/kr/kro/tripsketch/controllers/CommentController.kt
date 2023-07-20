package kr.kro.tripsketch.controllers

import kr.kro.tripsketch.dto.CommentDto
import kr.kro.tripsketch.dto.CommentUpdateDto
import kr.kro.tripsketch.services.CommentService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/comments")
class CommentController(private val commentService: CommentService) {

    @GetMapping
    fun getAllComments(): List<CommentDto> {
        return commentService.getAllComments()
    }

    // Other endpoints go here
    @PostMapping
    fun createComment(@RequestBody commentDto: CommentDto): CommentDto {
        val comment = commentService.createComment(commentDto)
        return CommentDto.fromComment(comment)
    }

    @PatchMapping("/{id}")
    fun updateCommentById(@PathVariable id: String, @RequestBody updatedComment: CommentUpdateDto): CommentDto {
        return commentService.updateComment(id, updatedComment)
    }
}
