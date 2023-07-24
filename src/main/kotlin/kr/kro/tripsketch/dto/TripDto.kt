package kr.kro.tripsketch.dto

import org.bson.types.ObjectId  // ObjectId import
import java.time.LocalDateTime

data class TripDto(
    var id: String? = null,
    var title: String,
    var content: String,
    var likes: Int,
    var views: Int,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime? = null,
    var deletedAt: LocalDateTime? = null,
    var hidden: Boolean = false
)

// data class TripDto(
//     val id: String? = null,
//     // val userId: String, // 외래키-User객체자체를 참조  // val userId: String,
//     // val scheduleId: String,
//     val userId: ObjectId, // ObjectId로 타입 변경
//     val scheduleId: ObjectId, // ObjectId로 타입 변경
//     var title: String,
//     var content: String,
//     var likes: Int,
//     var views: Int,
//     var location: String? = null,
//     var startedAt: LocalDateTime = LocalDateTime.now(),
//     var endAt: LocalDateTime = LocalDateTime.now(),
//     var hashtag: String,
//     var hidden: Int,
//     val createdAt: LocalDateTime = LocalDateTime.now(),
//     var updatedAt: LocalDateTime? = null,
//     var deletedAt: LocalDateTime? = null,
//     var likeFlag: Int = 0,
//     val tripViews: Set<String> = setOf(),
// )
