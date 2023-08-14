package kr.kro.tripsketch.controllers

import kr.kro.tripsketch.domain.Trip
import kr.kro.tripsketch.services.TripService
import org.bson.types.ObjectId // ObjectId import
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kr.kro.tripsketch.utils.TokenUtils
import kr.kro.tripsketch.services.JwtService

@RestController
@RequestMapping("api/trips")
class TripController(private val tripService: TripService, private val jwtService: JwtService) {

    /** trip 전체 조회 */
    @GetMapping
    fun getAllTrips(): ResponseEntity<List<Trip>> {
        val trips = tripService.getAllTrips()
        return ResponseEntity.ok(trips)
    }

    // to-do: trip 게시물내용, 이미지, like, comment 다 같이 불러오기
    // to-do: trip 게시물내용, 이미지, like, comment 별도로 불러오기

    /** trip id 조회 */
    @GetMapping("/{id}")
    fun getTripById(@PathVariable id: String): ResponseEntity<Trip> {
        val trip = tripService.getTripById(id)
        return if (trip != null) {
            ResponseEntity.ok(trip)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /** trip 신규 생성 */
    @PostMapping
    fun createTrip(@RequestHeader("Authorization") token: String, @RequestBody trip: Trip): ResponseEntity<Trip> {
        val actualToken = TokenUtils.validateAndExtractToken(jwtService, token)
        val createdTrip = tripService.createTrip(trip)
        return ResponseEntity.ok(createdTrip)
//        return TripService.createTrip(actualToken,createdTrip)
    }

    /** trip 수정 */
    @PatchMapping("/{id}")            
    fun updateTripById(@RequestHeader("Authorization") token: String, @PathVariable id: String, @RequestBody trip: Trip): ResponseEntity<Trip> {
        val actualToken = TokenUtils.validateAndExtractToken(jwtService, token)

        val updatedTrip = tripService.updateTripById(actualToken, id, trip) // id를 String 타입으로 전달
//        return ResponseEntity.ok(actualToken, updatedTrip)
        return ResponseEntity.ok(updatedTrip)
    }

    // @DeleteMapping("/{id}")
    // fun deleteHardTrip(@PathVariable id: String): ResponseEntity<Unit> {
    //     val existingTrip = tripService.getTripById(id)
    //     if (existingTrip != null) {
    //         tripService.deleteHardTripById(id)
    //         return ResponseEntity.noContent().build()
    //     }
    //     return ResponseEntity.notFound().build()
    // }

    /** trip 삭제 */
    @DeleteMapping("/{id}")
    fun deleteTripById(@RequestHeader("Authorization") token: String, @PathVariable id: String): ResponseEntity<Unit> {
        val actualToken = TokenUtils.validateAndExtractToken(jwtService, token)
        val existingTrip = tripService.deleteTripById(actualToken, id)
        if (existingTrip != null) {
            tripService.deleteTripById(id)
            return ResponseEntity.noContent().build()
        }
        return ResponseEntity.notFound().build()
    }
}
