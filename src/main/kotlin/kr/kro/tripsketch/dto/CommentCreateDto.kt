package kr.kro.tripsketch.dto

import java.time.LocalDateTime
data class CommentCreateDto(
    val tripId: String,
    val parentId: String? = null,
    val content: String? = null,
    val replyTo: String? = null,
)