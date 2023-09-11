package kr.kro.tripsketch.dto

import jakarta.validation.constraints.NotBlank
import kr.kro.tripsketch.domain.HashtagInfo
import org.hibernate.validator.constraints.Length
import java.time.LocalDateTime

data class TripUpdateDto(
    @field:NotBlank(message = "게시물 아이디(tripId)는 필수 항목입니다.")
    var id: String,
    @field:NotBlank(message = "제목을 입력하세요.")
    @field:Length(min = 5, max = 100, message = "텍스트 길이는 5자 이상 50자이내여야 합니다.")
    var title: String,
    @field:NotBlank(message = "내용을 입력하세요.")
    @field:Length(min = 5, max = 100, message = "텍스트 길이가 5자 이상이어야 합니다.")
    var content: String,
    var location: String? = null,
    var startedAt: LocalDateTime? = LocalDateTime.now(),
    var endAt: LocalDateTime? = LocalDateTime.now(),
    var isPublic: Boolean? = true,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var hashtagInfo: HashtagInfo? = null,
    var updatedAt: LocalDateTime? = LocalDateTime.now()
)