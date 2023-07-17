package kr.kro.tripsketch.repositories

import kr.kro.tripsketch.domain.Trip
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import java.time.LocalDateTime

interface TripRepository : MongoRepository<Trip, String> {

    // 특정 tripId를 조회하는 메서드
    // fun findTripById(id: String): Set<Trip>
    fun findTripById(id: String): Trip?

    // 특정 userId가 작성한 Trip 들을 조회하는 메서드
    fun findTripByUserId(userId: String): Set<Trip>?

    // 특정 시간 이후에 생성된 Trip들을 조회하는 메서드
    fun findTripByCreatedAtAfter(date: LocalDateTime): Set<Trip>?

    // 특정 시간 이전에 생성된 Trip들을 조회하는 메서드
    fun findTripByCreatedAtBefore(date: LocalDateTime): Set<Trip>?

    // 좋아요(likes) 수가 특정 값 이상인 Trip들을 조회하는 메서드
    // fun findTripByLikesGreaterThanEqual(likes: Int): Set<Trip>

    // 좋아요(likes) 수 순으로 Trip들을 정렬하여 조회하는 메서드
    // 여기에서는 Query 어노테이션을 사용하여 직접 쿼리를 작성함
    // @Query("{}")
    // fun findAllOrderByLikesDesc(): Set<Trip>

    // 사용자 정의 쿼리를 사용하여, 특정 userId가 좋아요한 Trip들을 조회하는 메서드
    // @Query("{ 'likedBy': ?0 }")
    // fun findTripLikedByUser(userId: String): Set<Trip>

    // 신규 Trip 생성 및 수정 메서드
    fun save(trip: Trip): Trip?      
    // saveTrip -> Save로 변경

    // 삭제(soft delete)
    fun deleteTripById(id: String): Trip? {
        val trip = findTripById(id)
        if (trip != null) {
            trip.deletedAt = LocalDateTime.now()
            trip.hidden = 1         // 값은 체크 다시하기
            return save(trip)
        }
        return null
    }

    // 삭제(hard delete)
    fun deleteHardTripById(id: String)
}
