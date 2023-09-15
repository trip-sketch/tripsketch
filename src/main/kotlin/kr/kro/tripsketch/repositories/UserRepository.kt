package kr.kro.tripsketch.repositories

import kr.kro.tripsketch.domain.User
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * `UserRepository`는 사용자 관련 데이터 액세스를 위한 리포지토리 인터페이스입니다.
 * MongoDB를 사용하는 경우, Spring Data MongoDB에서 제공하는 `MongoRepository`를 상속받아 사용합니다.
 *
 * `User` 엔티티의 CRUD 연산 뿐만 아니라, 회원 ID, 닉네임, 리프레시 토큰 등을 기반으로 한
 * 사용자 조회 및 존재 여부 확인과 같은 커스텀 쿼리 메서드도 정의되어 있습니다.
 * @author Hojun Song
 */
@Repository
interface UserRepository : MongoRepository<User, String> {

    /**
     * 회원 ID를 기반으로 사용자를 조회합니다.
     * @param memberId 회원 ID
     * @return 해당 회원 ID에 해당하는 사용자. 없을 경우 null.
     */
    fun findByMemberId(memberId: Long): User?

    /**
     * 닉네임을 기반으로 사용자를 조회합니다.
     * @param nickname 사용자 닉네임
     * @return 해당 닉네임에 해당하는 사용자. 없을 경우 null.
     */
    fun findByNickname(nickname: String): User?

    /**
     * 특정 닉네임을 가진 사용자가 존재하는지 확인합니다.
     * @param nickname 사용자 닉네임
     * @return 해당 닉네임을 가진 사용자가 존재하면 true, 그렇지 않으면 false
     */
    fun existsByNickname(nickname: String): Boolean

    /**
     * 특정 회원 ID를 가진 사용자가 존재하는지 확인합니다.
     * @param memberId 회원 ID
     * @return 해당 회원 ID를 가진 사용자가 존재하면 true, 그렇지 않으면 false
     */
    fun existsByMemberId(memberId: Long): Boolean

    /**
     * 주어진 리프레시 토큰을 가진 사용자를 조회합니다.
     * @param ourRefreshToken 사용자의 리프레시 토큰
     * @return 해당 리프레시 토큰에 해당하는 사용자. 없을 경우 null.
     */
    fun findByOurRefreshToken(ourRefreshToken: String): User?

    /**
     * 주어진 날짜 이전에 업데이트된 사용자 목록을 조회합니다.
     * @param cutoffDate 업데이트 기준 날짜
     * @return 해당 날짜 이전에 업데이트된 사용자의 목록
     */
    fun findUsersByUpdatedAtBefore(cutoffDate: LocalDateTime): List<User>
}
