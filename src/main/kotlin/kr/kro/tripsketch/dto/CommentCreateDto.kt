package kr.kro.tripsketch.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
data class CommentCreateDto(
    @field:NotBlank(message = "tripId는 필수 항목입니다.")
    val tripId: String,

    @field:NotBlank(message = "content는 필수 항목입니다.")
    @field:Size(max = 200, message = "content는 200자 이하로 입력해주세요.")
    val content: String,

    val parentId: String? = null,
    val replyTo: String? = null
)