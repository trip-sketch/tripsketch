package kr.kro.tripsketch.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "follows")
data class Follow(
    @Id val id: String? = null,
    val follower: String,  // 팔로우 하는 사람의 ID
    val following: String  // 팔로우 당하는 사람의 ID
)