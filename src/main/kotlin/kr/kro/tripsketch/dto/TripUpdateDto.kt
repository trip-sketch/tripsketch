package kr.kro.tripsketch.dto

import kr.kro.tripsketch.domain.HashtagInfo
import java.time.LocalDateTime

data class TripUpdateDto(
    var id: String,
    var title: String,
    var content: String,
    var location: String? = null,
    var startedAt: LocalDateTime? = LocalDateTime.now(),
    var endAt: LocalDateTime? = LocalDateTime.now(),
    var latitude: Double? = null,
    var longitude: Double? = null,
    var hashtagInfo: HashtagInfo? = null,
    var isPublic: Boolean? = true,
    var images: List<String>? = emptyList()
)
