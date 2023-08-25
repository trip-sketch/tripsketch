package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.Trip
import kr.kro.tripsketch.dto.TripDto
import kr.kro.tripsketch.dto.TripCreateDto
import kr.kro.tripsketch.dto.TripUpdateDto
import kr.kro.tripsketch.repositories.TripRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

class TripLikeService(private val tripRepository: TripRepository) {

    fun likeTrip(email: String, id: String) {
        val trip = tripRepository.findById(id).orElseThrow {
            EntityNotFoundException("조회되는 게시물이 없습니다.")
        }

        if (!trip.tripLikes.contains(email)) {
            trip.tripLikes.add(email)
            trip.likes++
            tripRepository.save(trip)
        } else {
            throw IllegalStateException("Trip is already liked by this user")
        }
    }

    fun unlikeTrip(email: String, id: String) {
        val trip = tripRepository.findById(id).orElseThrow {
            EntityNotFoundException("조회되는 게시물이 없습니다.")
        }

        if (trip.tripLikes.contains(email)) {
            trip.tripLikes.remove(email)
            trip.likes--
            tripRepository.save(trip)
        }
    }

}

class EntityNotFoundException(message: String) : RuntimeException(message)