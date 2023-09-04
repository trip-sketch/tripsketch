package kr.kro.tripsketch.domain

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.TextIndexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "trips")
data class Trip(
    @Id val id: String? = null,
    @field:Email(message = "올바른 이메일 형식이어야 합니다.")
    @field:NotBlank(message = "이메일은 비워둘 수 없습니다.")
    var email: String,
    @field:NotBlank(message = "제목을 입력하세요.")
    @field:Length(min = 5, max = 100, message = "텍스트 길이는 5자 이상 50자이내여야 합니다.")
    @TextIndexed
    var title: String,
    @field:NotBlank(message = "내용을 입력하세요.")
    @field:Length(min = 5, max = 100, message = "텍스트 길이가 5자 이상이어야 합니다.")
    @TextIndexed
    var content: String,
    var likes: Int = 0,
    var views: Int = 0,
    var location: String? = null,
    var startedAt: LocalDateTime = LocalDateTime.now(),
    var endAt: LocalDateTime = LocalDateTime.now(),
    var isPublic: Boolean? = true,        // 게시글 전체공개 또는 비공개 여부
    var isHidden: Boolean = false,        // 게시글 삭제 여부
    var latitude: Double? = null,
    var longitude: Double? = null,
    var hashtagInfo: HashtagInfo? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime? = LocalDateTime.now(),
    var deletedAt: LocalDateTime? = LocalDateTime.now(),
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