package kr.kro.tripsketch.dto

import java.time.LocalDateTime

data class TripUpdateDto(
    var id: String,
    var title: String,
    var content: String,
    var location: String? = null,
    var startedAt: LocalDateTime? = LocalDateTime.now(),
    var endAt: LocalDateTime? = LocalDateTime.now(),
    var hashtag: Set<String>? = setOf(),
    var public: Boolean? = true,
    var images: List<String>? = emptyList()
)
