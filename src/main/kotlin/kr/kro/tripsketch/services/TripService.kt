package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.Comment
import kr.kro.tripsketch.domain.Trip
import kr.kro.tripsketch.dto.CommentDto
import kr.kro.tripsketch.dto.TripDto
import kr.kro.tripsketch.dto.TripCreateDto
import kr.kro.tripsketch.dto.TripUpdateDto
import kr.kro.tripsketch.repositories.TripRepository
import org.springframework.stereotype.Service
import kr.kro.tripsketch.domain.User
import kr.kro.tripsketch.dto.UserDto
import kr.kro.tripsketch.repositories.UserRepository
import java.time.LocalDateTime

@Service
class TripService(private val tripRepository: TripRepository, private val jwtService: JwtService, private val userService: UserService) {

    fun createTrip(email: String, tripCreateDto: TripCreateDto): TripDto {
        val user = userService.findUserByEmail(email) 

        val newTrip = Trip( 
            userEmail = email,
            nickname = user!!.nickname,
            title = tripCreateDto.title,
            content = tripCreateDto.content,
            // likes = 0,
            // views = 0,  
            location = tripCreateDto.location,
            startedAt = LocalDateTime.now(),
            endAt = LocalDateTime.now(),
            hashtag = tripCreateDto.hashtag,
        )
        
        // val tripEntity = toTrip(newTrip)
        // val createdTripEntity = tripRepository.save(tripEntity)
        // return fromTrip(createdTripEntity)

        val createdTrip = tripRepository.save(newTrip)
        return fromTrip(createdTrip)
    }

    fun getAllTrips(userEmail: String): Set<TripDto> {
        val findTrips = tripRepository.findAll()
        return findTrips.map { fromTrip(it) }.toSet()
    }

    fun getTripById(userEmail: String, id: String): TripDto? {  // 자신이 작성한 trip id 만 조회 가능한지? 아니면 로그인상태면, 트립 id 로 조회가 가능한지?
        val findTrip = tripRepository.findById(id).orElse(null)
        return fromTrip(findTrip)
    }

    fun updateTrip(email: String, tripUpdateDto: TripUpdateDto): TripDto {
        val user = userService.findUserByEmail(email) 

        val updateTrip = Trip(
            userEmail = email,
            nickname = user!!.nickname,
            title = tripUpdateDto.title,
            content = tripUpdateDto.content,
            location = tripUpdateDto.location,
            startedAt = LocalDateTime.now(),
            endAt = LocalDateTime.now(),
            hashtag = tripUpdateDto.hashtag,        // DB 쪽에서 기존 데이터에  플러스 되어야하는거라면?
            updatedAt = LocalDateTime.now(),
        )

        val updatedTrip = tripRepository.save(updateTrip)
        return fromTrip(updatedTrip)
    }

    fun deleteTripById(userEmail: String, id: String) {
        tripRepository.deleteById(id)
    }
}

fun toTrip(tripDto: TripDto): Trip {
    return Trip(
        id = tripDto.id,
        userEmail = tripDto.userEmail,
        nickname = tripDto.nickname,
        title = tripDto.title,
        content = tripDto.content,
        likes = tripDto.likes,
        views = tripDto.views,
        location = tripDto.location,
        startedAt = tripDto.startedAt,
        endAt = tripDto.endAt,
        hashtag = tripDto.hashtag,
        hidden = tripDto.hidden,
        createdAt = tripDto.createdAt,
        updatedAt = tripDto.updatedAt,
        deletedAt = tripDto.deletedAt,
        tripViews = tripDto.tripViews
    )
}

fun fromTrip(trip: Trip): TripDto {
    return TripDto(
        id = trip.id,
        userEmail = trip.userEmail,
        nickname = trip.nickname,
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
        tripViews = trip.tripViews
    )
}