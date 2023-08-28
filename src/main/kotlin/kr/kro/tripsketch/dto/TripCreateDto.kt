package kr.kro.tripsketch.dto

import java.time.LocalDateTime

data class TripCreateDto(
    var title: String,
    var content: String,
    var location: String? = null,
    var startedAt: LocalDateTime = LocalDateTime.now(),
    var endAt: LocalDateTime = LocalDateTime.now(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    var hashtag: Set<String>? = setOf(),
    var images: List<String>? = emptyList()
)
