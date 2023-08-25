package kr.kro.tripsketch.repositories

import kr.kro.tripsketch.domain.Trip
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Repository
interface TripRepository : MongoRepository<Trip, String> {
    fun findTripByEmail(email: String): Set<Trip>
//    fun findTripByEmail(email: String, pageable: Pageable): Page<Trip>
}
