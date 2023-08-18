package kr.kro.tripsketch.dto

import java.time.LocalDateTime

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
    val replyTo: String? = null,
    val isDeleted: Boolean = false,
    var isLiked: Boolean = false,
    var numberOfComments: Int = 0,
    val children: MutableList<CommentDto> = mutableListOf(),
)
