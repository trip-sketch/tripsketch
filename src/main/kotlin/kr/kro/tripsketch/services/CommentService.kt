package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.Comment
import kr.kro.tripsketch.dto.CommentDto
import kr.kro.tripsketch.dto.CommentUpdateDto
import kr.kro.tripsketch.repositories.CommentRepository
import org.bson.types.ObjectId // ObjectId를 사용하기 위한 import
import org.springframework.stereotype.Service

@Service
class CommentService(private val commentRepository: CommentRepository) {

    fun getAllComments(): List<CommentDto> {
        return commentRepository.findAll().map { CommentDto.fromComment(it) }
    }

    fun getCommentByTripId(tripId: String): List<CommentDto> {
        val comments = commentRepository.findAllByTripId(tripId)
        return comments.map { CommentDto.fromComment(it) }
    }

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

        if (parentComment == null) {
            // parentId가 없는 경우: 새로운 댓글을 저장하고 반환
            return commentRepository.save(comment)
        } else {
            // parentId가 있는 경우: 새로운 댓글을 부모의 children 리스트에 추가하고 부모 댓글을 저장
            val childComment = Comment(
                id = ObjectId().toString(), // 새로운 ObjectId 생성
                userId = comment.userId,
                tripId = comment.tripId,
                parentId = comment.parentId,
                content = comment.content,
                replyTo = comment.replyTo,
                createdAt = comment.createdAt,
                updatedAt = comment.updatedAt,
                likes = comment.likes,
                likedBy = comment.likedBy,
                // 여기서 children 추가하지 않습니다.
            )
            parentComment.children.add(childComment)
            commentRepository.save(parentComment)
        }
        return commentRepository.save(parentComment)
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

    fun updateChildrenComment(parentId: String, id: String, commentUpdateDto: CommentUpdateDto): CommentDto {
        val parentComment = commentRepository.findById(parentId).orElse(null)
            ?: throw IllegalArgumentException("해당 parentId 댓글은 존재하지 않습니다.")

        val childCommentIndex = parentComment.children.indexOfFirst { it.id == id }
        if (childCommentIndex == -1) {
            throw IllegalArgumentException("해당 id에 대응하는 댓글이 children 존재하지 않습니다.")
        }

        val updatedChildComment = parentComment.children[childCommentIndex].copy(
            content = commentUpdateDto.content ?: parentComment.children[childCommentIndex].content,
            updatedAt = commentUpdateDto.updatedAt,
        )

        parentComment.children[childCommentIndex] = updatedChildComment
        val savedParentComment = commentRepository.save(parentComment)
        println("부모 댓글 업데이트 후: $savedParentComment")
        return CommentDto.fromComment(savedParentComment)
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
