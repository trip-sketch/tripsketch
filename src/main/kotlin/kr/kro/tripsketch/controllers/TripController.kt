package kr.kro.tripsketch.controllers

import jakarta.servlet.http.HttpServletRequest
import kr.kro.tripsketch.dto.TripCreateDto
import kr.kro.tripsketch.dto.TripUpdateDto
import kr.kro.tripsketch.dto.TripDto
import kr.kro.tripsketch.services.TripService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
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
    fun getAllTrips(req: HttpServletRequest): ResponseEntity<Set<TripDto>> {
        val findTrips = tripService.getAllTrips()
        return ResponseEntity.ok(findTrips)
    }

    @GetMapping("/nickname")
    fun getTripByNickname(@RequestParam nickname: String): ResponseEntity<Set<TripDto>> {
        val findTrips = tripService.getTripByNickname(nickname)
        return ResponseEntity.ok(findTrips)
    }

//    @GetMapping("/nickname")
//    fun getTripByNickname(
//        @RequestParam nickname: String,
//        pageable: Pageable
//    ): ResponseEntity<Page<TripDto>> {
////        val findTrips = tripService.getTripByNickname(nickname)
////        return ResponseEntity.ok(findTrips)
//
//        val findTrips = tripService.getTripByNickname(nickname, pageable)
//        println(findTrips)
//        return ResponseEntity.ok(findTrips)
//    }

    @GetMapping("/{id}")
    fun getTripByEmailAndId(req: HttpServletRequest, @PathVariable id: String): ResponseEntity<TripDto> {
        val email = req.getAttribute("userEmail") as String
        val findTrip = tripService.getTripByEmailAndId(email, id)
        return if (findTrip != null) {
            ResponseEntity.ok(findTrip)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/guest/{id}")
    fun getTripById(@PathVariable id: String): ResponseEntity<TripDto> {
        val findTrip = tripService.getTripById(id)
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
        val findTrip = tripService.getTripById(id)
        if (findTrip != null) {
            val updatedTrip = tripService.updateTrip(email, tripUpdateDto)
            return ResponseEntity.ok(updatedTrip)
        }
        return ResponseEntity.notFound().build()
    }

    @DeleteMapping("/{id}")
    fun deleteTrip(req: HttpServletRequest, @PathVariable id: String): ResponseEntity<String> {
        val email = req.getAttribute("userEmail") as String
        val findTrip = tripService.getTripById(id)
        if (findTrip != null) {
            tripService.deleteTripById(email, id)
//            return ResponseEntity.noContent().build()
            return ResponseEntity.ok("게시물이 삭제되었습니다")
        }
        return ResponseEntity.notFound().build()
    }

}
