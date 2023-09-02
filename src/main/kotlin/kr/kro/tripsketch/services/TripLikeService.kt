package kr.kro.tripsketch.services

import kr.kro.tripsketch.repositories.TripRepository
import org.springframework.stereotype.Service

@Service
class TripLikeService(
    private val tripRepository: TripRepository,
    private val notificationService: NotificationService
) {
    fun likeTrip(email: String, tripId: String) {
        val findTrip = tripRepository.findById(tripId).orElse(null)
            ?: throw IllegalArgumentException("조회되는 게시물이 없습니다.")
        println(findTrip)
        if (!findTrip.tripLikes.contains(email)) {
            findTrip.tripLikes.add(email)
            findTrip.likes++
            tripRepository.save(findTrip)
        } else {
            throw IllegalStateException("이미 좋아요한 게시물입니다.")
        }
    }

    fun unlikeTrip(email: String, tripId: String)  {
        val findTrip = tripRepository.findById(tripId).orElseThrow {
            EntityNotFoundException("조회되는 게시물이 없습니다.")
        }
        println(findTrip)
        if (findTrip.tripLikes.contains(email)) {
            findTrip.tripLikes.remove(email)
            findTrip.likes--
            tripRepository.save(findTrip)
        } else {
            throw IllegalStateException("이미 좋아요 취소한 게시물입니다.")
        }
    }
}

class EntityNotFoundException(message: String) : RuntimeException(message)