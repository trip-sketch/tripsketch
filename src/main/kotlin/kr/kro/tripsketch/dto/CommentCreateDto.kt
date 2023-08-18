package kr.kro.tripsketch.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
data class CommentCreateDto(
    @field:NotBlank(message = "게시물 아이디(tripId)는 필수 항목입니다.")
    val tripId: String,

    @field:NotBlank(message = "댓글 내용(content)은 필수 항목입니다.")
    @field:Size(max = 200, message = "댓글 내용(content)은 200자 이하로 입력해주세요.")
    val content: String,

    val parentId: String? = null,
    val replyTo: String? = null
)