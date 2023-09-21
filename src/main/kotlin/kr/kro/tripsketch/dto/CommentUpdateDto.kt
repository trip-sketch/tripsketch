package kr.kro.tripsketch.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 댓글 수정 시 요청 시 사용할 DTO 클래스입니다.
 *
 * @property content 댓글 내용 (Required, 최대 200자)
 * @author BYEONGUK KO
 */
data class CommentUpdateDto(
    @field:NotBlank(message = "댓글 내용(content)은 필수 항목입니다.")
    @field:Size(max = 200, message = "댓글 내용(content)은 200자 이하로 입력해주세요.")
    val content: String? = null,
)
