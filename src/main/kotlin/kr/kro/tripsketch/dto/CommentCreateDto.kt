package kr.kro.tripsketch.dto

import kr.kro.tripsketch.domain.Comment
import java.time.LocalDateTime
data class CommentCreateDto(
    val tripId: String,
    val parentId: String? = null,
    val content: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val replyTo: String? = null,
)