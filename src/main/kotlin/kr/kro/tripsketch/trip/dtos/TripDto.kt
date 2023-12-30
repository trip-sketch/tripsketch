package kr.kro.tripsketch.trip.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.Length
import java.time.LocalDate
import java.time.LocalDateTime

data class TripDto(
    var id: String? = null,
    @field:Size(min = 3, max = 50, message = "별명은 3자에서 50자 사이여야 합니다.")
    var nickname: String?,

    @field:NotBlank(message = "제목을 입력하세요.")
    @field:Length(min = 5, max = 100, message = "텍스트 길이는 5자 이상 50자이내여야 합니다.")
    var title: String,

    @field:NotBlank(message = "내용을 입력하세요.")
    @field:Length(min = 5, max = 100, message = "텍스트 길이가 5자 이상이어야 합니다.")
    var content: String,

    var likes: Int?,
    var views: Int?,
    var location: String? = null,
    var startedAt: LocalDate? = null,
    var endAt: LocalDate? = null,
    var hashtag: Set<String>? = setOf(),
    var latitude: Double? = null, // 위도
    var longitude: Double? = null, // 경도
    var isPublic: Boolean,
    var isHidden: Boolean = false,
    val createdAt: LocalDateTime,
    var updatedAt: LocalDateTime? = null,
    var deletedAt: LocalDateTime? = null,
    var images: List<String>? = emptyList(),
    var isLiked: Boolean,
)
