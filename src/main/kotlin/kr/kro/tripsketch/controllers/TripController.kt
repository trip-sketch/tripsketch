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


/**
 * 게시물과 관련된 요청을 처리하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("api/trip")
class TripController(private val tripService: TripService) {

    /**
     * 게시물을 작성합니다.
     * */
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

    /**
     * 관리자 권한으로 전체 게시물을 조회합니다.
     * */
    @GetMapping("/admin/trips")
    fun getAllTrips(
        req: HttpServletRequest,
        @RequestParam("page", required = false, defaultValue = "1") page: Int,
        @RequestParam("size", required = false, defaultValue = "10") size: Int,
        @RequestParam("sortType", required = false, defaultValue = "1") sortType: Int
    ): ResponseEntity<Any> {
        return try {
            val memberId = req.getAttribute("memberId") as Long
            val sort = getSort(sortType)
            val pagenationUtil = PagenationUtil()
            val (validatedPage, validatedSize) = pagenationUtil.validatePageAndSize(page, size)
            val pageable: Pageable = PageRequest.of(validatedPage - 1, validatedSize, sort)
            val findTrips = tripService.getAllTrips(memberId, pageable)
            ResponseEntity.status(HttpStatus.OK).body(findTrips)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to (e.message ?: "")))
        }
    }

    /**
     * 사용자 권한으로 전체 게시물을 조회합니다.
     * */
    @GetMapping("/trips")
    fun getAllTripsByUser(
        req: HttpServletRequest,
        @RequestParam("page", required = false, defaultValue = "1") page: Int,
        @RequestParam("size", required = false, defaultValue = "10") size: Int,
        @RequestParam("sortType", required = false, defaultValue = "1") sortType: Int
    ): ResponseEntity<Any> {
        return try {
            val memberId = req.getAttribute("memberId") as Long
            val sort = getSort(sortType)
            val pageable: Pageable = PageRequest.of(page - 1, size, sort)
            val findTrips = tripService.getAllTripsByUser(memberId, pageable)
            ResponseEntity.status(HttpStatus.OK).body(findTrips)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to (e.message ?: "")))
        }
    }

    /**
     * 사용자가 작성한 전체 게시물을 조회합니다.
     * */
    @GetMapping("/trips/mytrips")
    fun getAllMyTripsByUser(
        req: HttpServletRequest,
        @RequestParam("page", required = false, defaultValue = "1") page: Int,
        @RequestParam("size", required = false, defaultValue = "10") size: Int,
    ): ResponseEntity<Any> {
        return try {
            val memberId = req.getAttribute("memberId") as Long
            val pagenationUtil = PagenationUtil()
            val (validatedPage, validatedSize) = pagenationUtil.validatePageAndSize(page, size)
            val pageable: Pageable = PageRequest.of(validatedPage - 1, validatedSize,Sort.by("createdAt").descending())
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

    /**
     * 게스트 권한으로 전체 게시물을 조회합니다.
     * */
    @GetMapping("/guest/trips")
    fun getAllTripsByGuest(
        @RequestParam("page", required = false, defaultValue = "1") page: Int,
        @RequestParam("size", required = false, defaultValue = "10") size: Int,
        @RequestParam("sortType", required = false, defaultValue = "2") sortType: Int
    ): ResponseEntity<Any> {
        return try {
           val sort = getSort(sortType)
           val pagenationUtil = PagenationUtil()
            val (validatedPage, validatedSize) = pagenationUtil.validatePageAndSize(page, size)
            val pageable: Pageable = PageRequest.of(validatedPage - 1, validatedSize, sort)
            val findTrips = tripService.getAllTripsByGuest(pageable)
            ResponseEntity.status(HttpStatus.OK).body(findTrips)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to (e.message ?: "")))
        }
    }

    /**
     * 닉네임을 기준으로 전체 게시물을 조회합니다.
     * */
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
            ResponseEntity.status(HttpStatus.OK).body(findTrips)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("message" to (e.message ?: "")))
        }
    }

    // 트립 아이디로 트립을 가져와서 트립 + 댓글s 가져오는 비회원 라우터
    @GetMapping("/guest/trip-comments/{tripId}")
    fun getTripAndCommentsByTripId(@PathVariable tripId: String): ResponseEntity<TripAndCommentResponseDto> {
        val findTripAndComment = tripService.getTripAndCommentsIsPublicByTripIdGuest(tripId)
        return ResponseEntity.ok(findTripAndComment)
    }

    // 트립 아이디로 트립을 가져와서 트립 + 댓글s 가져오는 회원 라우터
    @GetMapping("/user/trip-comments/{tripId}")
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
    fun getTripsCategorizedByCountry(@RequestParam("nickname") nickname: String): ResponseEntity<Pair<Map<String, Int>, Set<TripCardDto>>> {
        val sortedCountryFrequencyMap = tripService.getTripCategoryByNickname(nickname)
        return ResponseEntity.ok(sortedCountryFrequencyMap)
    }

    @GetMapping("/nickname/trips-pagination/categories")
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
    ): ResponseEntity<Set<TripCardDto>> {
        val sortedCountryFrequencyMap = tripService.getTripsInCountry(nickname, country)
        return ResponseEntity.ok(sortedCountryFrequencyMap)
    }

    @GetMapping("/nickname/trips-pagination/country/{country}")
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


    /**
     * 사용자 권한, 게시물 ID 를 기준으로 게시물을 조회합니다.
     * */
    @GetMapping("/{id}")
    fun getTripByIdWithMemberId(req: HttpServletRequest, @PathVariable id: String): ResponseEntity<Any> {
        return try {
            val memberId = req.getAttribute("memberId") as Long
            val findTrip = tripService.getTripByIdWithMemberId(memberId, id)
            if (findTrip != null) {
                ResponseEntity.status(HttpStatus.OK).body(findTrip)
            } else {
                ResponseEntity.status(HttpStatus.OK).body(mapOf("message" to "해당 게시글이 존재하지 않습니다."))
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to (e.message ?: "")))
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


    /**
     * 게스트 권한, 게시물 ID 를 기준으로 게시물을 조회합니다.
     * */
    @GetMapping("/guest/{id}")
    fun getTripById(@PathVariable id: String): ResponseEntity<Any> {
        return try {
            val findTrip = tripService.getTripById(id)
            if (findTrip != null) {
                ResponseEntity.status(HttpStatus.OK).body(findTrip)
            } else {
                ResponseEntity.status(HttpStatus.OK).body(mapOf("message" to "해당 게시글이 존재하지 않습니다."))
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to (e.message ?: "")))
        }
    }

    /**
     * 내가 구독한 게시물을 조회합니다.
     * 정렬방식은 구독하는 사람 각각의 게시글 중 가장 높은 조회수의 게시물을 1개씩 조회하여 정렬합니다
     * */
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
            val tripsList = findTrips["trips"] as List<*>
            if (tripsList.isNotEmpty()) {
                ResponseEntity.status(HttpStatus.OK).body(findTrips)
            } else {
                ResponseEntity.status(HttpStatus.OK).body(mapOf("message" to "구독하는 게시물이 없습니다."))
            }
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to (e.message ?: "")))
        }
    }

    /**
     * 검색어를 입력하여 게시물을 조회합니다.
     * 검색 조건은 게시글의 제목과 내용이며, sortType 에 따라 정렬합니다.
     * */
    @GetMapping("/search")
    fun getSearchTripsByKeyword(
        req: HttpServletRequest,
        @RequestParam keyword: String,
        @RequestParam("page", required = false, defaultValue = "1") page: Int,
        @RequestParam("size", required = false, defaultValue = "10") size: Int,
        @RequestParam("sortType", required = false, defaultValue = "1") sortType: Int
    ): ResponseEntity<Any> {
        return try {
            val memberId = req.getAttribute("memberId") as Long
            val sort = getSort(sortType)
            val pagenationUtil = PagenationUtil()
            val (validatedPage, validatedSize) = pagenationUtil.validatePageAndSize(page, size)
            val pageable: Pageable = PageRequest.of(validatedPage - 1, validatedSize, sort)
            val findTrips = tripService.getSearchTripsByKeyword(memberId, keyword, pageable)
            ResponseEntity.status(HttpStatus.OK).body(findTrips)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to (e.message ?: "")))
        }
    }

    /**
     * 게스트 권한으로 검색어를 입력하여 게시물을 조회합니다.
     * 검색 조건은 게시글의 제목과 내용이며, sortType 에 따라 정렬합니다.
     * */
    @GetMapping("/guest/search")
    fun getSearchTripsByKeywordAsGuest(
        @RequestParam keyword: String,
        @RequestParam("page", required = false, defaultValue = "1") page: Int,
        @RequestParam("size", required = false, defaultValue = "10") size: Int,
        @RequestParam("sortType", required = false, defaultValue = "1") sortType: Int
    ): ResponseEntity<Any> {
        return try {
            val sort = getSort(sortType)
            val pagenationUtil = PagenationUtil()
            val (validatedPage, validatedSize) = pagenationUtil.validatePageAndSize(page, size)
            val pageable: Pageable = PageRequest.of(validatedPage - 1, validatedSize, sort)
            val findTrips = tripService.getSearchTripsByKeywordAsGuest(keyword, pageable)
            ResponseEntity.status(HttpStatus.OK).body(findTrips)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to (e.message ?: "")))
        }
    }

    /**
     * 게시물을 수정합니다.
     * */
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
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("message" to "해당 게시글이 존재하지 않습니다."))
            }
        } catch (e: IllegalArgumentException) {
            throw BadRequestException("요청이 잘못되었습니다. ${e.message}")
        } catch (e: IllegalAccessException) {
            throw ForbiddenException("수정할 권한이 없습니다. ${e.message}")
        }
    }

    /**
     * 게시물을 삭제합니다(soft delete).
     * */
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


/**
 * 에러메세지와 반환형태를 전달해주는 함수입니다.
 * */
private fun ResponseEntity.BodyBuilder.body(returnedTripDto: TripDto, message: String): ResponseEntity<Any> {
    val responseBody = mapOf(
        "message" to message,
        "trip" to returnedTripDto,
    )
    return this.body(responseBody)
}

/**
 * sortType 에 따른 정렬방식을 정해주는 함수입니다.
 * */
private fun getSort(sortType: Int): Sort {
    return when (sortType) {
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
        3 -> Sort.by(
            Sort.Order(Sort.Direction.DESC, "likes"),
            Sort.Order(Sort.Direction.DESC, "createdAt")
        )
        -3 -> Sort.by(
            Sort.Order(Sort.Direction.ASC, "likes"),
            Sort.Order(Sort.Direction.DESC, "createdAt")
        )
        else -> throw IllegalArgumentException("Invalid sort type")
    }
}
