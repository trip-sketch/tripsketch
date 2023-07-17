package kr.kro.tripsketch.controllers

import kr.kro.tripsketch.dto.TripDto
import kr.kro.tripsketch.services.TripService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/trips")
class TripController(private val tripService: TripService) {

    @GetMapping("/{id}")
    fun getTripById(@PathVariable id: String): TripDto? {
        return tripService.getTripById(id)
    }

    @PostMapping
    fun createOrUpdateTrip(@RequestBody tripDto: TripDto): TripDto {
        return tripService.createOrUpdateTrip(tripDto)
    }

    @DeleteMapping("/{id}")
    fun deleteTripById(@PathVariable id: String) {
        return tripService.deleteTripById(id)
    }
}
