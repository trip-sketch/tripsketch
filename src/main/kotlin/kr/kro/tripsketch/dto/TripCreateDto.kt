package kr.kro.tripsketch.dto

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

data class TripCreateDto(
    @field:NotBlank(message = "제목을 입력하세요.")
    @field:Length(min = 5, max = 100, message = "텍스트 길이는 5자 이상 50자이내여야 합니다.")
    var title: String,
    @field:NotBlank(message = "내용을 입력하세요.")
    @field:Length(min = 5, max = 100, message = "텍스트 길이가 5자 이상이어야 합니다.")
    var content: String,
    var location: String? = null,
    var startedAt: LocalDate? = LocalDate.now(),
    var endAt: LocalDate? = LocalDate.now(),
    var isPublic: Boolean? = true,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var images: List<MultipartFile>? = null,
    var countryCode: String? = null,
    var country: String? = null,
    var city: String? = null,
    var municipality: String? = null,
    var name: String? = null,
    var displayName: String? = null,
    var road: String? = null,
    var address: String? = null,
    var etc: Set<String>? = null,
)
