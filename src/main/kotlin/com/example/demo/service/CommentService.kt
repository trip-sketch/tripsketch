package com.example.demo.service

import com.example.demo.domain.Comment
import com.example.demo.dto.CommentDto
import com.example.demo.repositories.CommentRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CommentService(private val commentRepository: CommentRepository) {

    fun getCommentsByTripId(tripId: String): List<CommentDto> {
        return commentRepository.findByTripId(tripId).map { convertToDto(it) }
    }

    private fun convertToDto(comment: Comment): CommentDto {
        return CommentDto(
            comment.id,
            comment.userId,
            comment.tripId,
            comment.parentId,
            comment.content,
            comment.createdAt,
            comment.updatedAt,
            comment.likes,
            comment.likedBy
        )
    }
}
