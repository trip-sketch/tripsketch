package kr.kro.tripsketch.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "comments")
data class Comment(
    @Id val id: String? = null,
    val userEmail: String,
    val tripId: String,
    val parentId: String? = null,
    val content: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val likedBy: MutableSet<String> = mutableSetOf(),
    val replyTo: String? = null,
    val children: MutableList<Comment> = mutableListOf(),
    val isDeleted: Boolean = false,
    val isLiked: Boolean = false,
    var numberOfComments: Int = 0,
)
