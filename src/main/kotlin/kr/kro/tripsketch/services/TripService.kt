package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.Comment
import kr.kro.tripsketch.domain.Trip
import kr.kro.tripsketch.dto.CommentDto
import kr.kro.tripsketch.dto.TripDto
import kr.kro.tripsketch.dto.TripCreateDto
import kr.kro.tripsketch.dto.TripUpdateDto
import org.bson.types.ObjectId  // ObjectId import
import kr.kro.tripsketch.repositories.TripRepository
import kr.kro.tripsketch.repositories.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TripService(private val tripRepository: TripRepository, private val jwtService: JwtService) {

    fun createTrip(actualToken: String, tripCreateDto: TripCreateDto): TripDto {

        val userEmail = jwtService.getEmailFromToken(actualToken)

        val newTrip = Trip(
            userEmail = userEmail,
            scheduleId = "scheduleId",
            title = tripCreateDto.title,
            content = tripCreateDto.content,
            likes = 0,
            views = 0,
            location = "location",
            startedAt = LocalDateTime.now(),
            endAt = LocalDateTime.now(),
            hashtag = tripCreateDto.hashtag,
            hidden = false,
            createdAt = LocalDateTime.now(),
            updatedAt = null,
            deletedAt = null,
            tripViews = emptySet()
        )

        val createdTrip = tripRepository.save(newTrip)
        return fromTrip(createdTrip)
    }


//    fun createOrUpdateTrip(actualToken: String, tripCreateDto: TripCreateDto): TripDto {
//
//        val userEmail = jwtService.getEmailFromToken(actualToken)
//
//        val newTrip = Trip(
//            userEmail = userEmail,
//            scheduleId = "scheduleId",
//            title = tripCreateDto.title,
//            content = tripCreateDto.content,
//            likes = 0,
//            views = 0,
//            location = "location",
//            startedAt = LocalDateTime.now(),
//            endAt = LocalDateTime.now(),
//            hashtag = tripCreateDto.hashtag,
//            hidden = false,
//            createdAt = LocalDateTime.now(),
//            updatedAt = null,
//            deletedAt = null,
//            tripViews = Set<String> = setOf()
//        )
//        return tripRepository.save(newTrip)
//    }

    fun getAllTrips(actualToken: String): Set<TripDto> {
//        return tripRepository.findAll()
        // to-do: actualToken 이 관리자일경우 해당 API 가 작동하게 해줄까?
        val findTrips = tripRepository.findAll()
        return findTrips.map { fromTrip(it) }.toSet()
    }

    fun getTripById(id: String): TripDto? {
//        return tripRepository.findById(id).orElse(null)
        val findTrip = tripRepository.findById(id).orElse(null)
        return fromTrip(findTrip)
    }

    fun updateTrip(actualToken: String, tripUpdateDto: TripUpdateDto): TripDto {

        val userEmail = jwtService.getEmailFromToken(actualToken)

        val updateTrip = Trip(
            userEmail = userEmail,
            scheduleId = "scheduleId",
            title = tripUpdateDto.title,
            content = tripUpdateDto.content,
            likes = 0,
            views = 0,
            location = "location",
            startedAt = LocalDateTime.now(),
            endAt = LocalDateTime.now(),
            hashtag = tripUpdateDto.hashtag,
            hidden = false,
            createdAt = tripUpdateDto.createdAt,
            updatedAt = LocalDateTime.now(),
//            deletedAt = null,
            tripViews = tripUpdateDto.tripViews
        )

        val updatedTrip = tripRepository.save(updateTrip)
        return fromTrip(updatedTrip)
    }

    fun deleteTripById(id: String) {
        tripRepository.deleteById(id)
    }
}


fun fromTrip(trip: Trip): TripDto {
    return TripDto(
        id = trip.id,
        userEmail = trip.userEmail,
        scheduleId = trip.scheduleId,
        title = trip.title,
        content = trip.content,
        likes = trip.likes,
        views = trip.views,
        location = trip.location,
        startedAt = trip.startedAt,
        endAt = trip.endAt,
        hashtag = trip.hashtag,
        hidden = trip.hidden,
        createdAt = trip.createdAt,
        updatedAt = trip.updatedAt,
        deletedAt = trip.deletedAt,
        tripViews = trip.tripViews
    )

}


// @Service
// class TripService(private val tripRepository: TripRepository) {

//     // Trip 전체 조회
//     fun getAllTrips(): List<Trip> {
//         return tripRepository.findAll()
//     }

//     // Trip 조회
//     fun findById(id: String): Trip? {
//         return tripRepository.findById(id).orElseThrow { NoSuchElementException("Trip not found") }
//     }

//     // Trip 생성 및 수정
//     fun createOrUpdateTrip(tripDto: TripDto): Trip {
//         val trip = Trip(
//             id = tripDto.id,
//             userId = tripDto.userId,
//             scheduleId = tripDto.scheduleId,
//             title = tripDto.title,
//             content = tripDto.content,
//             likes = tripDto.likes,
//             views = tripDto.views,
//             location = tripDto.location,
//             startedAt = tripDto.startedAt,
//             endAt = tripDto.endAt,
//             hashtag = tripDto.hashtag,
//             hidden = tripDto.hidden,
//             createdAt = tripDto.createdAt,
//             updatedAt = tripDto.updatedAt,
//             deletedAt = tripDto.deletedAt,
//             likeFlag = tripDto.likeFlag,
//             tripViews = tripDto.tripViews
//         )
//         return tripRepository.save(trip)
//     }

//     // Trip 생성 및 수정
//     // fun createOrUpdateTrip(tripDto: TripDto): Trip {
//     //     val trip = convertToEntity(tripDto)
//     //     val savedTrip = tripRepository.save(trip)
//     //     return convertToDto(savedTrip)
//     // }

//     // 삭제(soft delete)
//     // fun deleteTripById(id: String) {
//     //     return tripRepository.deleteTripById(id)
//     // }

//     // // 삭제(soft delete)
//     // fun deleteTripById(id: String) {
//     //     val trip = findById(id)
//     fun deleteTripById(id: ObjectId) { // ObjectId 타입으로 변경
//         val trip = findById(id.toString()) // ObjectId를 String으로 변환하여 findById 메서드에 전달
//         if (trip != null) {
//             trip.deletedAt = LocalDateTime.now()
//             trip.hidden = 1 // 값은 체크 다시하기
//             tripRepository.save(trip)
//         } else {
//             throw NoSuchElementException("Trip not found")
//         }
//     }


// }