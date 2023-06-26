package com.example.demo.controller

import com.example.demo.dto.CommentDto
import com.example.demo.service.CommentService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/comments")
class CommentController(private val commentService: CommentService) {

    @GetMapping
    fun getCommentsByTripId(@RequestParam tripId: String): List<CommentDto> {
        return commentService.getCommentsByTripId(tripId)
    }
}
