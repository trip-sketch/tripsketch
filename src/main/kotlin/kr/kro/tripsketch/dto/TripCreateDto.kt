package kr.kro.tripsketch.dto

import jakarta.validation.constraints.NotBlank
import kr.kro.tripsketch.domain.HashtagInfo
import org.hibernate.validator.constraints.Length
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

data class TripCreateDto(
    @field:NotBlank(message = "제목을 입력하세요.")
    @field:Length(min = 5, max = 100, message = "텍스트 길이는 5자 이상 50자이내여야 합니다.")
    var title: String,
    @field:NotBlank(message = "내용을 입력하세요.")
    @field:Length(min = 5, max = 100, message = "텍스트 길이가 5자 이상이어야 합니다.")
    var content: String,
    var location: String? = null,
    var startedAt: LocalDateTime? = LocalDateTime.now(),
    var endAt: LocalDateTime? = LocalDateTime.now(),
    var latitude: Double? = null,
    var longitude: Double? = null,
    var hashtagInfo: HashtagInfo? = null,
    var isPublic: Boolean? = true,
//    var images: List<String>? = emptyList()
    var images: List<MultipartFile>?
//    var images: MutableList<String> = mutableListOf()
)

data class HashtagInfo(
    val countryCode: String? = null,
    val country: String? = null,
    val city: String? = null,
    val municipality: String? = null,
    val name: String? = null,
    val displayName: String? = null,
    val road: String? = null,
    val address: String? = null,
    val etc: Set<String>? = null
)