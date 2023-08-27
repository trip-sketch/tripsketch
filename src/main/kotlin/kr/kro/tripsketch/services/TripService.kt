package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.Comment
import kr.kro.tripsketch.domain.Trip
import kr.kro.tripsketch.dto.CommentDto
import kr.kro.tripsketch.dto.TripDto
import kr.kro.tripsketch.dto.TripCreateDto
import kr.kro.tripsketch.dto.TripUpdateDto
import kr.kro.tripsketch.repositories.TripRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import kr.kro.tripsketch.domain.User
import kr.kro.tripsketch.dto.UserDto
import kr.kro.tripsketch.repositories.UserRepository
import java.time.LocalDateTime

@Service
class TripService(private val tripRepository: TripRepository, private val jwtService: JwtService, private val userService: UserService) {

    fun createTrip(email: String, tripCreateDto: TripCreateDto): TripDto {

        val newTrip = Trip( 
            email = email,
            title = tripCreateDto.title,
            content = tripCreateDto.content,
            location = tripCreateDto.location,
            startedAt = LocalDateTime.now(),
            endAt = LocalDateTime.now(),
            hashtag = tripCreateDto.hashtag,
            images = tripCreateDto.images
        )

        val createdTrip = tripRepository.save(newTrip)
        return fromTrip(createdTrip, false)
    }


//    fun getAllTrips(): Set<TripDto> {
//        val findTrips = tripRepository.findAll()
//        return findTrips.map { fromTrip(it, false) }.toSet()
//    }


    fun getAllTrips(): Set<TripDto> {
        val findTrips = tripRepository.findAll()
        println(findTrips)
        return findTrips.map { fromTrip(it, false) }.toSet()
    }


    fun getTripByNickname(nickname: String): Set<TripDto> {
        val user = userService.findUserByNickname(nickname)
        val findTrips = tripRepository.findTripByEmail(user!!.email)
//            ?: throw IllegalArgumentException("작성한 게시글이 존재하지 않습니다.")
        return findTrips.map { fromTrip(it, false) }.toSet()
    }

//    fun getTripByNickname(nickname: String, pageable: Pageable): Page<TripDto> {
//        val user = userService.findUserByNickname(nickname)
//        val findTrips = tripRepository.findTripByEmail(user!!.email, pageable)
////        return findTrips.map { fromTrip(it, false) }.toSet()
//        return findTrips.map { fromTrip(it, false) }.let { PageImpl(it.toList(), pageable, findTrips.totalElements) }
//
//    }

    fun getTripByEmailAndId(email: String, id: String): TripDto? {
        val findTrip = tripRepository.findById(id).orElse(null)
        ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")

        // 조회수
        if (!findTrip.tripViews.contains(email) && findTrip.email != email ) {
            findTrip.tripViews.add(email)
            findTrip.views += 1
        }

        // to-do: likes
        if (findTrip.tripLikes.contains(email)) {
            // 좋아요 상태 표시
            println("좋아요 상태표시")
        }

        tripRepository.save(findTrip)
        return fromTrip(findTrip, false)
    }


    fun getTripById(id: String): TripDto? {
        val findTrip = tripRepository.findById(id).orElse(null)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        return fromTrip(findTrip, false)
    }


    fun updateTrip(email: String, tripUpdateDto: TripUpdateDto): TripDto {

        val updateTrip = Trip(
            email = email,
            title = tripUpdateDto.title,
            content = tripUpdateDto.content,
            location = tripUpdateDto.location,
            startedAt = LocalDateTime.now(),
            endAt = LocalDateTime.now(),
            hashtag = tripUpdateDto.hashtag,
            updatedAt = LocalDateTime.now(),
            images = tripUpdateDto.images
        )

        val updatedTrip = tripRepository.save(updateTrip)
        return fromTrip(updatedTrip, false)
    }


    fun deleteTripById(email: String, id: String) {
        tripRepository.deleteById(id)
    }


    fun toTrip(tripDto: TripDto): Trip {
        return Trip(
            id = tripDto.id,
            email = tripDto.email!!,
            title = tripDto.title,
            content = tripDto.content,
            likes = tripDto.likes!!,
            views = tripDto.views!!,
            location = tripDto.location,
            startedAt = tripDto.startedAt,
            endAt = tripDto.endAt,
            hashtag = tripDto.hashtag,
            hidden = tripDto.hidden,
            createdAt = tripDto.createdAt,
            updatedAt = tripDto.updatedAt,
            deletedAt = tripDto.deletedAt,
            tripLikes = tripDto.tripLikes,
//            tripViews = tripDto.tripViews,
            images = tripDto.images
        )
    }

    fun fromTrip(trip: Trip, includeEmail: Boolean = true): TripDto {

        val user = userService.findUserByEmail(trip.email)
        val isLiked = trip.tripLikes.contains(trip.email)  // 사용자의 email 정보를 바로 사용

        return if (includeEmail) {
            TripDto(
                id = trip.id,
                email = trip.email,
                nickname = user!!.nickname,
                title = trip.title,
                content = trip.content,
                likes = trip.likes,
                views = trip.views,
                location = trip.location,
                startedAt = trip.startedAt,
                endAt = trip.endAt,
                hashtag = trip.hashtag,
                hidden = trip.hidden,
                createdAt = trip.createdAt,
                updatedAt = trip.updatedAt,
                deletedAt = trip.deletedAt,
                tripLikes = trip.tripLikes,
//                tripViews = trip.tripViews,
                isLiked = isLiked,
                images = trip.images
            )

        } else {
            TripDto(
                id = trip.id,
                email = null,
                nickname = user!!.nickname,
                title = trip.title,
                content = trip.content,
                likes = trip.likes,
                views = trip.views,
                location = trip.location,
                startedAt = trip.startedAt,
                endAt = trip.endAt,
                hashtag = trip.hashtag,
                hidden = trip.hidden,
                createdAt = trip.createdAt,
                updatedAt = trip.updatedAt,
                deletedAt = trip.deletedAt,
                tripLikes = trip.tripLikes,
//                tripViews = trip.tripViews,
                isLiked = isLiked,
                images = trip.images
            )
        }
    }

}
