package kr.kro.tripsketch.controllers

import kr.kro.tripsketch.domain.Trip
import kr.kro.tripsketch.dto.TripDto
import kr.kro.tripsketch.services.TripService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping
class TripController(private val tripService: TripService) {

    @GetMapping("/trip")
    fun getAllTrips(): ResponseEntity<List<Trip>> {
        val trips = tripService.getAllTrips()
        return ResponseEntity.ok(trips)
    }

    @GetMapping("/trip/{id}")
    fun getTripById(@PathVariable id: String): TripDto? {
        val trip = tripService.findById(id)
        return trip?.let { mapToTripDto(it) }
    }

    // Helper function to map a Trip object to a TripDto object
    private fun mapToTripDto(trip: Trip): TripDto {
        return TripDto(
            id = trip.id,
            userId = trip.userId,
            scheduleId = trip.scheduleId,
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
            likeFlag = trip.likeFlag,
            tripViews = trip.tripViews,
        )
    }

    @PostMapping("/trip")
    fun createTrip(@RequestBody tripDto: TripDto): TripDto {
        val trip = tripService.createTrip(tripDto)
        return mapToTripDto(trip)
    }

    // @PostMapping
    // fun createOrUpdateTrip(@RequestBody tripDto: TripDto): TripDto {
    //     return tripService.createOrUpdateTrip(tripDto)
    // }

    // @DeleteMapping("/trip/{id}")
    // fun deleteTripById(@PathVariable id: String) {
    //     return tripService.deleteTripById(id)
    // }

}
