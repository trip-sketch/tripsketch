package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.Comment
import kr.kro.tripsketch.dto.CommentDto
import kr.kro.tripsketch.repositories.CommentRepository
import org.springframework.stereotype.Service

@Service
class CommentService(private val commentRepository: CommentRepository) {

    fun getAllComments(): List<CommentDto> {
        return commentRepository.findAll().map { CommentDto(it.id, it.userId, it.tripId, it.parentId, it.content, it.createdAt, it.updatedAt, it.likes, it.likedBy, it.replyTo) }
    }

    // Other CRUD operations go here
    fun createComment(dto: CommentDto): CommentDto {
        val comment = Comment(
            userId = dto.userId,
            tripId = dto.tripId,
            parentId = dto.parentId,
            content = dto.content,
            replyTo = dto.replyTo,
        )

        val savedComment = commentRepository.save(comment)

        return CommentDto(
            id = savedComment.id,
            userId = savedComment.userId,
            tripId = savedComment.tripId,
            parentId = savedComment.parentId,
            content = savedComment.content,
            createdAt = savedComment.createdAt,
            updatedAt = savedComment.updatedAt,
            likes = savedComment.likes,
            likedBy = savedComment.likedBy,
            replyTo = savedComment.replyTo,
        )
    }
}
