package kr.kro.tripsketch.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.bson.types.ObjectId  // ObjectId import
import java.time.LocalDateTime

@Document(collection = "trips")
data class Trip(
    // @Id val id: ObjectId, // ObjectId로 타입 변경
    // val userId: ObjectId,         // ObjectId로 타입 변경
    // val scheduleId: ObjectId,     // ObjectId로 타입 변경
    @Id val id: String? = null,
    val userId: String,                     // 외래키-User객체자체를 참조  // val userId: String,
    val scheduleId: String,
    var title: String,
    var content: String,
    var likes: Int,
    var views: Int,
    var location: String? = null,
    var startedAt: LocalDateTime = LocalDateTime.now(),
    var endAt: LocalDateTime = LocalDateTime.now(),
    var hashtag: String,
    var hidden: Int,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime? = null,
    var deletedAt: LocalDateTime? = null,
    var likeFlag: Int = 0,
    val tripViews: Set<String> = setOf(), // tripviews 배열로 set 형태로 받겠다?   - 원하면 redis 로도 해줘도 됌!
)
