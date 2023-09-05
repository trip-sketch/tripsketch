package kr.kro.tripsketch.repositories

import kr.kro.tripsketch.domain.Trip
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface TripRepository : MongoRepository<Trip, String> {
    fun findTripByUserId(userId: String): Set<Trip>

    // tripLikes 배열의 길이를 조회하는 메소드
    fun countByTripLikes(id: String): Long

    // isHidden 값이 false인 게시물 조회
    fun findByIsHiddenIsFalse(): Set<Trip>

    // isHidden 값이 false이고, id가 일치하는 게시물 조회
    fun findByIdAndIsHiddenIsFalse(id: String): Trip?

    // email과 isHidden 값이 false인 게시물 조회
    fun findTripByUserIdAndIsHiddenIsFalse(userId: String): Set<Trip>

    // isPublic 값이 true이고 isHidden 값이 false인 게시물 조회
    fun findByIsPublicIsTrueAndIsHiddenIsFalse(): Set<Trip>
}