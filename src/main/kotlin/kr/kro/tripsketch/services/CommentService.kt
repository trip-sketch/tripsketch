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

    fun getCommentByTripId(tripId: String): List<CommentDto> {
        val comments = commentRepository.findAllByTripId(tripId)
        return comments.map { CommentDto.fromComment(it) }
    }

    // Other CRUD operations go here
    fun createComment(dto: CommentDto): Comment {
        val parentComment: Comment? = dto.parentId?.let {
            commentRepository.findById(it).orElse(null)
        }

        val comment = Comment(
            userId = dto.userId,
            tripId = dto.tripId,
            parentId = dto.parentId,
            content = dto.content,
            replyTo = dto.replyTo,
        )

        val savedComment = commentRepository.save(comment)

        // If the parentComment exists, add the new comment as a child to the parent
        parentComment?.let {
            it.children.add(savedComment)
            commentRepository.save(it)
        }

        return savedComment
    }

    fun updateComment(id: String, commentUpdateDto: CommentUpdateDto): CommentDto {
        val comment = commentRepository.findById(id).orElse(null) ?: throw IllegalArgumentException("해당 id 댓글은 존재하지 않습니다.")

        val updatedComment = comment.copy(
            content = commentUpdateDto.content ?: comment.content,
            updatedAt = commentUpdateDto.updatedAt,
        )

        val savedComment = commentRepository.save(updatedComment)

        return CommentDto.fromComment(savedComment)
    }

    fun deleteComment(id: String) {
        val comment = commentRepository.findById(id).orElse(null)
            ?: throw IllegalArgumentException("해당 id 댓글은 존재하지 않습니다.")

        if (comment.parentId != null) {
            // 부모 댓글의 children 목록에서 삭제합니다.
            val parentComment = commentRepository.findById(comment.parentId).orElse(null)
                ?: throw IllegalArgumentException("댓글의 부모 댓글이 존재하지 않습니다.")
            parentComment.children.removeIf { it.id == id }
            commentRepository.save(parentComment)
        }

        commentRepository.delete(comment)
    }
}
