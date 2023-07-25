package kr.kro.tripsketch.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.bson.types.ObjectId  // ObjectId import
import java.time.LocalDateTime

@Document(collection = "trips")
data class Trip(
    // @Id var id: String? = null,
    @Id var id: ObjectId = ObjectId.get(),
    // var userId: ObjectId,            // 자동생성되니까.. 일단 String으로 구현!
    // var scheduleId: ObjectId,        // 자동생성되니까.. 일단 String으로 구현!
    var userId: String,   
    var scheduleId: String,
    var title: String,
    var content: String,
    var likes: Int,
    var views: Int,
    var location: String? = null,
    var startedAt: LocalDateTime = LocalDateTime.now(),
    var endAt: LocalDateTime = LocalDateTime.now(),
    var hashtag: String,
    var hidden: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime? = null,
    var deletedAt: LocalDateTime? = null,
    var likeFlag: Int = 0,
    var tripViews: Set<String> = setOf()
)
