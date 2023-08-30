package kr.kro.tripsketch.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

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
    var public: Boolean? = true,        // 게시글 전체공개 또는 비공개 여부
    var hidden: Boolean = false,        // 게시글 삭제 여부
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