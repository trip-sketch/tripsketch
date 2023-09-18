package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.HashtagInfo
import kr.kro.tripsketch.domain.Trip
import kr.kro.tripsketch.dto.*
import kr.kro.tripsketch.exceptions.ForbiddenException
import kr.kro.tripsketch.repositories.CommentRepository
import kr.kro.tripsketch.repositories.FollowRepository
import kr.kro.tripsketch.repositories.TripRepository
import kr.kro.tripsketch.repositories.UserRepository
import kr.kro.tripsketch.utils.EnvLoader
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import kotlin.math.ceil

@Service
class TripService(
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val followRepository: FollowRepository,
    private val userService: UserService,
    private val notificationService: NotificationService,
    private val imageService: ImageService,
    private val commentService: CommentService,
) {
    /**
     * 게시물을 작성하는 메소드입니다.
     * @param memberId 현재 사용자의 멤버 ID
     * @param tripCreateDto 신규로 작성될 게시물 정보
     * @param images 업로드 파일
     * @return 신규로 작성된 게시물 (TripDto)
     * */
    fun createTrip(memberId: Long, tripCreateDto: TripCreateDto, images: List<MultipartFile>?): TripDto {
        val user = userService.findUserByMemberId(memberId) ?: throw IllegalArgumentException("해당 이메일의 사용자 존재하지 않습니다.")
        val startedAt = tripCreateDto.startedAt
        val endAt = tripCreateDto.endAt
        if (startedAt != null && endAt != null && startedAt.isAfter(endAt)) {
            throw IllegalArgumentException("시작일은 종료일보다 같거나 그 이전이어야 합니다.")
        }
        val uploadedImageUrls = images?.map { imageService.uploadImage("tripsketch/trip-sketching", it) }

        val hashtagInfo = HashtagInfo(
            countryCode = tripCreateDto.countryCode,
            country = tripCreateDto.country,
            city = tripCreateDto.city,
            municipality = tripCreateDto.municipality,
            name = tripCreateDto.name,
            displayName = tripCreateDto.displayName,
            road = tripCreateDto.road,
            address = tripCreateDto.address,
            etc = tripCreateDto.etc,
        )

        val newTrip = Trip(
            userId = user.id!!,
            title = tripCreateDto.title,
            content = tripCreateDto.content,
            location = tripCreateDto.country,
            startedAt = startedAt,
            endAt = endAt,
            latitude = tripCreateDto.latitude,
            longitude = tripCreateDto.longitude,
            hashtagInfo = hashtagInfo,
            isPublic = tripCreateDto.isPublic,
            images = uploadedImageUrls
        )
        val createdTrip = tripRepository.save(newTrip)
        val userId = userRepository.findByMemberId(memberId)?.id ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        val follower = followRepository.findByFollowing(userId)
        val filteredFollower = follower.filter { it.follower != userId }
        val followerUserIds = filteredFollower.map { it.follower }
        val followingNickname = userService.findUserByMemberId(memberId)?.nickname ?: "Unknown user"
        val followingProfileUrl = userService.findUserByMemberId(memberId)?.profileImageUrl ?: ""
        notificationService.sendPushNotification(
            followerUserIds,
            "새로운 여행의 시작, 트립스케치",
            "$followingNickname 님이 새로운 글을 작성하였습니다.",
            null,
            null,
            createdTrip.id,
            followingNickname,
            followingProfileUrl,
        )
        return fromTrip(createdTrip, userId)
    }

    /**
     * 관리자 권한으로 전체 게시물을 조회합니다.
     *  - 비공개로 작성된 게시물 및 삭제한 게시물을 모두 조회할 수 있습니다.
     * @param memeberId 현재 사용자의 멤버 ID
     * @param pageable 페이지네이션 정보
     * @return 조회한 게시물 정보 목록 (TripDto)
     * */
    fun getAllTrips(memberId: Long, pageable: Pageable): Map<String, Any> {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        val findTrips = tripRepository.findAll(pageable)
        val tripsDtoList = findTrips.content.map { fromTrip(it, userId) }
        val currentPage = findTrips.number + 1
        val totalPage = findTrips.totalPages
        val postsPerPage = findTrips.size
        if (currentPage > totalPage && findTrips.content.isNotEmpty()) {
            throw IllegalArgumentException("현재 페이지가 총 페이지 수보다 큽니다.")
        }
        val trips = if (tripsDtoList.isNotEmpty()) tripsDtoList else emptyList<Map<String, Any>>()
        return mapOf(
            "currentPage" to currentPage,
            "trips" to trips,
            "postsPerPage" to postsPerPage,
            "totalPages" to totalPage,
        )
    }

    /**
     * 사용자 권한으로 전체 게시물을 조회합니다.
     *  - 비공개로 작성된 게시물 및 삭제한 게시물을 모두 조회할 수 있습니다.
     * @param memeberId 현재 사용자의 멤버 ID
     * @param pageable 페이지네이션 정보
     * @return 조회한 게시물 정보 목록 (TripCardDto)
     * */
    fun getAllTripsByUser(memberId: Long, pageable: Pageable): Map<String, Any> {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        val findTrips = tripRepository.findByIsPublicIsTrueAndIsHiddenIsFalse(pageable)
        val tripsDtoList = findTrips.content.map { fromTripToTripCardDto(it, userId) }
        val currentPage = findTrips.number + 1
        val totalPage = findTrips.totalPages
        val postsPerPage = findTrips.size
        if (currentPage > totalPage && findTrips.content.isNotEmpty()) {
            throw IllegalArgumentException("현재 페이지가 총 페이지 수보다 큽니다.")
        }
        val trips = if (tripsDtoList.isNotEmpty()) tripsDtoList else emptyList<Map<String, Any>>()
        return mapOf(
            "currentPage" to currentPage,
            "trips" to trips,
            "postsPerPage" to postsPerPage,
            "totalPages" to totalPage,
        )
    }

    /**
     * 사용자 권한으로 사용자가 작성한 전체 게시물을 조회합니다.
     *  - 사용자가 직접 작성한 비공개 게시물을 조회할 수 있습니다.
     * @param memeberId 현재 사용자의 멤버 ID
     * @param pageable 페이지네이션 정보
     * @return 조회한 게시물 정보 목록 (TripCardDto)
     * */
    fun getAllMyTripsByUser(memberId: Long, pageable: Pageable): Map<String, Any> {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        val findTrips = tripRepository.findByIsHiddenIsFalseAndUserId(userId, pageable)
        val tripsDtoList = findTrips.content.map { fromTripToTripCardDto(it, userId) }
        val currentPage = findTrips.number + 1
        val totalPage = findTrips.totalPages
        val postsPerPage = findTrips.size
        if (currentPage > totalPage && findTrips.content.isNotEmpty()) {
            throw IllegalArgumentException("현재 페이지가 총 페이지 수보다 큽니다.")
        }
        return mapOf(
            "currentPage" to currentPage,
            "trips" to tripsDtoList,
            "postsPerPage" to postsPerPage,
            "totalPages" to totalPage,
        )
    }

    /**
     * 게스트 권한으로 전체 게시물을 조회합니다.
     *  - 사용자가 직접 작성한 비공개 게시물을 조회할 수 있습니다.
     * @param pageable 페이지네이션 정보
     * @return 조회한 게시물 정보 목록 (TripCardDto)
     * */
    fun getAllTripsByGuest(pageable: Pageable): Map<String, Any> {
        val findTrips = tripRepository.findByIsPublicIsTrueAndIsHiddenIsFalse(pageable)
        val tripsDtoList = findTrips.content.map { fromTripToTripCardDto(it, "") }
        val currentPage = findTrips.number + 1
        val totalPage = findTrips.totalPages
        val postsPerPage = findTrips.size
        if (currentPage > totalPage && findTrips.content.isNotEmpty()) {
            throw IllegalArgumentException("현재 페이지가 총 페이지 수보다 큽니다.")
        }
        val trips = if (tripsDtoList.isNotEmpty()) tripsDtoList else emptyList<Map<String, Any>>()
        return mapOf(
            "currentPage" to currentPage,
            "trips" to trips,
            "postsPerPage" to postsPerPage,
            "totalPages" to totalPage,
        )
    }

    /**
     * 닉네임을 기준으로 게시물을 조회합니다.
     * @param nickname 닉네임
     * @param pageable 페이지네이션 정보
     * @return 조회한 게시물 정보 목록 (TripCardDto)
     * */
    fun getTripsByNickname(nickname: String, pageable: Pageable): Map<String, Any> {
        val user = userService.findUserByNickname(nickname) ?: throw IllegalArgumentException("해당 유저를 조회 할 수 없습니다.")
        val findTrips = user.id?.let { tripRepository.findByIsPublicIsTrueAndIsHiddenIsFalseAndUserId(it, pageable) }
            ?: throw IllegalArgumentException("작성한 게시글이 존재하지 않습니다.")
        val tripsDtoList = findTrips.content.map { fromTripToTripCardDto(it, user.id) }
        val currentPage = findTrips.number + 1
        val totalPage = findTrips.totalPages
        val postsPerPage = findTrips.size
        if (currentPage > totalPage && findTrips.content.isNotEmpty()) {
            throw IllegalArgumentException("현재 페이지가 총 페이지 수보다 큽니다.")
        }
        val trips = if (tripsDtoList.isNotEmpty()) tripsDtoList else emptyList<Map<String, Any>>()
        return mapOf(
            "currentPage" to currentPage,
            "trips" to trips,
            "postsPerPage" to postsPerPage,
            "totalPages" to totalPage,
        )
    }

    fun getTripAndCommentsIsPublicByTripIdGuest(id: String): TripAndCommentResponseDto {
        val findTrip = tripRepository.findByIdAndIsPublicIsTrueAndIsHiddenIsFalse(id) ?: throw IllegalArgumentException("게시글이 존재하지 않습니다.")
        val commentDtoList = commentService.getCommentsGuestByTripId(id)
        return fromTripAndComments(findTrip, commentDtoList, "")
    }

    fun getTripAndCommentsIsLikedByTripIdMember(id: String, memberId: Long): TripAndCommentResponseDto {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        val findTrip = tripRepository.findByIdAndIsHiddenIsFalse(id) ?: throw IllegalArgumentException("게시글이 존재하지 않습니다.")
        if (findTrip.isPublic == false && findTrip.userId != userId) {
            throw ForbiddenException("해당 게시물에 접근 권한이 없습니다.")
        }
        val commentDtoList = commentService.getIsLikedByMemberIdForTrip(memberId, id)
        return fromTripAndComments(findTrip, commentDtoList, userId)
    }

    fun getTripCategoryByNickname(nickname: String): Pair<Map<String, Int>, Set<TripCardDto>> {
        val user = userService.findUserByNickname(nickname) ?: throw IllegalArgumentException("해당 유저를 조회 할 수 없습니다.")
        val findTrips = user.id?.let { tripRepository.findTripByUserIdAndIsPublicIsTrueAndIsHiddenIsFalse(it) }
            ?: throw IllegalArgumentException("해당 게시물 존재하지 않습니다.")
        return findTrips.categorizeTripsByCountry()
    }

    fun getTripCategoryByNickname(nickname: String, page: Int, pageSize: Int): Map<String, Any> {
        val user = userService.findUserByNickname(nickname) ?: throw IllegalArgumentException("해당 유저를 조회 할 수 없습니다.")
        val findTrips = user.id?.let { tripRepository.findTripByUserIdAndIsPublicIsTrueAndIsHiddenIsFalse(it) }
            ?: throw IllegalArgumentException("해당 게시물 존재하지 않습니다.")
        // 전체 여행 목록을 카테고리화
        val categorizedTrips = findTrips.categorizeTripsByCountry()
        // 페이지네이션 적용
        return paginateTrips(categorizedTrips.second, page, pageSize)
    }

    fun getTripsInCountry(nickname: String, country: String): Set<TripCardDto> {
        val user = userService.findUserByNickname(nickname) ?: throw IllegalArgumentException("해당 유저를 조회 할 수 없습니다.")
        val findTrips = user.id?.let { tripRepository.findTripByUserIdAndIsPublicIsTrueAndIsHiddenIsFalse(it) }
            ?: throw IllegalArgumentException("해당 게시물 존재하지 않습니다.")
        return findTrips.getTripsInCountry(country)
    }

    fun getTripsInCountry(nickname: String, country: String, page: Int, pageSize: Int): Map<String, Any> {
        val user = userService.findUserByNickname(nickname) ?: throw IllegalArgumentException("해당 유저를 조회 할 수 없습니다.")
        val findTrips = user.id?.let { tripRepository.findTripByUserIdAndIsPublicIsTrueAndIsHiddenIsFalse(it) }
            ?: throw IllegalArgumentException("해당 게시물 존재하지 않습니다.")
        val tripsInCountry = findTrips.getTripsInCountry(country)

        // 페이지네이션 적용
        return paginateTrips(tripsInCountry, page, pageSize)
    }

    fun getCountryFrequencies(nickname: String): List<TripCountryFrequencyDto> {
        val user = userService.findUserByNickname(nickname) ?: throw IllegalArgumentException("해당 유저를 조회 할 수 없습니다.")
        val findTrips = user.id?.let { tripRepository.findTripByUserIdAndIsPublicIsTrueAndIsHiddenIsFalse(it) }
            ?: throw IllegalArgumentException("해당 게시물 존재하지 않습니다.")
        return findTrips.sortTripsByCountryFrequency()
    }

    /**
     * 여행 목록을 나라 기준으로 카테고리화하고 결과를 반환합니다.
     *
     */
    fun Set<Trip>.categorizeTripsByCountry(): Pair<Map<String, Int>, Set<TripCardDto>> {
        // 나라별 여행 횟수를 계산하기 위한 맵
        val countryFrequencyMap = mutableMapOf<String, Int>()

        // 각 여행을 반복하면서 나라별 횟수를 업데이트
        for (trip in this) {
            trip.hashtagInfo?.country?.let { country ->
                countryFrequencyMap[country] = countryFrequencyMap.getOrDefault(country, 0) + 1
            }
        }

        // 나라별 횟수를 내림차순으로 정렬한 맵
        val sortedCountryFrequencyMap = countryFrequencyMap.entries
            .sortedByDescending { it.value }
            .associateBy({ it.key }, { it.value })

        // 여행 목록을 최신순으로 정렬하고 나라별 횟수 순으로 다시 정렬하여 카테고리화
        val sortedTrips = this.sortedByDescending { it.createdAt }
            .sortedWith(compareByDescending { sortedCountryFrequencyMap[it.hashtagInfo?.country] })

        // TripDto로 변환한 여행 목록을 Set으로 반환
        val categorizedTrips = sortedTrips.map { fromTripToTripCardDto(it, "") }.toSet()

        return sortedCountryFrequencyMap to categorizedTrips
    }

    /**
     * 특정 나라의 여행 목록을 반환합니다.
     *
     * @param targetCountry 검색할 나라의 이름
     * @return 해당 나라의 여행 목록
     */
    fun Set<Trip>.getTripsInCountry(targetCountry: String): Set<TripCardDto> {
        // 지정된 나라와 일치하는 여행만 필터링하고 TripDto로 변환하여 반환
        val filteredTrips = this.filter { trip ->
            trip.hashtagInfo?.country == targetCountry
        }

        // 최신순으로 정렬
        val sortedTrips = filteredTrips.sortedByDescending { it.createdAt }

        return sortedTrips.map { fromTripToTripCardDto(it, "") }.toSet()
    }

    /**
     * 나라 기준으로 여행 횟수를 많은 순으로 정렬하여 반환합니다.
     *
     * @return 나라별 여행 횟수를 내림차순으로 정렬한 맵
     */
    fun Set<Trip>.sortTripsByCountryFrequency(): List<TripCountryFrequencyDto> {
        val countryInfoList = mutableListOf<TripCountryFrequencyDto>()
        // 나라별 여행 횟수를 계산하기 위한 맵
        val countryFrequencyMap = mutableMapOf<String, Int>()

        // 최신순으로 정렬
        val sortedTrips = this.sortedByDescending { it.createdAt }

        // 각 여행을 반복하면서 나라별 횟수를 업데이트
        for (trip in sortedTrips) {
            trip.hashtagInfo?.country?.let { country ->
                countryFrequencyMap[country] = countryFrequencyMap.getOrDefault(country, 0) + 1
            }
        }

        // 나라별 횟수를 내림차순으로 정렬한 맵을 리스트로 변환
        val sortedCountryFrequencyList = countryFrequencyMap.entries
            .sortedByDescending { it.value }

        // CountryInfo 객체를 생성하여 리스트에 추가
        for (entry in sortedCountryFrequencyList) {
            val countryInfo = TripCountryFrequencyDto(entry.key, entry.value)
            countryInfoList.add(countryInfo)
        }

        return countryInfoList
    }

    /**
     * 사용자 권한으로 게시물 ID 를 기준으로 게시물을 조회합니다.
     *  - 단, 사용자 정보는 로그인 상태만 확인하고, 게시물 조회에 영향을 주지 않습니다.
     * @param memeberId 현재 사용자의 멤버 ID
     * @param id 게시물 ID
     * @return 조회한 게시물 정보 (TripDto) 또는 null
     * */
    fun getTripByIdWithMemberId(memberId: Long, id: String): TripDto? {
        val findTrip = tripRepository.findByIdAndIsHiddenIsFalse(id)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        if (!findTrip.tripViews.contains(userId) && findTrip.userId != userId) {
            findTrip.tripViews.add(userId)
            findTrip.views += 1
            tripRepository.save(findTrip)
        }
        return fromTrip(findTrip, userId)
    }

    fun getTripByMemberIdAndIdToUpdate(memberId: Long, id: String): TripUpdateResponseDto? {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        val findTrip = tripRepository.findByIdAndIsHiddenIsFalse(id)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        return fromTripToUpdate(findTrip, userId)
    }


    /**
     * 게스트 권한, 게시물 ID 를 기준으로 게시물을 조회합니다.
     *  - 단, 좋아요 상태는 게스트이기 때문에 false 로 표시됩니다.
     * @param id 게시물 ID
     * @return 조회한 게시물 정보 (TripDto)
     * */
    fun getTripById(id: String): TripDto? {
        val findTrip = tripRepository.findByIdAndIsHiddenIsFalse(id)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        return fromTrip(findTrip, "")
    }

    /**
     * 내가 구독한 게시물을 조회합니다.
     * @param memeberId 현재 사용자의 멤버 ID
     * @param pageable 페이지네이션
     * @return 내가 구독하는 게시물 목록
     * */
    fun getListFollowingTrips(memberId: Long, pageable: Pageable): Map<String, Any> {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        val followingUsers = followRepository.findByFollower(userId)
            .filter { it.following != userId }
            .map { it.following }
        val tripDtoList = mutableListOf<TripCardDto>()
        followingUsers.forEach { followingUserId ->
            val findLatestTrip = tripRepository.findFirstByUserIdAndIsHiddenIsFalseOrderByCreatedAtDesc(followingUserId)
            if (findLatestTrip != null) {
                tripDtoList.add(fromTripToTripCardDto(findLatestTrip, userId))
            }
        }
        tripDtoList.sortWith(compareBy<TripCardDto> { it.views }.thenByDescending { it.createdAt })

        val startIndex = pageable.pageNumber * pageable.pageSize
        val endIndex = (startIndex + pageable.pageSize).coerceAtMost(tripDtoList.size)
        val currentPage = pageable.pageNumber + 1
        val totalPage = ceil(tripDtoList.size.toDouble() / pageable.pageSize).toInt()
        val postsPerPage = pageable.pageSize

        val pagedTripDtoList = tripDtoList.subList(startIndex, endIndex)
        if (currentPage > totalPage && tripDtoList.isNotEmpty()) {
            throw IllegalArgumentException("현재 페이지가 총 페이지 수보다 큽니다.")
        }

        return mapOf(
            "currentPage" to currentPage,
            "trips" to pagedTripDtoList,
            "postsPerPage" to postsPerPage,
            "totalPages" to totalPage,
        )
    }

    /**
     * 사용자 권한, keyword 를 기반으로 게시물 목록을 조회합니다.
     *  - 게시물 마다 좋아요 상태가 표시됩니다.
     * @param memeberId 현재 사용자의 멤버 ID
     * @param keyword 검색어(제목 또는 내용으로 검색)
     * @param pageable 페이지네이션 정보
     * @return 검색된 게시물 목록
     * */
    fun getSearchTripsByKeyword(memberId: Long, keyword: String, pageable: Pageable): Map<String, Any> {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")

        val findTrips = tripRepository.findTripsByKeyword(keyword, pageable)
        val tripsDtoList = findTrips.content.map { fromTripToTripCardDto(it, userId) }
        val currentPage = findTrips.number + 1
        val totalPage = findTrips.totalPages
        val postsPerPage = findTrips.size
        if (currentPage > totalPage && findTrips.content.isNotEmpty()) {
            throw IllegalArgumentException("현재 페이지가 총 페이지 수보다 큽니다.")
        }
        val trips = if (tripsDtoList.isNotEmpty()) tripsDtoList else emptyList<Map<String, Any>>()
        return mapOf(
            "currentPage" to currentPage,
            "trips" to trips,
            "postsPerPage" to postsPerPage,
            "totalPages" to totalPage,
        )
    }

    /**
     * 게스트 권한, keyword 를 기반으로 게시물 목록을 조회합니다.
     *  - 단, 좋아요 상태는 게스트이기 때문에 false 로 표시됩니다.
     * @param keyword 검색어(제목 또는 내용으로 검색)
     * @param pageable 페이지네이션 정보
     * @return 검색된 게시물 목록
     * */
    fun getSearchTripsByKeywordAsGuest(keyword: String, pageable: Pageable): Map<String, Any> {
        val findTrips = tripRepository.findTripsByKeyword(keyword, pageable)
        val tripsDtoList = findTrips.content.map { fromTripToTripCardDto(it, "") }
        val currentPage = findTrips.number + 1
        val totalPage = findTrips.totalPages
        val postsPerPage = findTrips.size
        if (currentPage > totalPage && findTrips.content.isNotEmpty()) {
            throw IllegalArgumentException("현재 페이지가 총 페이지 수보다 큽니다.")
        }
        val trips = if (tripsDtoList.isNotEmpty()) tripsDtoList else emptyList<Map<String, Any>>()
        return mapOf(
            "currentPage" to currentPage,
            "trips" to trips,
            "postsPerPage" to postsPerPage,
            "totalPages" to totalPage,
        )
    }

    /**
     * 게시물을 수정합니다.
     * @param memeberId 현재 사용자의 멤버 ID
     * @param tripUpdateDto 수정할 게시물 정보
     * @return 수정된 게시물 - TripDto 형태의 데이터
     * */
    fun updateTrip(memberId: Long, tripUpdateDto: TripUpdateDto): TripDto {
        val findTrip = tripRepository.findById(tripUpdateDto.id).orElse(null)
            ?: throw IllegalArgumentException("조회되는 게시물이 없습니다.")
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")

        if (findTrip.userId == userId) {
            findTrip.title = tripUpdateDto.title
            findTrip.content = tripUpdateDto.content
            findTrip.location = tripUpdateDto.country
            if (tripUpdateDto.startedAt != null && tripUpdateDto.endAt != null && tripUpdateDto.startedAt!!.isAfter(tripUpdateDto.endAt)) {
                throw IllegalArgumentException("시작일은 종료일보다 같거나 이전이어야 합니다.")
            }
            findTrip.startedAt = tripUpdateDto.startedAt
            findTrip.endAt = tripUpdateDto.endAt
            findTrip.latitude = tripUpdateDto.latitude ?: findTrip.latitude
            findTrip.longitude = tripUpdateDto.longitude ?: findTrip.longitude

            // HashtagInfo 업데이트 로직
            tripUpdateDto.countryCode?.let {
                findTrip.hashtagInfo = HashtagInfo(
                    countryCode = tripUpdateDto.countryCode,
                    country = tripUpdateDto.country,
                    city = tripUpdateDto.city,
                    municipality = tripUpdateDto.municipality,
                    name = tripUpdateDto.name,
                    displayName = tripUpdateDto.displayName,
                    road = tripUpdateDto.road,
                    address = tripUpdateDto.address,
                    etc = tripUpdateDto.etc ?: emptySet(),
                )
            }

            findTrip.isPublic = tripUpdateDto.isPublic ?: findTrip.isPublic

            // 삭제될 이미지 처리
            tripUpdateDto.deletedImageUrls?.let { deletedUrls ->
                val currentImages = (findTrip.images ?: emptyList()).toMutableList()
                for (url in deletedUrls) {
                    try {
                        imageService.deleteImage(url)
                        currentImages.remove(url) // 이미지 URL을 리스트에서 제거합니다.
                    } catch (e: Exception) {
                        println("이미지 삭제에 실패했습니다. URL: $url, 오류: ${e.message}")
                    }
                }
                findTrip.images = currentImages
            }

            // 새로운 이미지 처리
            tripUpdateDto.images?.let { images ->
                val updatedImages = (findTrip.images ?: emptyList()).toMutableList()
                for (newImage in images) {
                    val newImageUrl = imageService.uploadImage("tripsketch/trip-sketching", newImage)
                    updatedImages.add(newImageUrl)
                }
                findTrip.images = updatedImages
            }

            val updatedTrip = tripRepository.save(findTrip)
            return fromTrip(updatedTrip, userId)
        } else {
            throw IllegalAccessException("수정할 권한이 없습니다.")
        }
    }

    /**
     * softdelete 방식으로 게시물을 삭제합니다.
     *  - 단, 작성한 사용자 본인 또는 관리자만 삭제 가능합니다.
     * @param memeberId 현재 사용자의 멤버 ID
     * @param id 삭제할 게시물 ID
     * */
    fun deleteTripById(memberId: Long, id: String) {
        val findTrip = tripRepository.findById(id).orElse(null)
            ?: throw IllegalArgumentException("삭제할 게시물이 존재하지 않습니다.")
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        val adminIdsStrings = EnvLoader.getProperty("ADMIN_IDS")?.split(",") ?: listOf()
        val adminIds = adminIdsStrings.mapNotNull { it.toLongOrNull() }

        if (memberId in adminIds || findTrip.userId == userId) {
            commentService.deleteAllCommentsAdminByTripId(id)
            val images = findTrip.images
            if (images != null) {
                val currentImages = images.toMutableList()
                for (url in images) {
                    try {
                        imageService.deleteImage(url)
                        currentImages.remove(url)
                    } catch (e: Exception) {
                        println("이미지 삭제에 실패했습니다. URL: $url, 오류: ${e.message}")
                    }
                }
                findTrip.images = currentImages
            } else {
                throw IllegalAccessException("삭제할 권한이 없습니다.")
            }
            findTrip.isHidden = true
            findTrip.deletedAt = LocalDateTime.now()
            tripRepository.save(findTrip)
        } else {
            throw IllegalAccessException("삭제할 권한이 없습니다.")
        }
    }

    /**
     * Trip 데이터를 TripDto 형식으로 변환합니다.
     * @param trip 변환할 Trip 도메인
     * @param currentUserId 현재 사용자 ID
     * @return 변환된 TripDto 데이터
     * */
    fun fromTrip(trip: Trip, currentUserId: String): TripDto {
        val tripUser = userService.findUserById(trip.userId) ?: throw IllegalArgumentException("해당 사용자가 존재하지 않습니다.")
        val isLiked: Boolean = if (currentUserId != "") {
            val currentUser =
                userService.findUserById(currentUserId) ?: throw IllegalArgumentException("해당 사용자가 존재하지 않습니다.")
            trip.tripLikes.contains(currentUser.id)
        } else {
            false
        }
        val hashtags = mutableSetOf<String>()
        trip.hashtagInfo?.let { hashtagInfo ->
            with(hashtagInfo) {
                val nonEmptyFields = listOf(country, city, municipality, name, road, address)
                hashtags.addAll(nonEmptyFields.filterNotNull().filter { it.isNotBlank() && it != "undefined" })
                etc?.let {
                    hashtags.addAll(it)
                }
            }
        }
        return TripDto(
            id = trip.id,
            nickname = tripUser.nickname,
            title = trip.title,
            content = trip.content,
            likes = trip.likes,
            views = trip.views,
            location = trip.location,
            startedAt = trip.startedAt,
            endAt = trip.endAt,
            isPublic = trip.isPublic ?: true,
            isHidden = trip.isHidden,
            latitude = trip.latitude,
            longitude = trip.longitude,
            hashtag = hashtags,
            createdAt = trip.createdAt,
            updatedAt = trip.updatedAt,
            deletedAt = trip.deletedAt,
            isLiked = isLiked,
            images = trip.images,
        )
    }

    fun fromTripToUpdate(trip: Trip, currentUserId: String): TripUpdateResponseDto {
        val user = userService.findUserById(trip.userId) ?: throw IllegalArgumentException("해당 유저가 존재하지 않습니다.")

        val isLiked: Boolean = if (currentUserId != "") {
            val currentUser =
                userService.findUserById(currentUserId) ?: throw IllegalArgumentException("해당 유저가 존재하지 않습니다.")
            trip.tripLikes.contains(currentUser.id)
        } else {
            false
        }

        return TripUpdateResponseDto(
            id = trip.id,
            nickname = user.nickname,
            title = trip.title,
            content = trip.content,
            likes = trip.likes,
            views = trip.views,
            location = trip.location,
            startedAt = trip.startedAt,
            endAt = trip.endAt,
            latitude = trip.latitude,
            longitude = trip.longitude,
            hashtagInfo = trip.hashtagInfo,
            isPublic = trip.isPublic ?: true,
            isHidden = trip.isHidden,
            createdAt = trip.createdAt,
            updatedAt = trip.updatedAt,
            deletedAt = trip.deletedAt,
            tripLikes = trip.tripLikes,
            isLiked = isLiked,
            images = trip.images,
        )
    }

    fun fromTripAndComments(trip: Trip, comments: List<CommentDto>, currentUserId: String): TripAndCommentResponseDto {
        return TripAndCommentResponseDto(
            tripAndCommentPairDataByTripId = Pair(
                fromTrip(trip, currentUserId),
                comments,
            ),
        )
    }

    /**
     * Trip 을 TripCardDto 형식으로 변환합니다.
     * @param trip 변환할 Trip 도메인
     * @param currentUserId 현재 사용자 ID
     * @return 변환된 TripCardDto 데이터
     * */
    fun fromTripToTripCardDto(trip: Trip, currentUserId: String): TripCardDto {
        val tripUser = userService.findUserById(trip.userId) ?: throw IllegalArgumentException("해당 사용자가 존재하지 않습니다.")
        val profileImageUrl = tripUser.profileImageUrl
        val comments = commentRepository.countCommentsByTripId(trip.id!!)
        val hashtags = trip.hashtagInfo
        val countryCode = hashtags?.countryCode ?: ""
        val country = hashtags?.country ?: ""
        val isLiked: Boolean = if (currentUserId != "") {
            val currentUser = userService.findUserById(currentUserId) ?: throw IllegalArgumentException("해당 사용자가 존재하지 않습니다.")
            trip.tripLikes.contains(currentUser.id)
        } else {
            false
        }
        return TripCardDto(
            id = trip.id,
            nickname = tripUser.nickname,
            profileImageUrl = profileImageUrl,
            title = trip.title,
            likes = trip.likes,
            views = trip.views,
            comments = comments,
            countryCode = countryCode,
            country = country,
            createdAt = trip.createdAt,
            image = trip.images?.firstOrNull(),
            isLiked = isLiked,
        )
    }
}

fun paginateTrips(trips: Set<TripCardDto>, page: Int, pageSize: Int): Map<String, Any> {
    val tripList = trips.toList()
    val totalTrips = tripList.size
    val startIndex = (page - 1) * pageSize
    val endIndex = if (startIndex + pageSize < totalTrips) {
        startIndex + pageSize
    } else {
        totalTrips
    }

    val paginatedTrips = tripList.slice(startIndex until endIndex)
    val totalPage = if (tripList.isEmpty()) 0 else (totalTrips + pageSize - 1) / pageSize

    return mapOf(
        "trips" to paginatedTrips,
        "currentPage" to page,
        "totalPage" to totalPage,
        "postsPerPage" to pageSize,
    )
}
