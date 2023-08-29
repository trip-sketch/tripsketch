package kr.kro.tripsketch.repositories

import kr.kro.tripsketch.domain.Trip
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface TripRepository : MongoRepository<Trip, String> {
    fun findTripByEmail(email: String): Set<Trip>

    // tripLikes 배열의 길이를 조회하는 메소드
    fun countByTripLikes(id: String): Long

    // hidden 값이 false인 게시물 조회
    fun findByHiddenIsFalse(): Set<Trip>

    // hidden 값이 false이고, id가 일치하는 게시물 조회
    fun findByIdAndHiddenIsFalse(id: String): Trip?

    // email과 hidden 값이 false인 게시물 조회
    fun findTripByEmailAndHiddenIsFalse(email: String): Set<Trip>
}



//    fun findTripByEmail(email: String, pageable: Pageable): Page<Trip>
