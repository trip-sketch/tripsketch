package kr.kro.tripsketch.repositories

import kr.kro.tripsketch.domain.Trip
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface TripRepository : MongoRepository<Trip, String> {
    // 별도의 쿼리 메서드가 필요하면 여기에 추가 가능
}
