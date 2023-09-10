package kr.kro.tripsketch.repositories

import kr.kro.tripsketch.domain.Notification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : MongoRepository<Notification, String> {
    fun findByReceiverIdsContains(receiverId: String, pageable: Pageable): Page<Notification>
}
