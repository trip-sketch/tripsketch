package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.Comment
import kr.kro.tripsketch.dto.CommentDto
import kr.kro.tripsketch.dto.CommentUpdateDto
import kr.kro.tripsketch.repositories.CommentRepository
import org.springframework.stereotype.Service

@Service
class CommentService(private val commentRepository: CommentRepository) {

    fun getAllComments(): List<CommentDto> {
        return commentRepository.findAll().map { CommentDto(it.id, it.userId, it.tripId, it.parentId, it.content, it.createdAt, it.updatedAt, it.likes, it.likedBy, it.replyTo) }
    }

    // Other CRUD operations go here
    fun createComment(dto: CommentDto): Comment {
        val comment = Comment(
            userId = dto.userId,
            tripId = dto.tripId,
            parentId = dto.parentId,
            content = dto.content,
            replyTo = dto.replyTo,
        )

        return commentRepository.save(comment)
    }

    fun updateComment(id: String, commentUpdateDto: CommentUpdateDto): CommentDto {
        val comment = commentRepository.findById(id).orElse(null) ?: throw IllegalArgumentException("해당 id 댓글은 존재하지 않습니다.")

        val updatedComment = comment.copy(
        content = commentUpdateDto.content ?: comment.content,
        updatedAt = commentUpdateDto.updatedAt
    )

    val savedComment = commentRepository.save(updatedComment)

    return CommentDto.fromComment(savedComment)
    }

}
