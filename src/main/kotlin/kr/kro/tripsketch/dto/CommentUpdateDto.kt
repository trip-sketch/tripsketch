package kr.kro.tripsketch.dto

import jakarta.validation.constraints.NotBlank

data class CommentUpdateDto(
    @field:NotBlank(message = "댓글 내용(content)은 필수 항목입니다.")
    val content: String? = null,
)
