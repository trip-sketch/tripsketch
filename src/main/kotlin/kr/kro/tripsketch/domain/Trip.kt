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
    var scheduleId: String,
    var title: String,
    var content: String,
    var likes: Int,
    var views: Int,
    var location: String? = null,
    var startedAt: LocalDateTime = LocalDateTime.now(),
    var endAt: LocalDateTime = LocalDateTime.now(),
    var hashtag: String? = null,
    var hidden: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime? = null,
    var deletedAt: LocalDateTime? = null,
//    var likeFlag: Int = 0,
    var tripViews: Set<String> = setOf()
) {
    fun update(updatedTrip: Trip) {
        title = updatedTrip.title
        content = updatedTrip.content
        likes = updatedTrip.likes
        views = updatedTrip.views
        location = updatedTrip.location
        // hidden = updatedTrip.hidden // 추가: hidden 프로퍼티 업데이트
        updatedAt = LocalDateTime.now()
    }

    fun TripDto.toTrip(): Trip {
        return Trip(
//            userId = this.userId,
//            id = this.userId,
            userEmail = this.userEmail,
            scheduleId = this.scheduleId,
            title = this.title,
            content = this.content,
            likes = this.likes,
            views = this.views,
            location = this.location,
            startedAt = this.startedAt,
            endAt = this.endAt,
            hashtag = this.hashtag,
            hidden = this.hidden,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            deletedAt = this.deletedAt,
//            likeFlag = this.likeFlag,
            tripViews = this.tripViews
        )
    }
}
