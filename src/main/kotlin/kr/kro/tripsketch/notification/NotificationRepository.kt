package kr.kro.tripsketch.notification

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

/**
 * `NotificationRepository`는 알림 관련 데이터 액세스를 위한 리포지토리 인터페이스입니다.
 * MongoDB를 사용하는 경우, Spring Data MongoDB에서 제공하는 `MongoRepository`를 상속받아 사용합니다.
 *
 * `Notification` 엔티티의 CRUD 연산 뿐만 아니라, 수신자 ID를 기반으로 한
 * 알림 조회와 같은 커스텀 쿼리 메서드도 정의되어 있습니다.
 * @author Hojun Song
 */
@Repository
interface NotificationRepository : MongoRepository<Notification, String> {

    /**
     * 수신자 ID를 기반으로 알림을 페이지네이션 형태로 조회합니다.
     * @param receiverId 알림의 수신자 ID
     * @param pageable 페이지네이션 정보를 담은 객체 (예: 페이지 번호, 페이지 크기 등)
     * @return 해당 수신자 ID에 해당하는 알림의 페이지
     */
    fun findByReceiverId(receiverId: String, pageable: Pageable): Page<Notification>
}
