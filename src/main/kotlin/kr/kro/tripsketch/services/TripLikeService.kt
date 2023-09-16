package kr.kro.tripsketch.services

import kr.kro.tripsketch.repositories.TripRepository
import kr.kro.tripsketch.repositories.UserRepository
import org.springframework.stereotype.Service

@Service
class TripLikeService(
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val notificationService: NotificationService,
) {
    fun likeTrip(memberId: Long, tripId: String) {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자 ID가 없습니다.")
        val findTrip = tripRepository.findById(tripId).orElse(null)
            ?: throw IllegalArgumentException("조회되는 게시물이 없습니다.")
        if (!findTrip.tripLikes.contains(userId)) {
            findTrip.tripLikes.add(userId)
            findTrip.likes++
            tripRepository.save(findTrip)
            val tripWriterUserId = findTrip.userId
            if (userId != tripWriterUserId) {
                val findUser = userRepository.findById(userId)
                if (findUser.isPresent) {
                    val user = findUser.get()
                    val userNickname = user.nickname
                    val userProfileUrl = user.profileImageUrl ?: ""
                    notificationService.sendPushNotification(
                        listOf(findTrip.userId),
                        "새로운 여행의 시작, 트립스케치",
                        "$userNickname 님이 회원님의 글을 좋아합니다.",
                        null,
                        null,
                        findTrip.id,
                        userNickname,
                        userProfileUrl,
                    )
                } else {
                    throw IllegalArgumentException("조회되는 사용자가 없습니다.")
                }
            }
        } else {
            throw IllegalArgumentException("이미 '좋아요'한 게시물입니다.")
        }
    }

    fun unlikeTrip(memberId: Long, tripId: String) {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자 ID가 없습니다.")
        val findTrip = tripRepository.findById(tripId).orElse(null)
            ?: throw IllegalArgumentException("조회되는 게시물이 없습니다.")
        if (findTrip.tripLikes.contains(userId)) {
            findTrip.tripLikes.remove(userId)
            findTrip.likes--
            tripRepository.save(findTrip)
        } else {
            throw IllegalArgumentException("이미 '좋아요'를 취소한 게시물입니다.")
        }
    }

    fun toggleTripLike(memberId: Long, tripId: String) {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자 ID가 없습니다.")
        val findTrip = tripRepository.findById(tripId).orElse(null)
            ?: throw IllegalArgumentException("조회되는 게시물이 없습니다.")
        if (findTrip.tripLikes.contains(userId)) {
            findTrip.tripLikes.remove(userId)
            findTrip.likes--
        } else {
            findTrip.tripLikes.add(userId)
            findTrip.likes++
            val tripWriterUserId = findTrip.userId
            if (userId != tripWriterUserId) {
                val findUser = userRepository.findById(userId)
                if (findUser.isPresent) {
                    val user = findUser.get()
                    val userNickname = user.nickname
                    val userProfileUrl = user.profileImageUrl ?: ""
                    notificationService.sendPushNotification(
                        listOf(findTrip.userId),
                        "새로운 여행의 시작, 트립스케치",
                        "$userNickname 님이 회원님의 글을 좋아합니다.",
                        null,
                        null,
                        findTrip.id,
                        userNickname,
                        userProfileUrl,
                    )
                } else {
                    throw IllegalArgumentException("조회되는 사용자가 없습니다.")
                }
            }
        }
        tripRepository.save(findTrip)
    }

    fun isTripLiked(memberId: Long, tripId: String): Boolean {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자 ID가 없습니다.")
        val findTrip = tripRepository.findById(tripId).orElse(null)
            ?: throw IllegalArgumentException("조회되는 게시물이 없습니다.")
        return findTrip.tripLikes.contains(userId)
    }
}
