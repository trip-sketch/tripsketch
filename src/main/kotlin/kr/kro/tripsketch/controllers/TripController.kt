package kr.kro.tripsketch.controllers

import jakarta.servlet.http.HttpServletRequest
import kr.kro.tripsketch.domain.Trip
import kr.kro.tripsketch.dto.TripCreateDto
import kr.kro.tripsketch.dto.TripUpdateDto
import kr.kro.tripsketch.dto.TripDto
import org.bson.types.ObjectId  // ObjectId import
import kr.kro.tripsketch.services.TripService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kr.kro.tripsketch.utils.TokenUtils
import kr.kro.tripsketch.services.JwtService


@RestController
@RequestMapping("api/trips")
class TripController(private val tripService: TripService, private val jwtService: JwtService) {

    @TokenValidation
    @PostMapping
    fun createTrip(
        req: HttpServletRequest,
        @RequestBody tripCreateDto: TripCreateDto
    ): ResponseEntity<TripDto> {

        val userEmail = req.getAttribute("userEmail") as String
        val createdTrip = tripService.createTrip(email, tripCreateDto)

        return ResponseEntity.ok(createdTrip)
    }

    @GetMapping
    fun getAllTrips(@RequestHeader("Authorization") token: String): ResponseEntity<Set<TripDto>> {
        val actualToken = TokenUtils.validateAndExtractToken(jwtService, token)
        val findTrips = tripService.getAllTrips(actualToken)
        return ResponseEntity.ok(findTrips)
    }

    @GetMapping("/{id}")
    fun getTripById(@PathVariable id: String): ResponseEntity<TripDto> {
        val findTrip = tripService.getTripById(id)
        if (findTrip != null) {
            return ResponseEntity.ok(findTrip)
        } else {
            return ResponseEntity.notFound().build()
        }
    }

    @PatchMapping("/{id}")
    fun updateTrip(
        @RequestHeader("Authorization") token: String,
        @PathVariable id: String,
        @RequestBody tripUpdateDto: TripUpdateDto)
    : ResponseEntity<TripDto> {
        val existingTrip = tripService.getTripById(id)
        if (existingTrip != null) {
//            trip.id = ObjectId(id) // String을 ObjectId로 변환하여 사용
            val actualToken = TokenUtils.validateAndExtractToken(jwtService, token)
            val updatedTrip = tripService.updateTrip(actualToken, tripUpdateDto)
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
    fun deleteTrip(@PathVariable id: String): ResponseEntity<Unit> {
        val existingTrip = tripService.getTripById(id)
        if (existingTrip != null) {
            tripService.deleteTripById(id)
            return ResponseEntity.noContent().build()
        }
        return ResponseEntity.notFound().build()
    }
}
