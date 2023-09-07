package kr.kro.tripsketch.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import jakarta.validation.constraints.NotBlank
import org.springframework.data.mongodb.core.index.Indexed
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

@Document(collection = "comments")
data class Comment(
    @Id val id: String? = null,

    @field:NotBlank(message = "User email must not be blank")
    @field:Email(message = "Invalid email format")
    @Indexed(unique = true)
    val userId: String? = null,

    @field:NotBlank(message = "User tripId must not be blank")
    @Indexed(unique = true)
    val tripId: String,

    val parentId: String? = null,

    @field:NotBlank(message = "User content must not be blank")
    @field:Size(max = 200, message = "댓글 내용(content)은 200자 이하로 입력해주세요.")
    val content: String? = null,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val likedBy: MutableSet<String> = mutableSetOf(),
    val replyToUserId: String? = null,
    val children: MutableList<Comment> = mutableListOf(),
    val isDeleted: Boolean = false,
    val isLiked: Boolean = false,
    var numberOfLikes: Int = 0,
)
