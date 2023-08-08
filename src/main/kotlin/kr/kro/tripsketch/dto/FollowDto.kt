package kr.kro.tripsketch.dto

import jakarta.validation.constraints.Email

data class FollowDto(
    @field:Email(message = "유효한 이메일 주소를 입력해주세요.")
    val email: String
)