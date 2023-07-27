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
        val createdTrip = tripService.createTrip(trip)
        return ResponseEntity.ok(createdTrip)
    }
    
    @PatchMapping("/{id}")            
    fun updateTrip(@PathVariable id: String, @RequestBody trip: Trip): ResponseEntity<Trip> {
        val updatedTrip = tripService.updateTrip(id, trip) // id를 String 타입으로 전달
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
