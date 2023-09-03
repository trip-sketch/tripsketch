package kr.kro.tripsketch.repositories

import kr.kro.tripsketch.domain.Trip
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TripRepository : MongoRepository<Trip, String> {
    fun findTripByEmail(email: String): Set<Trip>

    // tripLikes 배열의 길이를 조회하는 메소드
    fun countByTripLikes(id: String): Long

    // isHidden 값이 false인 게시물 조회
    fun findByIsHiddenIsFalse(): Set<Trip>

    // isHidden이 false이고 email이 일치하는 여행을 가져오는 메서드
    fun findByIsHiddenIsFalseAndEmail(email: String): Set<Trip>

    // isHidden 값이 false이고, id가 일치하는 게시물 조회
    fun findByIdAndIsHiddenIsFalse(id: String): Trip?

    // email과 isHidden 값이 false인 게시물 조회
    fun findTripByEmailAndIsHiddenIsFalse(email: String): Set<Trip>

    // isPublic 값이 true이고 isHidden 값이 false인 게시물 조회
//    fun findByIsPublicIsTrueAndIsHiddenIsFalse(): Set<Trip>
    fun findByIsPublicIsTrueAndIsHiddenIsFalse(@Param("email") email: String = ""): Set<Trip>

    // email 조건이 맞고, isPublic 값이 true이고 isHidden 값이 false인 게시물 조회
    fun findByIsPublicIsTrueAndIsHiddenIsFalseAndEmail(email: String): Set<Trip>

    fun findByIsPublicIsTrueAndIsHiddenIsFalseAndEmailNot(email: String): Set<Trip>

}