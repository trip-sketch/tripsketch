package kr.kro.tripsketch.repositories

import kr.kro.tripsketch.domain.Follow
import org.springframework.data.mongodb.repository.MongoRepository

/**
 * `FollowRepository`는 팔로우 관련 데이터 액세스를 위한 리포지토리 인터페이스입니다.
 * MongoDB를 사용하는 경우 Spring Data MongoDB에서 제공하는 `MongoRepository`를 상속받아 사용합니다.
 *
 * `Follow` 엔티티의 CRUD 연산 뿐만 아니라, 특정 팔로우 관계나 팔로우/팔로잉 카운트와 같은
 * 커스텀 쿼리 메서드도 정의되어 있습니다.
 *  @author Hojun Song
 */
interface FollowRepository : MongoRepository<Follow, String> {

    /**
     * 특정 사용자가 팔로우하는 사용자 목록을 반환합니다.
     * @param follower 팔로우하는 사용자의 ID
     * @return 팔로우 목록
     */
    fun findByFollower(follower: String): List<Follow>

    /**
     * 특정 사용자를 팔로우하는 사용자 목록을 반환합니다.
     * @param following 팔로우 당하는 사용자의 ID
     * @return 팔로워 목록
     */
    fun findByFollowing(following: String): List<Follow>

    /**
     * 특정 팔로우 관계가 존재하는지 확인합니다.
     * @param follower 팔로우하는 사용자의 ID
     * @param following 팔로우 당하는 사용자의 ID
     * @return 팔로우 관계가 존재하면 true, 그렇지 않으면 false
     */
    fun existsByFollowerAndFollowing(follower: String, following: String): Boolean

    /**
     * 특정 팔로우 관계를 삭제합니다.
     * @param follower 팔로우하는 사용자의 ID
     * @param following 팔로우 당하는 사용자의 ID
     */
    fun deleteByFollowerAndFollowing(follower: String, following: String)

    /**
     * 특정 사용자가 팔로우하는 사용자 수를 반환합니다.
     * @param follower 팔로우하는 사용자의 ID
     * @return 팔로우 수
     */
    fun countByFollower(follower: String): Long

    /**
     * 특정 사용자를 팔로우하는 사용자 수를 반환합니다.
     * @param following 팔로우 당하는 사용자의 ID
     * @return 팔로워 수
     */
    fun countByFollowing(following: String): Long
}

