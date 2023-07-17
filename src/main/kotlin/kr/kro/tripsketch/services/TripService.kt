package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.Trip
import kr.kro.tripsketch.repositories.TripRepository
import org.springframework.stereotype.Service

@Service
class TripService(private val tripRepository: TripRepository) {

    // Trip 조회
    fun findById(id: String): Trip? {
        return tripRepository.findById(id).orElseThrow { NoSuchElementException("Trip not found") }
    }

    fun getAllTrips(): List<Trip> {
        return tripRepository.findAll()
    }

    // Trip 생성 및 수정
    // fun createOrUpdateTrip(tripDto: TripDto): TripDto {
    //     val trip = convertToEntity(tripDto)
    //     val savedTrip = tripRepository.save(trip)
    //     return convertToDto(savedTrip)
    // }

    // 삭제(soft delete)
    // fun deleteTripById(id: String) {
    //     return tripRepository.deleteTripById(id)
    // }
}
