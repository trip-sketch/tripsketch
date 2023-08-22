package kr.kro.tripsketch.dto

import java.time.LocalDateTime

data class TripCreateDto(
    var id: String? = null,
    var userEmail: String,
    var title: String,
    var content: String,
    var location: String? = null,
    var startedAt: LocalDateTime = LocalDateTime.now(),
    var endAt: LocalDateTime = LocalDateTime.now(),
    var hashtag: String? = null
)
