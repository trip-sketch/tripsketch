package kr.kro.tripsketch.dto

import java.time.LocalDateTime

data class TripUpdateDto(
    var id: String? = null,
    var userEmail: String,
    var title: String,
    var content: String,
    var location: String? = null,
    var startedAt: LocalDateTime = LocalDateTime.now(),
    var endAt: LocalDateTime = LocalDateTime.now(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    var hashtag: String? = null,
    var updatedAt: LocalDateTime? = null
)
