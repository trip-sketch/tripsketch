package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.Trip
import kr.kro.tripsketch.domain.User
import kr.kro.tripsketch.dto.TripDto
import kr.kro.tripsketch.repositories.TripRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TripService(private val tripRepository: TripRepository) {

    // Trip 조회
    fun getTripById(id: String): Trip? {
    // fun getTripById(id: String): TripDto? {
        // val trip = tripRepository.findTripById(id)
        // return trip?.let { convertToDto(it) }
        return tripRepository.findTripById(id)
    }

    // Trip 생성 및 수정
    fun createOrUpdateTrip(tripDto: TripDto): TripDto {
        val trip = convertToEntity(tripDto)
        val savedTrip = tripRepository.save(trip)
        return convertToDto(savedTrip)
    }

    // 삭제(soft delete)
    fun deleteTripById(id: String) {
        return tripRepository.deleteTripById(id)
    }
    
    private fun convertToDto(trip: Trip): TripDto {
        return TripDto(
            trip.id,
            trip.userId,
            trip.scheduleId,
            trip.title,
            trip.content,
            trip.likes,
            trip.views,
            trip.location,
            trip.startedAt,
            trip.endAt,
            trip.hashtag,
            trip.hidden,
            trip.createdAt,
            trip.updatedAt,
            trip.deletedAt,
            trip.likeFlag,
            trip.tripViews
        )
    }

    private fun convertToEntity(tripDto: TripDto): Trip {
        return Trip(
            tripDto.id,
            tripDto.userId,
            tripDto.scheduleId,
            tripDto.title,
            tripDto.content,
            tripDto.likes,
            tripDto.views,
            tripDto.location,
            tripDto.startedAt,
            tripDto.endAt,
            tripDto.hashtag,
            tripDto.hidden,
            tripDto.createdAt,
            tripDto.updatedAt,
            tripDto.deletedAt,
            tripDto.likeFlag,
            tripDto.tripViews
        )
    }
}
