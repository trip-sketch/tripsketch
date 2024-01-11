package kr.kro.tripsketch.follow.dtos

import jakarta.validation.constraints.NotBlank

/**
 * @author Hojun Song
 */
data class FollowDto(
    @field:NotBlank(message = "닉네임을 입력해주세요")
    val nickname: String,
)
