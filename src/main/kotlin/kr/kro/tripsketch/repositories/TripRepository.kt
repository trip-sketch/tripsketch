package kr.kro.tripsketch.repositories

import kr.kro.tripsketch.domain.Trip
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface TripRepository : MongoRepository<Trip, String> {

    // 신규 Trip 생성 및 수정 메서드
    // fun save(trip: Trip): Trip?
    // saveTrip -> Save로 변경

    // 삭제(soft delete)
    // fun deleteTripById(id: String): Trip? {
    //     val trip = findByTripId(id)
    //     if (trip != null) {
    //         trip.deletedAt = LocalDateTime.now()
    //         trip.hidden = 1         // 값은 체크 다시하기
    //         return save(trip)
    //     }
    //     return null
    // }

    // 삭제(hard delete)
    // fun deleteHardTripById(id: String)
}
