package kr.kro.tripsketch.dto

import kr.kro.tripsketch.domain.HashtagInfo
import java.time.LocalDateTime

data class TripUpdateDto(
    var id: String? = null,
//    var email: String,
    var title: String,
    var content: String,
    var location: String? = null,
    var startedAt: LocalDateTime = LocalDateTime.now(),
    var endAt: LocalDateTime = LocalDateTime.now(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    var hashtagInfo: HashtagInfo? = null,
    var public: Boolean? = true,
    var updatedAt: LocalDateTime? = null,
    var images: List<String>? = emptyList()
)
