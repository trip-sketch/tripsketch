package kr.kro.tripsketch.controllers

import jakarta.servlet.http.HttpServletRequest
import kr.kro.tripsketch.dto.*
import kr.kro.tripsketch.exceptions.BadRequestException
import kr.kro.tripsketch.exceptions.DataNotFoundException
import kr.kro.tripsketch.exceptions.ForbiddenException
import kr.kro.tripsketch.services.TripService
import kr.kro.tripsketch.utils.PagenationUtil
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/trip")
class TripController(private val tripService: TripService) {

    @PostMapping(consumes = ["multipart/form-data"])
    fun createTrip(
        req: HttpServletRequest,
        @Validated @ModelAttribute tripCreateDto: TripCreateDto,
    ): ResponseEntity<TripDto> {
        try {
            val memberId = req.getAttribute("memberId") as Long
            val createdTrip = tripService.createTrip(memberId, tripCreateDto, tripCreateDto.images)
            return ResponseEntity.ok(createdTrip)
        } catch (e: IllegalArgumentException) {
            throw BadRequestException("요청이 잘못되었습니다. ${e.message}")
        }
    }

//    @GetMapping("/admin/trips")
//    fun getAllTrips(
//        req: HttpServletRequest,
//        @RequestParam("page", required = false, defaultValue = "1") page: Int,
//        @RequestParam("size", required = false, defaultValue = "10") size: Int,
//    ): ResponseEntity<Map<String, Any>> {
//        return try {
//            val memberId = req.getAttribute("memberId") as Long
//            val pageable: Pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending())
//            val findTrips = tripService.getAllTrips(memberId, pageable)
//            ResponseEntity.ok(findTrips)
//        } catch (e: IllegalArgumentException) {
//            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to (e.message ?: "")))
//        } catch (e: DataNotFoundException) {
//            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("message" to e.message))
//        }
//    }

    @GetMapping("/admin/trips")
    fun getAllTrips(
        req: HttpServletRequest,
        @RequestParam("page", required = false, defaultValue = "1") page: Int,
        @RequestParam("size", required = false, defaultValue = "10") size: Int,
        @RequestParam("sortType", required = false, defaultValue = "1") sortType: Int
    ): ResponseEntity<Any> {
        return try {
            val memberId = req.getAttribute("memberId") as Long
            val sort = when (sortType) {
                1 -> Sort.by(Sort.Direction.DESC, "createdAt")
                -1 -> Sort.by(Sort.Direction.ASC, "createdAt")
                2 -> Sort.by(
                    Sort.Order(Sort.Direction.DESC, "views"),
                    Sort.Order(Sort.Direction.DESC, "createdAt")
                )
                -2 -> Sort.by(
                    Sort.Order(Sort.Direction.ASC, "views"),
                    Sort.Order(Sort.Direction.DESC, "createdAt")
                )
                else -> throw IllegalArgumentException("Invalid sort type")
            }
            val pageable: Pageable = PageRequest.of(page - 1, size, sort)
            val findTrips = tripService.getAllTrips(memberId, pageable)
//            if (findTrips.isNotEmpty()) {
//                ResponseEntity.status(HttpStatus.OK).body(findTrips)
//            } else {
//                ResponseEntity.status(HttpStatus.OK).body(mapOf("message" to "조회되는 게시물이 없습니다."))
//            }
            val tripsList = findTrips["trips"] as List<*>
            if (tripsList.isNotEmpty()) {
                ResponseEntity.status(HttpStatus.OK).body(findTrips)
            } else {
                ResponseEntity.status(HttpStatus.OK).body(mapOf("message" to "조회되는 게시물이 없습니다."))
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to (e.message ?: "")))
        } catch (e: DataNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("message" to e.message))
        }
    }

//    @GetMapping("/trips")
//    fun getAllTripsByUser(req: HttpServletRequest): ResponseEntity<Set<TripDto>> {
//        val memberId = req.getAttribute("memberId") as Long
//        val findTrips = tripService.getAllTripsByUser(memberId)
//        return ResponseEntity.ok(findTrips)
//    }

    @GetMapping("/trips")
    fun getAllTripsByUser(
        req: HttpServletRequest,
        @RequestParam("page", required = false, defaultValue = "1") page: Int,
        @RequestParam("size", required = false, defaultValue = "10") size: Int,
        @RequestParam("sortType", required = false, defaultValue = "1") sortType: Int
    ): ResponseEntity<Any> {
        return try {
            val memberId = req.getAttribute("memberId") as Long
            val sort = when (sortType) {
                1 -> Sort.by(Sort.Direction.DESC, "createdAt")
                -1 -> Sort.by(Sort.Direction.ASC, "createdAt")
                2 -> Sort.by(
                    Sort.Order(Sort.Direction.DESC, "views"),
                    Sort.Order(Sort.Direction.DESC, "createdAt")
                )
                -2 -> Sort.by(
                    Sort.Order(Sort.Direction.ASC, "views"),
                    Sort.Order(Sort.Direction.DESC, "createdAt")
                )
                else -> throw IllegalArgumentException("Invalid sort type")
            }
            val pageable: Pageable = PageRequest.of(page - 1, size, sort)
            val findTrips = tripService.getAllTripsByUser(memberId, pageable)
//            if (findTrips.isNotEmpty()) {
//                ResponseEntity.status(HttpStatus.OK).body(findTrips)
//            } else {
//                ResponseEntity.status(HttpStatus.OK).body(mapOf("message" to "조회되는 게시물이 없습니다."))
//            }
            val tripsList = findTrips["trips"] as List<*>
            if (tripsList.isNotEmpty()) {
                ResponseEntity.status(HttpStatus.OK).body(findTrips)
            } else {
                ResponseEntity.status(HttpStatus.OK).body(mapOf("message" to "조회되는 게시물이 없습니다."))
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to (e.message ?: "")))
        }
    }

    @GetMapping("/trips/mytrips")
    fun getAllMyTripsByUser(
        req: HttpServletRequest,
        @RequestParam("page", required = false, defaultValue = "1") page: Int,
        @RequestParam("size", required = false, defaultValue = "10") size: Int,
    ): ResponseEntity<Any> {
        return try {
            val memberId = req.getAttribute("memberId") as Long
            val pageable: Pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending())
            val findTrips = tripService.getAllMyTripsByUser(memberId, pageable)
            val tripsList = findTrips["trips"] as List<*>
            if (tripsList.isNotEmpty()) {
                ResponseEntity.status(HttpStatus.OK).body(findTrips)
            } else {
                ResponseEntity.status(HttpStatus.OK).body(mapOf("message" to "조회되는 게시물이 없습니다."))
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to (e.message ?: "")))
        } catch (e: DataNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("message" to e.message))
        }
    }

//    @GetMapping("/guest/trips")
//    fun getAllTripsByGuest(): ResponseEntity<Any> {
//        return try {
//            val findTrips = tripService.getAllTripsByGuest()
//            if (findTrips.isNotEmpty()) {
//                ResponseEntity.ok(findTrips)
//            } else {
//                ResponseEntity.notFound().build()
//            }
//        } catch (ex: IllegalAccessException) {
//            ResponseEntity.status(HttpStatus.FORBIDDEN).body("조회할 권한이 없습니다.")
//        }
//    }

    @GetMapping("/guest/trips")
    fun getAllTripsByGuest(
        @RequestParam("page", required = false, defaultValue = "1") page: Int,
        @RequestParam("size", required = false, defaultValue = "10") size: Int,
        @RequestParam("sortType", required = false, defaultValue = "2") sortType: Int
    ): ResponseEntity<Any> {
        return try {
            val sort = when (sortType) {
                1 -> Sort.by(Sort.Direction.DESC, "createdAt")
                -1 -> Sort.by(Sort.Direction.ASC, "createdAt")
                2 -> Sort.by(
                    Sort.Order(Sort.Direction.DESC, "views"),
                    Sort.Order(Sort.Direction.DESC, "createdAt")
                )
                -2 -> Sort.by(
                    Sort.Order(Sort.Direction.ASC, "views"),
                    Sort.Order(Sort.Direction.DESC, "createdAt")
                )
                else -> throw IllegalArgumentException("Invalid sort type")
            }
            val pageable: Pageable = PageRequest.of(page - 1, size, sort)
            val findTrips = tripService.getAllTripsByGuest(pageable)
            val tripsList = findTrips["trips"] as List<*>
            if (tripsList.isNotEmpty()) {
                ResponseEntity.status(HttpStatus.OK).body(findTrips)
            } else {
                ResponseEntity.status(HttpStatus.OK).body(mapOf("message" to "조회되는 게시물이 없습니다."))
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to (e.message ?: "")))
        }
    }

    @GetMapping("/nickname")
    fun getTripsByNickname(
        @RequestParam nickname: String,
        @RequestParam("page", required = false, defaultValue = "1") page: Int,
        @RequestParam("size", required = false, defaultValue = "10") size: Int,
    ): ResponseEntity<Any> {
        return try {
            val pagenationUtil = PagenationUtil()
            val (validatedPage, validatedSize) = pagenationUtil.validatePageAndSize(page, size)
            val pageable: Pageable = PageRequest.of(validatedPage - 1, validatedSize, Sort.by("createdAt").descending())
            val findTrips = tripService.getTripsByNickname(nickname, pageable)
//            return ResponseEntity.ok(findTrips)
            val tripsList = findTrips["trips"] as List<*>
            if (tripsList.isNotEmpty()) {
                ResponseEntity.status(HttpStatus.OK).body(findTrips)
            } else {
                ResponseEntity.status(HttpStatus.OK).body(mapOf("message" to "조회되는 게시물이 없습니다."))
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("message" to (e.message ?: "")))
        }
    }

    // 트립 아이디로 트립을 가져와서 트립 + 댓글s 가져오는 비회원 라우터
    @GetMapping("/guest/tripAndComments/{tripId}")
    fun getTripAndCommentsByTripId(@PathVariable tripId: String): ResponseEntity<TripAndCommentResponseDto> {
        val findTripAndComment = tripService.getTripAndCommentsIsPublicByTripIdGuest(tripId)
        return ResponseEntity.ok(findTripAndComment)
    }

    // 트립 아이디로 트립을 가져와서 트립 + 댓글s 가져오는 회원 라우터
    @GetMapping("/user/tripAndComments/{tripId}")
    fun getTripIsLikedAndCommentsByTripId(
        req: HttpServletRequest,
        @PathVariable tripId: String,
    ): ResponseEntity<TripAndCommentResponseDto> {
        val memberId = req.getAttribute("memberId") as Long
        val findTripAndComment = tripService.getTripAndCommentsIsLikedByTripIdMember(tripId, memberId)
        return ResponseEntity.ok(findTripAndComment)
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
        @RequestParam("pageSize", required = false, defaultValue = "10") pageSize: Int,
    ): ResponseEntity<Map<String, Any>> {
        val sortedCountryFrequencyMap = tripService.getTripCategoryByNickname(nickname, page, pageSize)
        return ResponseEntity.ok(sortedCountryFrequencyMap)
    }

    // 해당 nickname 트립을 가져와서 특정 나라의 여행 목록을 반환하는 엔드포인트
    @GetMapping("/nickname/trips/country/{country}")
    fun getTripsInCountry(
        @RequestParam("nickname") nickname: String,
        @PathVariable("country") country: String,
    ): ResponseEntity<Set<TripDto>> {
        val sortedCountryFrequencyMap = tripService.getTripsInCountry(nickname, country)
        return ResponseEntity.ok(sortedCountryFrequencyMap)
    }

    @GetMapping("/nickname/tripsWithPagination/country/{country}")
    fun getTripsInCountryWithPagination(
        @RequestParam("nickname") nickname: String,
        @PathVariable("country") country: String,
        @RequestParam("page", required = false, defaultValue = "1") page: Int,
        @RequestParam("pageSize", required = false, defaultValue = "10") pageSize: Int,
    ): ResponseEntity<Map<String, Any>> {
        val sortedCountryFrequencyMap = tripService.getTripsInCountry(nickname, country, page, pageSize)
        return ResponseEntity.ok(sortedCountryFrequencyMap)
    }

    // 해당 nickname 트립을 가져와서 나라별 여행 횟수를 많은 순으로 정렬하여 반환하는 엔드포인트
    @GetMapping("/nickname/trips/country-frequencies")
    fun getCountryFrequencies(@RequestParam("nickname") nickname: String): ResponseEntity<List<TripCountryFrequencyDto>> {
        val countryFrequencyMap = tripService.getCountryFrequencies(nickname)
        return ResponseEntity.ok(countryFrequencyMap)
    }

    @GetMapping("/{id}")
    fun getTripByMemberIdAndId(req: HttpServletRequest, @PathVariable id: String): ResponseEntity<TripDto> {
        val memberId = req.getAttribute("memberId") as Long
        val findTrip = tripService.getTripByMemberIdAndId(memberId, id)
        return if (findTrip != null) {
            ResponseEntity.ok(findTrip)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("modify/{id}")
    fun getTripByMemberIdAndIdToUpdate(
        req: HttpServletRequest,
        @PathVariable id: String,
    ): ResponseEntity<TripUpdateResponseDto> {
        val memberId = req.getAttribute("memberId") as Long
        val findTrip = tripService.getTripByMemberIdAndIdToUpdate(memberId, id)
        return if (findTrip != null) {
            ResponseEntity.ok(findTrip)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/guest/{id}")
    fun getTripById(@PathVariable id: String): ResponseEntity<TripDto> {
        val findTrip = tripService.getTripIsPublicById(id)
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

    @GetMapping("/list/following")
    fun getListFollowingTrips(
        req: HttpServletRequest,
        @RequestParam("page", required = false, defaultValue = "1") page: Int,
        @RequestParam("size", required = false, defaultValue = "10") size: Int,
    ): ResponseEntity<Any> {
        return try {
            val memberId = req.getAttribute("memberId") as Long
            val pagenationUtil = PagenationUtil()
            val (validatedPage, validatedSize) = pagenationUtil.validatePageAndSize(page, size)
            val pageable: Pageable = PageRequest.of(validatedPage - 1, validatedSize)
            val findTrips = tripService.getListFollowingTrips(memberId, pageable)
            if (findTrips.isNotEmpty()) {
                ResponseEntity.status(HttpStatus.OK).body(findTrips)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body("조회할 권한이 없습니다.")
        } catch (e: DataNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
        }
    }

//    @GetMapping("/search")
//    fun getSearchTripsByKeyword(
//        req: HttpServletRequest,
//        @RequestParam keyword: String,
//        @RequestParam sorting: Int
//    ): ResponseEntity<Any> {
//        return try {
//            val memberId = req.getAttribute("memberId") as Long
//            val findTrips = tripService.getSearchTripsByKeyword(memberId, keyword, sorting)
//            if (findTrips.isNotEmpty()) {
//                ResponseEntity.status(HttpStatus.OK).body(findTrips)
//            } else {
//                ResponseEntity.status(HttpStatus.OK).body(mapOf("message" to "조회되는 게시물이 없습니다."))
//            }
//        } catch (e: IllegalArgumentException) {
//            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to (e.message ?: "")))
//        }
//    }

//    @GetMapping("/search")
//    fun getSearchTripsByKeyword(
//        req: HttpServletRequest,
//        @RequestParam keyword: String,
//        @RequestParam("page", required = false, defaultValue = "1") page: Int,
//        @RequestParam("size", required = false, defaultValue = "10") size: Int,
//        @RequestParam("sortType", required = false, defaultValue = "1") sortType: Int
//    ): ResponseEntity<Any> {
//        return try {
//            val memberId = req.getAttribute("memberId") as Long
//            val pagenationUtil = PagenationUtil()
//            val (validatedPage, validatedSize) = pagenationUtil.validatePageAndSize(page, size)
//            val pageable: Pageable = PageRequest.of(validatedPage - 1, validatedSize)
//            val findTrips = tripService.getSearchTripsByKeyword(memberId, keyword, sortType, pageable)
//            if (findTrips.isNotEmpty()) {
//                ResponseEntity.status(HttpStatus.OK).body(findTrips)
//            } else {
//                ResponseEntity.status(HttpStatus.OK).body(mapOf("message" to "조회되는 게시물이 없습니다."))
//            }
//        } catch (e: IllegalArgumentException) {
//            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to (e.message ?: "")))
//        }
//    }

    @PutMapping("/{id}", consumes = ["multipart/form-data"])
    fun updateTrip(
        req: HttpServletRequest,
        @PathVariable id: String,
        @Validated @ModelAttribute tripUpdateDto: TripUpdateDto,
    ): ResponseEntity<Any> {
        return try {
            val memberId = req.getAttribute("memberId") as Long
            val findTrip = tripService.getTripById(id)
            if (findTrip != null) {
                val updatedTrip = tripService.updateTrip(memberId, tripUpdateDto)
                ResponseEntity.status(HttpStatus.OK).body(updatedTrip, "게시물이 수정되었습니다.")
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalArgumentException) {
            throw BadRequestException("요청이 잘못되었습니다. ${e.message}")
        } catch (e: IllegalAccessException) {
            throw ForbiddenException("수정할 권한이 없습니다. ${e.message}")
        }
    }

    @DeleteMapping("/{id}")
    fun deleteTrip(req: HttpServletRequest, @PathVariable id: String): ResponseEntity<Any> {
        return try {
            val memberId = req.getAttribute("memberId") as Long
            val findTrip = tripService.getTripById(id)
            if (findTrip != null) {
                tripService.deleteTripById(memberId, id)
                ResponseEntity.status(HttpStatus.OK).body(mapOf("message" to "게시물이 삭제되었습니다."))
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("message" to "해당 게시글이 존재하지 않습니다."))
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to (e.message ?: "")))
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("message" to (e.message ?: "")))
        }
    }
}

private fun ResponseEntity.BodyBuilder.body(returnedTripDto: TripDto, message: String): ResponseEntity<Any> {
    val responseBody = mapOf(
        "message" to message,
        "trip" to returnedTripDto,
    )
    return this.body(responseBody)
}
