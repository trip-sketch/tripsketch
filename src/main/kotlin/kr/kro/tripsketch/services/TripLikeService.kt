package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.Trip
import kr.kro.tripsketch.dto.TripDto
import kr.kro.tripsketch.dto.TripCreateDto
import kr.kro.tripsketch.dto.TripUpdateDto
import kr.kro.tripsketch.dto.TripIdAndEmailDto
import kr.kro.tripsketch.repositories.TripRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TripLikeService(private val tripRepository: TripRepository) {

//    fun likeTrip(email: String, id: String) {
    fun likeTrip(tripIdAndEmailDto: TripIdAndEmailDto) {
        val trip = tripRepository.findById(tripIdAndEmailDto.id).orElseThrow {
            EntityNotFoundException("조회되는 게시물이 없습니다.")
        }
        if (!trip.tripLikes.contains(tripIdAndEmailDto.email)) {
            trip.tripLikes.add(tripIdAndEmailDto.email)
            trip.likes++
            tripRepository.save(trip)
        } else {
            throw IllegalStateException("Trip is already liked by this user")
        }
    }

//    fun unlikeTrip(email: String, id: String) {
    fun unlikeTrip(tripIdAndEmailDto: TripIdAndEmailDto)  {
        val trip = tripRepository.findById(tripIdAndEmailDto.id).orElseThrow {
            EntityNotFoundException("조회되는 게시물이 없습니다.")
        }
        if (trip.tripLikes.contains(tripIdAndEmailDto.email)) {
            trip.tripLikes.remove(tripIdAndEmailDto.email)
            trip.likes--
            tripRepository.save(trip)
        } else {
            throw IllegalStateException("This user has not liked the trip")
        }
    }
}

class EntityNotFoundException(message: String) : RuntimeException(message)