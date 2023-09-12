package kr.kro.tripsketch.dto

import jakarta.validation.constraints.NotBlank

data class TripIdDto(
    @field:NotBlank(message = "게시물 아이디(tripId)는 필수 항목입니다.")
    val id: String,
)
