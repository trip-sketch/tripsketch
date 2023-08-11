package kr.kro.tripsketch.dto

import kr.kro.tripsketch.domain.Comment
import java.time.LocalDateTime
import kr.kro.tripsketch.dto.UserProfileDto
import kr.kro.tripsketch.repositories.UserRepository

data class CommentDto(
    val id: String? = null,
    val userEmail: String,
    val userNickName: String,
    val userProfileUrl: String,
    val tripId: String,
    val parentId: String? = null,
    val content: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val likedBy: MutableSet<String> = mutableSetOf(),
    val replyTo: String? = null,
    val isDeleted: Boolean = false,
    val children: MutableList<CommentDto> = mutableListOf(),
    val userRepository: UserRepository, // userRepository 추가
) {
    companion object {
        fun fromComment(comment: Comment, userRepository: UserRepository): CommentDto {
            val commenter = userRepository.findByEmail(comment.userEmail)
            val commenterProfile = commenter?.let {
                UserProfileDto(
                    email = it.email,
                    nickname = it.nickname,
                    introduction = it.introduction,
                    profileImageUrl = it.profileImageUrl
                )
            }

            return CommentDto(
                id = comment.id,
                userEmail = comment.userEmail,
                userNickName = commenterProfile?.nickname ?: "", // 사용자가 없을 경우 대비
                userProfileUrl = commenterProfile?.profileImageUrl ?: "", // 사용자가 없을 경우 대비
                tripId = comment.tripId,
                parentId = comment.parentId,
                content = comment.content,
                createdAt = comment.createdAt,
                updatedAt = comment.updatedAt,
                likedBy = comment.likedBy.toMutableSet(),
                replyTo = comment.replyTo,
                isDeleted = comment.isDeleted,
                children = comment.children.map { fromComment(it, userRepository) }.toMutableList(),
                userRepository = userRepository, // 의존성 주입
            )
        }
    }
}
