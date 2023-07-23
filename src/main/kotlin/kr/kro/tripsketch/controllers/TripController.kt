package kr.kro.tripsketch.controllers

import kr.kro.tripsketch.domain.Trip
import kr.kro.tripsketch.dto.TripDto
import org.bson.types.ObjectId  // ObjectId import
import kr.kro.tripsketch.services.TripService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping
class TripController(private val tripService: TripService) {

    @GetMapping("/trips")
    fun getAllTrips(): ResponseEntity<List<Trip>> {
        val trips = tripService.getAllTrips()
        return ResponseEntity.ok(trips)
    }

    @GetMapping("/trips/{id}")
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

    @PostMapping("/trips")
    fun createOrUpdateTrip(@RequestBody tripDto: TripDto): TripDto {
        val trip = tripService.createOrUpdateTrip(tripDto)
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

    @DeleteMapping("/trips/{id}")
    fun deleteTripById(@PathVariable id: String): ResponseEntity<Unit> {
        // tripService.deleteTripById(id)
        // return ResponseEntity.noContent().build()

        // String 타입으로 전달받은 id를 ObjectId로 변환하여 사용
        val objectId = ObjectId(id)
        tripService.deleteTripById(objectId)
        return ResponseEntity.noContent().build()
    }
}
