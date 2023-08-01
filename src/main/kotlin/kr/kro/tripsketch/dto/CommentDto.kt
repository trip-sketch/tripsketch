package kr.kro.tripsketch.dto

import kr.kro.tripsketch.domain.Comment
import java.time.LocalDateTime

data class CommentDto(
    val id: String? = null,
    val userId: String,
    val userNickName: String,          
    val userProfileUrl: String, 
    val tripId: String,
    val parentId: String? = null,
    val content: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val likedBy: List<String> = listOf(),
    val replyTo: String? = null,
    val isDeleted: Boolean = false,
    val children: MutableList<CommentDto> = mutableListOf(),
) {
    companion object {
        fun fromComment(comment: Comment): CommentDto {
            return CommentDto(
                id = comment.id,
                userId = comment.userId,
                userNickName = comment.userNickName,        
                userProfileUrl = comment.userProfileUrl,  
                tripId = comment.tripId,
                parentId = comment.parentId,
                content = comment.content,
                createdAt = comment.createdAt,
                updatedAt = comment.updatedAt,
                likedBy = comment.likedBy,
                replyTo = comment.replyTo,
                isDeleted = comment.isDeleted,
                children = comment.children.map { fromComment(it) }.toMutableList(), // MutableList로 변환
            )
        }
    }
}
