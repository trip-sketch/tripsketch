package kr.kro.tripsketch.domain

import jakarta.validation.constraints.NotBlank
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "follows")
data class Follow(
    @Id val id: String? = null,

    @field:NotBlank(message = "비워둘 수 없습니다.")
    val follower: String, // 팔로우 하는 사람의 email

    @field:NotBlank(message = "비워둘 수 없습니다.")
    val following: String, // 팔로우 당하는 사람의 email
)
