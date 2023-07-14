package kr.kro.tripsketch.controllers

import kr.kro.tripsketch.dto.CommentDto
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
        return commentService.createComment(commentDto)
    }
}
