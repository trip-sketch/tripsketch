package kr.kro.tripsketch.trip

import kr.kro.tripsketch.trip.model.Trip
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TripRepository : MongoRepository<Trip, String> {

    /**
     * userId를 기준으로 사용자가 작성한 게시글 조회합니다.
     * @param userId 사용자 ID
     * @return 페이지네이션된 결과
     * */
    fun findByIsHiddenIsFalseAndUserId(userId: String, pageable: Pageable): Page<Trip>

    /**
     * tripId를 기준으로 일치하는 게시글을 조회합니다.
     *  - 단, 비공개 게시글도 포함합니다.
     * @param id 게시물 ID
     * @return 해당 게시글에 해당하는 정보 또는 null
     * */
    fun findByIdAndIsHiddenIsFalse(id: String): Trip?

    /**
     * tripId를 기준으로 일치하는 전체공개 게시글을 조회합니다.
     * @param id 게시물 ID
     * @return 해당 게시글에 해당하는 정보 또는 null
     * */
    fun findByIdAndIsPublicIsTrueAndIsHiddenIsFalse(id: String): Trip?

    /**
     * userId를 기준으로 일치하는 전체공개인 게시글을 조회합니다.
     * @param userId 사용자 Id
     * @return 해당 게시글에 해당하는 정보
     * */
    fun findTripByUserIdAndIsPublicIsTrueAndIsHiddenIsFalse(userId: String): Set<Trip>

    /**
     * 전체공개인 게시글 조회합니다.
     * @param pageable 페이지네이션
     * @return 해당 게시물 목록에 해당하는 정보
     * */
    fun findByIsPublicIsTrueAndIsHiddenIsFalse(pageable: Pageable): Page<Trip>

    /**
     * userId를 기준으로 사용자가 작성한 게시글 조회합니다.
     * @param userId 사용자 Id
     * @param pageable 페이지네이션
     * @return 해당 게시물 목록에 해당하는 정보
     * */
    fun findByIsPublicIsTrueAndIsHiddenIsFalseAndUserId(userId: String, pageable: Pageable): Page<Trip>

    /**
     * 내가 작성한 글을 제외한 전체공개 게시글 목록을  조회합니다.
     * @param userId 사용자 ID
     * @return 해당 게시물에 해당하는 게시물 정보 또는 null
     */
    fun findByIsPublicIsTrueAndIsHiddenIsFalseAndUserIdNot(userId: String): Set<Trip>

    /**
     * 최신 게시물 하나를 찾는 쿼리입니다.
     * @param userId 사용자 ID
     * @return 해당 게시물에 해당하는 게시물 정보 또는 null
     */
    fun findFirstByUserIdAndIsHiddenIsFalseOrderByCreatedAtDesc(userId: String): Trip?

    /**
     * 검색어를 통하여 게시물을 검색합니다.
     * @param keyword 검색어
     * @return 해당 게시물에 해당하는 페이지네이션된 게시물의 정보
     */
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
}
