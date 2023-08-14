package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.Trip
import kr.kro.tripsketch.repositories.TripRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kr.kro.tripsketch.utils.TokenUtils
import kr.kro.tripsketch.services.JwtService

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
    fun createTrip(actualToken: String, trip: Trip): Trip {
        val userEmail = jwtService.getEmailFromToken(actualToken)
        if (userEmail != null) {
            return tripRepository.save(trip)
        }
    }

    /** Trip 수정 */    // update 틀만 잡기
    // fun updateTrip(id: String, trip: Trip): Trip {
    //     val existingTrip = getTripById(id)
    //     if (existingTrip != null) {
    //         trip.id = ObjectId(id) // String 타입의 id를 ObjectId로 변환하여 trip 객체에 설정
    //         existingTrip.update(trip) // 변경된 필드만 업데이트
    //         return tripRepository.save(existingTrip)
    //     }
    //     throw NoSuchElementException("Trip not found")
    // }

    /** Trip 수정 */    // test 성공
    fun updateTripById(actualToken: String, id: String, trip: Trip): Trip {
        val userEmail = jwtService.getEmailFromToken(actualToken)
        val existingTrip = tripRepository.getTripById(id)

        if (existingTrip != null && existingTrip.userEmail == trip.userEmail){
            if (trip.title != existingTrip.title) {
                existingTrip.title = trip.title
            }
            if (trip.content != existingTrip.content) {
                existingTrip.content = trip.content
            }
            if (trip.likes != existingTrip.likes) {
                existingTrip.likes = trip.likes
            }
            if (trip.views != existingTrip.views) {
                existingTrip.views = trip.views
            }
            if (trip.location != existingTrip.location) {
                existingTrip.location = trip.location
            }
            if (trip.startedAt != existingTrip.startedAt) {
                existingTrip.startedAt = trip.startedAt
            }
            if (trip.endAt != existingTrip.endAt) {
                existingTrip.endAt = trip.endAt
            }
            if (trip.likeFlag != existingTrip.likeFlag) {
                existingTrip.likeFlag = trip.likeFlag
            }

            existingTrip.updatedAt = LocalDateTime.now()

            return tripRepository.save(existingTrip)
        } else {
            throw NoSuchElementException("Trip not found")
        }
    }

    // /** Trip 삭제(hard delete) */
    // fun deleteHardTripById(id: String) {
    //     tripRepository.deleteById(id)
    // }

    /** Trip 삭제(soft delete) */
    // to-do: id 또는 Email 과 작성한 trip 게시글의 사용자가 일치해야 함!, patch로 바꾸기
    fun deleteTripById(actualToken: String, id: String) {
        val userEmail = jwtService.getEmailFromToken(actualToken)
        val existingTrip = tripRepository.findById(id).orElse(null)

        if (existingTrip != null && existingTrip.userEmail == trip.userEmail) {
            trip.deletedAt = LocalDateTime.now()
            trip.hidden = true
            tripRepository.save(trip)
        } else {
            throw NoSuchElementException("Trip not found")
        }
    }
}
