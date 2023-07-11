package kr.kro.tripsketch.dto

import java.time.LocalDateTime

data class CommentDto(
    val id: String?,
    val userId: String,
    val tripId: String,
    val parentId: String?,
    val content: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val likes: Int,
    val likedBy: List<String>
)