package kr.kro.tripsketch.dto

import com.fasterxml.jackson.annotation.JsonInclude
import kr.kro.tripsketch.domain.HashtagInfo
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TripUpdateResoponseDto(
    var id: String? = null,
    var email: String?,
    var nickname: String?,
    var title: String,
    var content: String,
    var likes: Int?,
    var views: Int?,
    var location: String? = null,
    var startedAt: LocalDateTime = LocalDateTime.now(),
    var endAt: LocalDateTime = LocalDateTime.now(),
    var hashtagInfo: Map<String, HashtagInfo>? = null,
    val latitude: Double? = null, // 위도
    val longitude: Double? = null, // 경도
    var hidden: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime? = null,
    var deletedAt: LocalDateTime? = null,
    var tripLikes: MutableSet<String> = mutableSetOf(),
//    var tripViews: MutableSet<String> = mutableSetOf(),
    var images: List<String>? = emptyList(),
    var isLiked: Boolean
)
