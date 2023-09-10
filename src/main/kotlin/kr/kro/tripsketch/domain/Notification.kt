package kr.kro.tripsketch.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document
data class Notification(
    @Id
    var id: String? = null,

    var receiverId: String,

    var title: String,

    var body: String,

    var commentId: String? = null,

    var parentId: String? = null,

    var tripId: String? = null,

    var nickname: String? = null,

    var profileUrl: String? = null,

    @Indexed(expireAfterSeconds = 30 * 24 * 60 * 60) // 30 days in seconds
    var createdAt: LocalDateTime = LocalDateTime.now(),
)
