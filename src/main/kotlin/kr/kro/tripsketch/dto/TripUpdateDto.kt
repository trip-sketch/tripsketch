package kr.kro.tripsketch.dto

import java.time.LocalDateTime

data class TripUpdateDto(
    var id: String? = null,
    var email: String,
    var title: String,
    var content: String,
    var location: String? = null,
    var startedAt: LocalDateTime = LocalDateTime.now(),
    var endAt: LocalDateTime = LocalDateTime.now(),
    var hashtag: Set<String>? = setOf(),
    var updatedAt: LocalDateTime? = null,
    var images: List<String>? = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
)
