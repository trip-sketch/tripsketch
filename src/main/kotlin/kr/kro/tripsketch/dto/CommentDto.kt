package kr.kro.tripsketch.dto

import java.time.LocalDateTime
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CommentDto(
    val id: String? = null,

    @field:NotBlank(message = "유저 닉네임(userNickName)은 필수 항목입니다.")
    val userNickName: String,

    @field:NotBlank(message = "유저 프로필(userProfileUrl)은 필수 항목입니다.")
    val userProfileUrl: String,

    @field:NotBlank(message = "게시물 아이디(tripId)는 필수 항목입니다.")
    val tripId: String,

    val parentId: String? = null,

    @field:NotBlank(message = "댓글 내용(content)은 필수 항목입니다.")
    @field:Size(max = 200, message = "댓글 내용(content)은 200자 이하로 입력해주세요.")
    val content: String? = null,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val replyToNickname: String? = null,
    val isDeleted: Boolean = false,
    var isLiked: Boolean = false,
    var numberOfLikes: Int = 0,
    val children: MutableList<CommentDto> = mutableListOf(),
)
