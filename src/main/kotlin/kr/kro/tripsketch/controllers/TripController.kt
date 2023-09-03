package kr.kro.tripsketch.controllers

import jakarta.servlet.http.HttpServletRequest
import kr.kro.tripsketch.dto.TripCreateDto
import kr.kro.tripsketch.dto.TripDto
import kr.kro.tripsketch.dto.TripUpdateDto
import kr.kro.tripsketch.dto.TripUpdateResponseDto
import kr.kro.tripsketch.services.JwtService
import kr.kro.tripsketch.services.TripService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*


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

    // trip 게시글 전체 조회 (isPublic, isHidden 값 상관없이)
    @GetMapping("/admin/trips")
    fun getAllTrips(req: HttpServletRequest): ResponseEntity<Set<TripDto>> {
        val email = req.getAttribute("userEmail") as String
        val findTrips = tripService.getAllTrips(email)
        return ResponseEntity.ok(findTrips)
    }

    @GetMapping("/trips")
    fun getAllTripsByUser(req: HttpServletRequest): ResponseEntity<Set<TripDto>> {
        val email = req.getAttribute("userEmail") as String
        val findTrips = tripService.getAllTripsByUser(email)
        return ResponseEntity.ok(findTrips)
    }

    @GetMapping("/guest/trips")
    fun getAllTripsByGuest(): ResponseEntity<Any> {
        return try {
            val findTrips = tripService.getAllTripsByGuest()
            if (findTrips.isNotEmpty()) {
                ResponseEntity.ok(findTrips)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (ex: IllegalAccessException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body("조회할 권한이 없습니다.")
        }
    }

    @GetMapping("/nickname")
    fun getTripByNickname(@RequestParam nickname: String): ResponseEntity<Set<TripDto>> {
        val findTrips = tripService.getTripByNickname(nickname)
        return ResponseEntity.ok(findTrips)
    }

    // 해당 nickname 트립을 가져와서 여행 목록을 나라 기준으로 카테고리화하여 반환하는 엔드포인트
    @GetMapping("/nickname/trips/categories")
    fun getTripsCategorizedByCountry(@RequestParam("nickname") nickname: String): ResponseEntity<Pair<Map<String, Int>, Set<TripDto>>> {
        val sortedCountryFrequencyMap = tripService.getTripCategoryByNickname(nickname)
        return ResponseEntity.ok(sortedCountryFrequencyMap)
    }

    @GetMapping("/nickname/tripsWithPagination/categories")
    fun getTripsCategorizedByCountryWithPagination(
        @RequestParam("nickname") nickname: String,
        @RequestParam("page", required = false, defaultValue = "1") page: Int,
        @RequestParam("pageSize", required = false, defaultValue = "10") pageSize: Int
    ): ResponseEntity<Map<String, Any>> {
        val sortedCountryFrequencyMap = tripService.getTripCategoryByNickname(nickname, page, pageSize)
        return ResponseEntity.ok(sortedCountryFrequencyMap)
    }

    // 해당 nickname 트립을 가져와서 특정 나라의 여행 목록을 반환하는 엔드포인트
    @GetMapping("/nickname/trips/country/{country}")
    fun getTripsInCountry(
        @RequestParam("nickname") nickname: String,
        @PathVariable("country") country: String
    ): ResponseEntity<Set<TripDto>> {
        val sortedCountryFrequencyMap = tripService.getTripsInCountry(nickname, country)
        return ResponseEntity.ok(sortedCountryFrequencyMap)
    }

    @GetMapping("/nickname/tripsWithPagination/country/{country}")
    fun getTripsInCountryWithPagination(
        @RequestParam("nickname") nickname: String,
        @PathVariable("country") country: String,
        @RequestParam("page", required = false, defaultValue = "1") page: Int,
        @RequestParam("pageSize", required = false, defaultValue = "10") pageSize: Int
    ): ResponseEntity<Map<String, Any>> {
        val sortedCountryFrequencyMap = tripService.getTripsInCountry(nickname, country, page, pageSize)
        return ResponseEntity.ok(sortedCountryFrequencyMap)
    }

    // 해당 nickname 트립을 가져와서 나라별 여행 횟수를 많은 순으로 정렬하여 반환하는 엔드포인트
    @GetMapping("/nickname/trips/country-frequencies")
    fun getCountryFrequencies(@RequestParam("nickname") nickname: String): ResponseEntity<Map<String, Int>> {
        val countryFrequencyMap = tripService.getCountryFrequencies(nickname)
        return ResponseEntity.ok(countryFrequencyMap)
    }

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

    @GetMapping("modify/{id}")
    fun getTripByEmailAndIdToUpdate(
        req: HttpServletRequest,
        @PathVariable id: String
    ): ResponseEntity<TripUpdateResponseDto> {
        val email = req.getAttribute("userEmail") as String
        val findTrip = tripService.getTripByEmailAndIdToUpdate(email, id)
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
            if (!findTrip.isHidden) {
                ResponseEntity.ok(findTrip)
            } else {
                ResponseEntity.notFound().build()
            }
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PatchMapping("/{id}")
    fun updateTrip(
        req: HttpServletRequest,
        @PathVariable id: String,
        @Validated @RequestBody tripUpdateDto: TripUpdateDto
    )
            : ResponseEntity<Any> {
        return try {
            val email = req.getAttribute("userEmail") as String
            val findTrip = tripService.getTripById(id)
            if (findTrip != null) {
                val updatedTrip = tripService.updateTrip(email, tripUpdateDto)
//                ResponseEntity.ok(updatedTrip)
                ResponseEntity.status(HttpStatus.OK).body(updatedTrip, "게시물이 수정되었습니다.")
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (ex: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        } catch (ex: IllegalAccessException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정할 권한이 없습니다.")
        }
    }

    @DeleteMapping("/{id}")
    fun deleteTrip(req: HttpServletRequest, @PathVariable id: String): ResponseEntity<Any> {
        return try {
            val email = req.getAttribute("userEmail") as String
            val findTrip = tripService.getTripById(id)
            if (findTrip != null) {
                tripService.deleteTripById(email, id)
                ResponseEntity.ok("게시물이 삭제되었습니다.")
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (ex: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        } catch (ex: IllegalAccessException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제할 권한이 없습니다.")
        }
    }

}

private fun ResponseEntity.BodyBuilder.body(returnedTripDto: TripDto, message: String): ResponseEntity<Any> {
    val responseBody = mapOf(
        "message" to message,
        "trip" to returnedTripDto
    )
    return this.body(responseBody)
}
