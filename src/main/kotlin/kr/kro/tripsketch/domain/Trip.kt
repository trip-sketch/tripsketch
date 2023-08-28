package kr.kro.tripsketch.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import kr.kro.tripsketch.dto.TripDto

@Document(collection = "trips")
data class Trip(
    @Id val id: String? = null,
    var userEmail: String,
    var nickname: String?,
    var title: String,
    var content: String,
    var likes: Int? = 0,
    var views: Int? = 0,
    var location: String? = null,
    var startedAt: LocalDateTime = LocalDateTime.now(),
    var endAt: LocalDateTime = LocalDateTime.now(),
    var hashtag: String? = null,
    var hidden: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime? = null,
    var deletedAt: LocalDateTime? = null,
    var tripViews: Set<String>? = setOf(),
    val latitude: Double? = null,
    val longitude: Double? = null
    // to-do: 이미지 배열 받기
    // var image: 
)