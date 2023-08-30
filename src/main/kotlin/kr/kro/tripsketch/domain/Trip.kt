package kr.kro.tripsketch.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import kr.kro.tripsketch.dto.TripDto

@Document(collection = "trips")
data class Trip(
    @Id val id: String? = null,
    var email: String,
    var title: String,
    var content: String,
    var likes: Int = 0,
    var views: Int = 0,
    var location: String? = null,
    var startedAt: LocalDateTime = LocalDateTime.now(),
    var endAt: LocalDateTime = LocalDateTime.now(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    var hashtagInfo: Map<String, HashtagInfo>? = null,
    var hidden: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime? = null,
    var deletedAt: LocalDateTime? = null,
    var tripLikes: MutableSet<String> = mutableSetOf(),
    var tripViews: MutableSet<String> = mutableSetOf(),
    var images: List<String>? = emptyList()
    // to-do: 이미지 배열 받기
    // var image:
)

data class HashtagInfo(
    val countryCode: String? = null,
    val country: String? = null,
    val city: String? = null,
    val municipality: String? = null,
    val name: String? = null,
    val displayName: String? = null,
    val road: String? = null,
    val address: String? = null,
    val etc: Set<String>? = null
)