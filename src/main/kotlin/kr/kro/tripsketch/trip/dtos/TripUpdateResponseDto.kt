package kr.kro.tripsketch.trip.dtos

import com.fasterxml.jackson.annotation.JsonInclude
import kr.kro.tripsketch.trip.HashtagInfo
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 여행 정보 수정 응답 DTO 클래스입니다.
 *
 * @author BYEONGUK KO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TripUpdateResponseDto(
    var id: String? = null,
    var nickname: String?,
    var title: String,
    var content: String,
    var likes: Int?,
    var views: Int?,
    var location: String? = null,
    var startedAt: LocalDate? = LocalDate.now(),
    var endAt: LocalDate? = LocalDate.now(),
    var hashtagInfo: HashtagInfo? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    var isPublic: Boolean,
    var isHidden: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime? = null,
    var deletedAt: LocalDateTime? = null,
    var tripLikes: MutableSet<String> = mutableSetOf(),
    var images: List<String>? = emptyList(),
    var isLiked: Boolean,
)
