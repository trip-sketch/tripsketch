package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.Trip
import kr.kro.tripsketch.dto.TripDto
import kr.kro.tripsketch.repositories.TripRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TripService(private val tripRepository: TripRepository) {

    // Trip 전체 조회
    fun getAllTrips(): List<Trip> {
        return tripRepository.findAll()
    }

    // Trip 조회
    fun findById(id: String): Trip? {
        return tripRepository.findById(id).orElseThrow { NoSuchElementException("Trip not found") }
    }

    // Trip 생성
    fun createTrip(tripDto: TripDto): Trip {
        val trip = Trip(
            id = tripDto.id,
            userId = tripDto.userId,
            scheduleId = tripDto.scheduleId,
            title = tripDto.title,
            content = tripDto.content,
            likes = tripDto.likes,
            views = tripDto.views,
            location = tripDto.location,
            startedAt = tripDto.startedAt,
            endAt = tripDto.endAt,
            hashtag = tripDto.hashtag,
            hidden = tripDto.hidden,
            createdAt = tripDto.createdAt,
            updatedAt = tripDto.updatedAt,
            deletedAt = tripDto.deletedAt,
            likeFlag = tripDto.likeFlag,
            tripViews = tripDto.tripViews
        )
        return tripRepository.save(trip)
    }

    // Trip 생성 및 수정
    // fun createOrUpdateTrip(tripDto: TripDto): Trip {
    //     val trip = convertToEntity(tripDto)
    //     val savedTrip = tripRepository.save(trip)
    //     return convertToDto(savedTrip)
    // }

    // 삭제(soft delete)
    // fun deleteTripById(id: String) {
    //     return tripRepository.deleteTripById(id)
    // }

    // // 삭제(soft delete)
    // fun deleteTripById(id: String) {
    //     val trip = findById(id)
    //     if (trip != null) {
    //         trip.deletedAt = LocalDateTime.now()
    //         trip.hidden = 1 // 값은 체크 다시하기
    //         tripRepository.save(trip)
    //     } else {
    //         throw NoSuchElementException("Trip not found")
    //     }
    // }
}
