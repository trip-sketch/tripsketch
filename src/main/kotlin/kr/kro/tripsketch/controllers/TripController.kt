package kr.kro.tripsketch.controllers

import kr.kro.tripsketch.domain.Trip
import kr.kro.tripsketch.services.TripService
import org.bson.types.ObjectId // ObjectId import
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/trips")
class TripController(private val tripService: TripService) {

    @GetMapping
    fun getAllTrips(): ResponseEntity<List<Trip>> {
        val trips = tripService.getAllTrips()
        return ResponseEntity.ok(trips)
    }

    @GetMapping("/{id}")
    fun getTripById(@PathVariable id: String): ResponseEntity<Trip> {
        val trip = tripService.getTripById(id)
        return if (trip != null) {
            ResponseEntity.ok(trip)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createTrip(@RequestBody trip: Trip): ResponseEntity<Trip> {
        val createdTrip = tripService.createOrUpdateTrip(trip)
        return ResponseEntity.ok(createdTrip)
    }

    @PutMapping("/{id}")
    fun updateTrip(@PathVariable id: ObjectId, @RequestBody trip: Trip): ResponseEntity<Trip> {
        val existingTrip = tripService.getTripById(id.toHexString()) // ObjectId를 String으로 변환하여 사용
        if (existingTrip != null) {
            trip.id = id // 업데이트하려는 객체의 ID를 URL에서 받은 ID로 설정
            val updatedTrip = tripService.createOrUpdateTrip(trip)
            return ResponseEntity.ok(updatedTrip)
        }
        return ResponseEntity.notFound().build()
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
