package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.Trip
import kr.kro.tripsketch.dto.TripDto
import org.bson.types.ObjectId  // ObjectId import
import kr.kro.tripsketch.repositories.TripRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TripService(private val tripRepository: TripRepository) {

    /** Trip 전체 조회 */
    fun getAllTrips(): List<Trip> {
        return tripRepository.findAll()
    }

    /** Trip Id 조회 */
    fun getTripById(id: String): Trip? {
        return tripRepository.findById(id).orElse(null)
    }

    /** Trip 생성 */
    fun createTrip(trip: Trip): Trip {
        return tripRepository.save(trip)
    }

    /** Trip 수정 */
    fun updateTrip(trip: Trip): Trip {
        val existingTrip = getTripById(trip.id.toHexString())
        if (existingTrip != null) {
            existingTrip.update(trip) // 변경된 필드만 업데이트
            return tripRepository.save(existingTrip)
        }
        throw NoSuchElementException("Trip not found")
    }

    // /** Trip 삭제(hard delete) */
    // fun deleteHardTripById(id: String) {
    //     tripRepository.deleteById(id)
    // }

    /** Trip 삭제(soft delete) */
    fun deleteTripById(id: String) {
        val trip = tripRepository.findById(id).orElse(null)
        if (trip != null) {
            trip.deletedAt = LocalDateTime.now()
            trip.hidden = true // 혹은 1에 해당하는 값으로 설정
            tripRepository.save(trip)
        } else {
            throw NoSuchElementException("Trip not found")
        }
    }
}
