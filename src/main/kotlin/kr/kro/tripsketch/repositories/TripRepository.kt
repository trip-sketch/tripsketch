package kr.kro.tripsketch.repositories

import kr.kro.tripsketch.domain.Trip
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TripRepository : MongoRepository<Trip, String> {

    // userId를 기반으로 여행을 검색하는 메소드
    fun findTripByUserId(userId: String): Set<Trip>

    // tripLikes 배열의 길이를 조회하는 메소드
    fun countByTripLikes(id: String): Long

    // 삭제할 게시글 조회
    fun findByIsHiddenIsFalseAndId(id: String): Trip?

    // 로그인 사용자가 작성한 게시글 조회 (userId 사용)
    fun findByIsHiddenIsFalseAndUserId(userId: String): Set<Trip>
    fun findByIsHiddenIsFalseAndUserId(userId: String, pageable: Pageable): Page<Trip>

    // trip id가 일치하는 게시글 조회
    fun findByIdAndIsHiddenIsFalse(id: String): Trip?

    // trip id가 일치하는 전체공개 게시글 조회
    fun findByIdAndIsPublicIsTrueAndIsHiddenIsFalse(id: String): Trip?

    // 로그인 사용자 외 작성자가 작성한 게시글 조회
    fun findByUserIdAndIsHiddenIsFalse(userId: String): Set<Trip>

    // isHidden 값이 false인 게시물 조회
    fun findTripByUserIdAndIsHiddenIsFalse(userId: String): Set<Trip>

    // 유저 아이디로 공개 + 삭제x 인 글들 조회
    fun findTripByUserIdAndIsPublicIsTrueAndIsHiddenIsFalse(userId: String): Set<Trip>

    // 전체공개 게시글 조회
    fun findByIsPublicIsTrueAndIsHiddenIsFalse(userId: String = ""): Set<Trip>
    fun findByIsPublicIsTrueAndIsHiddenIsFalse(pageable: Pageable): Page<Trip>

    // email 조건이 맞으면서 전체공개 게시글 조회
    fun findByIsPublicIsTrueAndIsHiddenIsFalseAndUserId(userId: String): Set<Trip>
    fun findByIsPublicIsTrueAndIsHiddenIsFalseAndUserIdIn(userIds: Set<String>): Set<Trip>
    fun findByIsPublicIsTrueAndIsHiddenIsFalseAndUserId(userId: String, pageable: Pageable): Page<Trip>

    fun findByIsPublicIsTrueAndIsHiddenIsFalseAndUserIdNot(userId: String): Set<Trip>

    fun findLatestTripByUserId(userId: String): Trip?

    // 최신 게시물 하나를 찾는 쿼리
    fun findFirstByUserIdAndIsHiddenIsFalseOrderByCreatedAtDesc(userId: String): Trip?

    @Query(
        "{" +
            "\$or: [" +
            "   { 'title': { \$regex: ?0, \$options: 'i' } }, " +
            "   { 'content': { \$regex: ?0, \$options: 'i' } }, " +
            "]," +
            "'isPublic': true, 'isHidden': false" +
            "}",
    )
    fun findTripsByKeyword(keyword: String, pageable: Pageable): Page<Trip>

    @Query(
        "{" +
            "\$or: [" +
            "   { 'title': { \$regex: ?0, \$options: 'i' } }, " +
            "   { 'content': { \$regex: ?0, \$options: 'i' } }" +
            "]," +
            "'isPublic': true, 'isHidden': false" +
            "}",
    )
    fun findTripsByKeywordWithLikes(keyword: String, sorting: Sort = Sort.by(Sort.Order.desc("likes"))): List<Trip>

//    @Query("{" +
//            "\$or: [" +
//            "   { 'title': { \$regex: ?0, \$options: 'i' } }, " +
//            "   { 'content': { \$regex: ?0, \$options: 'i' } }" +
//            "]," +
//            "'isPublic': true, 'isHidden': false" +
//            "}")
//    fun findTripsByKeywordWithLikes(keyword: String, sorting: Sort = Sort.by(Sort.Order.desc("likes"))): Trip?

//    @Query("{" +
//            "\$or: [" +
//            "   { 'title': { \$regex: ?0, \$options: 'i' } }, " +
//            "   { 'content': { \$regex: ?0, \$options: 'i' } }, " +
//            "]," +
//            "'isPublic': true, 'isHidden': false" +
//            "}",
//    )
//    fun findTripsByKeyword(keyword: String, sorting: Sort, pageable: Pageable): Page<Trip>
}
