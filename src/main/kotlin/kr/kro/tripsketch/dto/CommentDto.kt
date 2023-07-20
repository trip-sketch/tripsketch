package kr.kro.tripsketch.dto

import kr.kro.tripsketch.domain.Comment
import java.time.LocalDateTime

data class CommentDto(
    val id: String? = null,
    val userId: String,
    val tripId: String,
    val parentId: String? = null,
    val content: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val likes: Int = 0,
    val likedBy: List<String> = listOf(),
    val replyTo: String? = null,
) {
    companion object {
        fun fromComment(comment: Comment): CommentDto {
            return CommentDto(
                id = comment.id,
                userId = comment.userId,
                tripId = comment.tripId,
                parentId = comment.parentId,
                content = comment.content,
                createdAt = comment.createdAt,
                updatedAt = comment.updatedAt,
                likes = comment.likes,
                likedBy = comment.likedBy,
                replyTo = comment.replyTo,
            )
        }
    }
}
