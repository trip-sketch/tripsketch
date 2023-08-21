package kr.kro.tripsketch.domain

import org.bson.types.ObjectId // ObjectId import
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import kr.kro.tripsketch.dto.TripDto

@Document(collection = "trips")
data class Trip(
     @Id val id: String? = null,
//    @Id var id: ObjectId? = ObjectId.get() ,
    // var userId: ObjectId,            // 자동생성되니까.. 일단 String으로 구현!
    // var scheduleId: ObjectId,        // 자동생성되니까.. 일단 String으로 구현!
//    var userId: String,
    var userEmail: String,
    var nickname: String?,
//    @Id val scheduleId: String? = null,
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
//    var likeFlag: Int = 0,
    var tripViews: Set<String>? = setOf()
    // to-do: 이미지 배열 받기
    // var image: 
)