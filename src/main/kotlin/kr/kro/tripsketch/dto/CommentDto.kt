package kr.kro.tripsketch.dto

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
)
