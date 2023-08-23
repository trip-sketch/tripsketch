package kr.kro.tripsketch.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import kr.kro.tripsketch.domain.Trip
import kr.kro.tripsketch.dto.FollowDto
import kr.kro.tripsketch.dto.TripCreateDto
import kr.kro.tripsketch.dto.TripUpdateDto
import kr.kro.tripsketch.dto.TripDto
import kr.kro.tripsketch.services.TripService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kr.kro.tripsketch.utils.TokenUtils
import kr.kro.tripsketch.services.JwtService


@RestController
@RequestMapping("api/trip")
class TripController(private val tripService: TripService, private val jwtService: JwtService) {

    @PostMapping
    fun createTrip(
        req: HttpServletRequest,
        @RequestBody tripCreateDto: TripCreateDto
    ): ResponseEntity<TripDto> {
        val email = req.getAttribute("userEmail") as String
        val createdTrip = tripService.createTrip(email, tripCreateDto)
        return ResponseEntity.ok(createdTrip)
    }
    
    @GetMapping("/admin/trips")
    fun getAllTrips(): ResponseEntity<Set<TripDto>> {
//        val email = req.getAttribute("userEmail") as String
        val findTrips = tripService.getAllTrips()
        return ResponseEntity.ok(findTrips)
    }

    @GetMapping("/nickname")
    fun getTripByNickname(@RequestParam nickname: String): ResponseEntity<Set<TripDto>> {
        val findTrips = tripService.getTripByNickname(nickname)
        return if (findTrips != null) {
            ResponseEntity.ok(findTrips)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{id}")
    fun getTripById(req: HttpServletRequest, @PathVariable id: String): ResponseEntity<TripDto> {
        val email = req.getAttribute("userEmail") as String
        val findTrip = tripService.getTripById(email, id)
        return if (findTrip != null) {
            ResponseEntity.ok(findTrip)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PatchMapping("/{id}")
    fun updateTrip(
        req: HttpServletRequest,
        @PathVariable id: String,
        @RequestBody tripUpdateDto: TripUpdateDto)
    : ResponseEntity<TripDto> {
        val email = req.getAttribute("userEmail") as String
        val existingTrip = tripService.getTripById(email, id)
        if (existingTrip != null) {
            val updatedTrip = tripService.updateTrip(email, tripUpdateDto)
            return ResponseEntity.ok(updatedTrip)
        }
        return ResponseEntity.notFound().build()
    }

    // @PutMapping("/{id}")
    // fun updateTrip(@PathVariable id: ObjectId, @RequestBody trip: Trip): ResponseEntity<Trip> {
    //     val existingTrip = tripService.getTripById(id.toHexString()) // ObjectId를 String으로 변환하여 사용
    //     if (existingTrip != null) {
    //         trip.id = id // 업데이트하려는 객체의 ID를 URL에서 받은 ID로 설정
    //         val updatedTrip = tripService.createOrUpdateTrip(trip)
    //         return ResponseEntity.ok(updatedTrip)
    //     }
    //     return ResponseEntity.notFound().build()
    // }

    @DeleteMapping("/{id}")
    fun deleteTrip(req: HttpServletRequest, @PathVariable id: String): ResponseEntity<Unit> {
        val email = req.getAttribute("userEmail") as String
        val existingTrip = tripService.getTripById(email, id)
        if (existingTrip != null) {
            tripService.deleteTripById(email, id)
            return ResponseEntity.noContent().build()
        }
        return ResponseEntity.notFound().build()
    }
}
