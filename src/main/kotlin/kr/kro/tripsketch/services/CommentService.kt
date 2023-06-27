package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.Comment
import kr.kro.tripsketch.dto.CommentDto
import kr.kro.tripsketch.repositories.CommentRepository
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
