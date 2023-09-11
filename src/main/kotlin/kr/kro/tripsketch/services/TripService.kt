package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.Trip
import kr.kro.tripsketch.dto.*
import kr.kro.tripsketch.repositories.FollowRepository
import kr.kro.tripsketch.repositories.TripRepository
import kr.kro.tripsketch.repositories.UserRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
class TripService(
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository,
    private val followRepository: FollowRepository,
    private val userService: UserService,
    private val notificationService: NotificationService,
    private val imageService: ImageService
) {
    fun createTrip(memberId: Long, tripCreateDto: TripCreateDto, images: List<MultipartFile>?): TripDto {
        val user = userService.findUserByMemberId(memberId) ?: throw IllegalArgumentException("해당 이메일의 사용자 존재하지 않습니다.")
        val uploadedImageUrls = images?.map { imageService.uploadImage("tripsketch/trip-sketching", it) }
        val newTrip = Trip(
            userId = user.id!!,
            title = tripCreateDto.title,
            content = tripCreateDto.content,
            location = tripCreateDto.location,
            startedAt = tripCreateDto.startedAt,
            endAt = tripCreateDto.endAt,
            latitude = tripCreateDto.latitude,
            longitude = tripCreateDto.longitude,
            hashtagInfo = tripCreateDto.hashtagInfo,
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
            followingProfileUrl
        )
        return fromTrip(createdTrip, userId, false)
    }

    fun getAllTrips(memberId: Long): Set<TripDto> {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        val findTrips = tripRepository.findAll()
        return findTrips.map { fromTrip(it, userId, false) }.toSet()
    }

    fun getAllTripsByUser(memberId: Long): Set<TripDto> {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        val findTrips =
            tripRepository.findByIsHiddenIsFalseAndUserId(userId) + tripRepository.findByIsPublicIsTrueAndIsHiddenIsFalseAndUserIdNot(
                userId
            )
        return findTrips.map { fromTrip(it, userId, false) }.toSet()
    }

    fun getAllMyTripsByUser(memberId: Long): Set<TripDto> {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        val findTrips = tripRepository.findByIsHiddenIsFalseAndUserId(userId)
        return findTrips.map { fromTrip(it, userId, false) }.toSet()
    }

    fun getAllTripsByGuest(): Set<TripDto> {
        val findTrips = tripRepository.findByIsPublicIsTrueAndIsHiddenIsFalse()
        return findTrips.map { fromTrip(it, "", false) }.toSet()
    }

    fun getTripByNickname(nickname: String): Set<TripDto> {
        val user = userService.findUserByNickname(nickname) ?: throw IllegalArgumentException("해당 유저를 조회 할 수 없습니다.")
        val findTrips = user.id?.let { tripRepository.findTripByUserIdAndIsHiddenIsFalse(it) }
            ?: throw IllegalArgumentException("작성한 게시글이 존재하지 않습니다.")
        return findTrips.map { fromTrip(it, "", false) }.toSet()
    }

    fun getTripCategoryByNickname(nickname: String): Pair<Map<String, Int>, Set<TripDto>> {
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

    fun getTripsInCountry(nickname: String, country: String): Set<TripDto> {
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
    fun Set<Trip>.categorizeTripsByCountry(): Pair<Map<String, Int>, Set<TripDto>> {
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
        val categorizedTrips = sortedTrips.map { fromTrip(it, "", false) }.toSet()

        return sortedCountryFrequencyMap to categorizedTrips
    }

    /**
     * 특정 나라의 여행 목록을 반환합니다.
     *
     * @param targetCountry 검색할 나라의 이름
     * @return 해당 나라의 여행 목록
     */
    fun Set<Trip>.getTripsInCountry(targetCountry: String): Set<TripDto> {
        // 지정된 나라와 일치하는 여행만 필터링하고 TripDto로 변환하여 반환
        val filteredTrips = this.filter { trip ->
            trip.hashtagInfo?.country == targetCountry
        }

        // 최신순으로 정렬
        val sortedTrips = filteredTrips.sortedByDescending { it.createdAt }

        return sortedTrips.map { fromTrip(it, "", false) }.toSet()
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

    fun getTripByMemberIdAndId(memberId: Long, id: String): TripDto? {
        val findTrip = tripRepository.findByIdAndIsHiddenIsFalse(id)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")

        // 조회수
        if (!findTrip.tripViews.contains(userId) && findTrip.userId != userId) {
            findTrip.tripViews.add(userId)
            findTrip.views += 1
            tripRepository.save(findTrip)
        }
        return fromTrip(findTrip, userId, false)
    }

    fun getTripByMemberIdAndIdToUpdate(memberId: Long, id: String): TripUpdateResponseDto? {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        val findTrip = tripRepository.findByIdAndIsHiddenIsFalse(id)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        return fromTripToUpdate(findTrip, userId, false)
    }

    fun getTripById(id: String): TripDto? {
        val findTrip = tripRepository.findByIdAndIsHiddenIsFalse(id)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        return fromTrip(findTrip, "", false)
    }

    fun getTripIsPublicById(id: String): TripDto? {
        val findTrip = tripRepository.findByIdAndIsHiddenIsFalse(id)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        if (findTrip.isPublic == false) {
            throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        }
        return fromTrip(findTrip, "", false)
    }

    fun getListFollowingTrips(memberId: Long): List<TripDto> {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        val followingUsers = followRepository.findByFollower(userId)
            .filter { it.following != userId && it.following.isNotEmpty() }
            .map { it.following }
        if (followingUsers.isEmpty()) {
            return emptyList()
        }
//        return tripRepository.findTripsByUserId(followingUsers).map { trip -> fromTrip(trip, userId, false) }
        val latestTrips = mutableListOf<TripDto>()

        for (followingUser in followingUsers) {
            val latestTrip = tripRepository.findLatestTripByUserId(followingUser)
            latestTrip?.let {
                latestTrips.add(fromTrip(it, userId, false))
            }
        }
        return latestTrips
    }

    fun getSearchTripsByKeyword(memberId: Long, keyword: String, sorting: Int): List<TripDto> {
        try {
            val userId = userRepository.findByMemberId(memberId)?.id
                ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
            val sort: Sort = when (sorting) {
                1 -> Sort.by(Sort.Order.desc("createdAt"))
                -1 -> Sort.by(Sort.Order.asc("createdAt"))
                2 -> Sort.by(Sort.Order.desc("likes"))
                else -> Sort.unsorted()
            }

            val findTrips = tripRepository.findTripsByKeyword(keyword, sort)
            if (findTrips.isEmpty()) {
                throw IllegalArgumentException("조회되는 게시물이 없습니다.")
            }

            val tripDtoList = mutableListOf<TripDto>()
            findTrips.forEach { trip ->
                tripDtoList.add(fromTrip(trip, userId, false))
            }
            println(tripDtoList)
            return tripDtoList
        } catch (ex: Exception) {
            println("에러 발생: ${ex.message}")
            throw ex
        }
    }

    fun updateTrip(memberId: Long, tripUpdateDto: TripUpdateDto, images: List<MultipartFile>?): TripDto {
        val findTrip = tripRepository.findById(tripUpdateDto.id).orElse(null)
            ?: throw IllegalArgumentException("조회되는 게시물이 없습니다.")
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        if (findTrip.userId == userId) {
            tripUpdateDto.title?.let {
                if (it != findTrip.title) {
                    findTrip.title = it
                }
            }
            tripUpdateDto.content?.let {
                if (it != findTrip.content) {
                    findTrip.content = it
                }
            }
            tripUpdateDto.location?.let {
                if (it != findTrip.location) {
                    findTrip.location = it
                }
            }
            tripUpdateDto.startedAt?.let {
                if (it != findTrip.startedAt) {
                    findTrip.startedAt = it
                }
            }
            tripUpdateDto.endAt?.let {
                if (it != findTrip.endAt) {
                    findTrip.endAt = it
                }
            }
            tripUpdateDto.latitude?.let {
                if (it != findTrip.latitude) {
                    findTrip.latitude = it
                }
            }
            tripUpdateDto.longitude?.let {
                if (it != findTrip.longitude) {
                    findTrip.longitude = it
                }
            }
            tripUpdateDto.hashtagInfo?.let {
                if (it != findTrip.hashtagInfo) {
                    findTrip.hashtagInfo = it
                }
            }
            tripUpdateDto.isPublic?.let {
                if (it != findTrip.isPublic) {
                    findTrip.isPublic = it
                }
            }
            images?.let { newImages ->
                findTrip.images?.forEach { oldImageUrl ->
                    try {
                        imageService.deleteImage(oldImageUrl)
                    } catch (e: Exception) {
                        println("이미지 삭제에 실패하였습니다. URL: $oldImageUrl, 오류: {$e.message}")
                    }
                }
                val uploadedImageUrls = newImages.map { imageService.uploadImage("tripsketch/trip-sketching", it) }
                findTrip.images = uploadedImageUrls
            }
            val updatedTrip = tripRepository.save(findTrip)
            return fromTrip(updatedTrip, userId, false)
        } else {
            throw IllegalAccessException("수정할 권한이 없습니다.")
        }
    }

    fun deleteTripById(memberId: Long, id: String) {
        val findTrip = tripRepository.findById(id).orElse(null)
            ?: throw IllegalArgumentException("삭제할 게시물이 존재하지 않습니다.")
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        if (findTrip.userId == userId) { // 'findTrip.userId == user.id' 조건은 항상 false입니다 ?
            findTrip.isHidden = true
            findTrip.deletedAt = LocalDateTime.now()
            tripRepository.save(findTrip)
        } else {
            throw IllegalAccessException("삭제할 권한이 없습니다.")
        }
    }

    fun fromTrip(trip: Trip, currentUserId: String, includeUserId: Boolean = true): TripDto {
        val tripUser = userService.findUserById(trip.userId) ?: throw IllegalArgumentException("해당 유저가 존재하지 않습니다.")
        val isLiked: Boolean = if (currentUserId != "") {
            val currentUser =
                userService.findUserById(currentUserId) ?: throw IllegalArgumentException("해당 유저가 존재하지 않습니다.")
            trip.tripLikes.contains(currentUser.id)
        } else {
            false
        }
        val hashtags = mutableSetOf<String>()
        trip.hashtagInfo?.let { hashtagInfo ->
            with(hashtagInfo) {
                val nonEmptyFields = listOf(countryCode, country, city, municipality, name, displayName, road, address)
                hashtags.addAll(nonEmptyFields.filterNotNull().filter { it.isNotBlank() })
                etc?.let {
                    hashtags.addAll(it)
                }
            }
        }
        return if (includeUserId) {
            TripDto(
                id = trip.id,
                nickname = tripUser.nickname,
                title = trip.title,
                content = trip.content,
                likes = trip.likes,
                views = trip.views,
                location = trip.location,
                startedAt = trip.startedAt,
                endAt = trip.endAt,
                latitude = trip.latitude,
                longitude = trip.longitude,
                hashtag = hashtags,
                isPublic = trip.isPublic ?: true,
                isHidden = trip.isHidden,
                createdAt = trip.createdAt,
                updatedAt = trip.updatedAt,
                deletedAt = trip.deletedAt,
                tripLikes = trip.tripLikes,
                isLiked = isLiked,
                images = trip.images
            )
        } else {
            TripDto(
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
                latitude = trip.latitude,
                longitude = trip.longitude,
                hashtag = hashtags,
                isHidden = trip.isHidden,
                createdAt = trip.createdAt,
                updatedAt = trip.updatedAt,
                deletedAt = trip.deletedAt,
                tripLikes = trip.tripLikes,
                isLiked = isLiked,
                images = trip.images
            )
        }
    }

    fun fromTripToUpdate(trip: Trip, currentUserId: String, includeEmail: Boolean = false): TripUpdateResponseDto {
        val user = userService.findUserById(trip.userId) ?: throw IllegalArgumentException("해당 유저가 존재하지 않습니다.1")

        val isLiked: Boolean = if (currentUserId != "") {
            val currentUser =
                userService.findUserById(currentUserId) ?: throw IllegalArgumentException("해당 유저가 존재하지 않습니다.7")
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
            images = trip.images
        )
    }
}

fun paginateTrips(trips: Set<TripDto>, page: Int, pageSize: Int): Map<String, Any> {
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
        "posts" to paginatedTrips,
        "currentPage" to page,
        "totalPage" to totalPage,
        "postsPerPage" to pageSize
    )
}