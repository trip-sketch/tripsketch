package kr.kro.tripsketch.dto

import kr.kro.tripsketch.domain.HashtagInfo
import java.time.LocalDateTime

data class TripCreateDto(
    var title: String,
    var content: String,
    var location: String? = null,
    var startedAt: LocalDateTime = LocalDateTime.now(),
    var endAt: LocalDateTime = LocalDateTime.now(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    var hashtagInfo: HashtagInfo? = null,
    var public: Boolean? = true,
    var images: List<String>? = emptyList()
)
