package kr.kro.tripsketch.controllers

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
    @PostMapping
    fun createTrip(
        @RequestHeader("Authorization") token: String,
        @RequestBody tripCreateDto: TripCreateDto
    ): ResponseEntity<TripDto> {
        val actualToken = TokenUtils.validateAndExtractToken(jwtService, token)
        val createdTrip = tripService.createTrip(actualToken, tripCreateDto)
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

// @RestController
// @RequestMapping
// class TripController(private val tripService: TripService) {

//     @GetMapping("/trips")
//     fun getAllTrips(): ResponseEntity<List<Trip>> {
//         val trips = tripService.getAllTrips()
//         return ResponseEntity.ok(trips)
//     }

//     @GetMapping("/trips/{id}")
//     fun getTripById(@PathVariable id: String): TripDto? {
//         val trip = tripService.findById(id)
//         return trip?.let { mapToTripDto(it) }
//     }

//     // Helper function to map a Trip object to a TripDto object
//     private fun mapToTripDto(trip: Trip): TripDto {
//         return TripDto(
//             id = trip.id,
//             userId = trip.userId,
//             scheduleId = trip.scheduleId,
//             title = trip.title,
//             content = trip.content,
//             likes = trip.likes,
//             views = trip.views,
//             location = trip.location,
//             startedAt = trip.startedAt,
//             endAt = trip.endAt,
//             hashtag = trip.hashtag,
//             hidden = trip.hidden,
//             createdAt = trip.createdAt,
//             updatedAt = trip.updatedAt,
//             deletedAt = trip.deletedAt,
//             likeFlag = trip.likeFlag,
//             tripViews = trip.tripViews,
//         )
//     }

//     @PostMapping("/trips")
//     fun createOrUpdateTrip(@RequestBody tripDto: TripDto): TripDto {
//         val trip = tripService.createOrUpdateTrip(tripDto)
//         return mapToTripDto(trip)
//     }

//     // @PostMapping
//     // fun createOrUpdateTrip(@RequestBody tripDto: TripDto): TripDto {
//     //     return tripService.createOrUpdateTrip(tripDto)
//     // }

//     // @DeleteMapping("/trip/{id}")
//     // fun deleteTripById(@PathVariable id: String) {
//     //     return tripService.deleteTripById(id)
//     // }

//     @DeleteMapping("/trips/{id}")
//     fun deleteTripById(@PathVariable id: String): ResponseEntity<Unit> {
//         // tripService.deleteTripById(id)
//         // return ResponseEntity.noContent().build()

//         // String 타입으로 전달받은 id를 ObjectId로 변환하여 사용
//         val objectId = ObjectId(id)
//         tripService.deleteTripById(objectId)
//         return ResponseEntity.noContent().build()
//     }
// }