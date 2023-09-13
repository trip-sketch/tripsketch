package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.HashtagInfo
import kr.kro.tripsketch.domain.Trip
import kr.kro.tripsketch.dto.*
import kr.kro.tripsketch.exceptions.DataNotFoundException
import kr.kro.tripsketch.exceptions.ForbiddenException
import kr.kro.tripsketch.repositories.CommentRepository
import kr.kro.tripsketch.repositories.FollowRepository
import kr.kro.tripsketch.repositories.TripRepository
import kr.kro.tripsketch.repositories.UserRepository
import kr.kro.tripsketch.utils.EnvLoader
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
class TripService(
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val followRepository: FollowRepository,
    private val userService: UserService,
    private val notificationService: NotificationService,
    private val imageService: ImageService,
    private val commentService: CommentService
) {
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
            etc = tripCreateDto.etc
        )

        val newTrip = Trip(
            userId = user.id!!,
            title = tripCreateDto.title,
            content = tripCreateDto.content,
            location = tripCreateDto.location,
            startedAt = startedAt,
            endAt = endAt,
            latitude = tripCreateDto.latitude,
            longitude = tripCreateDto.longitude,
            hashtagInfo = hashtagInfo,
            isPublic = tripCreateDto.isPublic,
            images = uploadedImageUrls,

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
        return fromTrip(createdTrip, userId)
    }

    fun getAllTrips(memberId: Long, pageable: Pageable): Map<String, Any> {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        val findTrips = tripRepository.findAll(pageable)
        val tripsDtoList = findTrips.content.map { fromTrip(it, userId) }
        val currentPage = findTrips.number + 1
        val totalPage = findTrips.totalPages
        val postsPerPage = findTrips.size
        if (currentPage <= totalPage && findTrips.isEmpty) {
            throw DataNotFoundException("작성한 게시글이 존재하지 않습니다.")
        } else if (currentPage > totalPage) {
            throw IllegalArgumentException("현재 페이지가 총 페이지 수보다 큽니다.")
        }
        return mapOf(
            "currentPage" to currentPage,
            "trips" to tripsDtoList,
            "postsPerPage" to postsPerPage,
            "totalPages" to totalPage
        )
    }

    fun getAllTripsByUser(memberId: Long): Set<TripDto> {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        val findTrips =
            tripRepository.findByIsHiddenIsFalseAndUserId(userId) +
                    tripRepository.findByIsPublicIsTrueAndIsHiddenIsFalseAndUserIdNot(userId)
        return findTrips.map { fromTrip(it, userId) }.toSet()
    }

    fun getAllMyTripsByUser(memberId: Long, pageable: Pageable): Map<String, Any> {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        val findTrips = tripRepository.findByIsHiddenIsFalseAndUserId(userId, pageable)
        val tripsDtoList = findTrips.content.map { fromTrip(it, userId) }
        val currentPage = findTrips.number + 1
        val totalPage = findTrips.totalPages
        val postsPerPage = findTrips.size
        if (currentPage <= totalPage && findTrips.isEmpty) {
            throw DataNotFoundException("작성한 게시글이 존재하지 않습니다.")
        } else if (currentPage > totalPage) {
            throw IllegalArgumentException("현재 페이지가 총 페이지 수보다 큽니다.")
        }
        return mapOf(
            "currentPage" to currentPage,
            "trips" to tripsDtoList,
            "postsPerPage" to postsPerPage,
            "totalPages" to totalPage
        )
    }

    fun getAllTripsByGuest(): Set<TripDto> {
        val findTrips = tripRepository.findByIsPublicIsTrueAndIsHiddenIsFalse()
        return findTrips.map { fromTrip(it, "") }.toSet()
    }

    fun getTripsByNickname(nickname: String): Set<TripDto> {
        val user = userService.findUserByNickname(nickname) ?: throw IllegalArgumentException("해당 유저를 조회 할 수 없습니다.")
        val findTrips = user.id?.let { tripRepository.findTripByUserIdAndIsHiddenIsFalse(it) }
            ?: throw IllegalArgumentException("작성한 게시글이 존재하지 않습니다.")
        return findTrips.map { fromTrip(it, "") }.toSet()
    }

    fun getTripAndCommentsIsPublicByTripIdGuest(id: String): TripAndCommentResponseDto {
        val findTrip = tripRepository.findByIdAndIsPublicIsTrueAndIsHiddenIsFalse(id)?: throw IllegalArgumentException("게시글이 존재하지 않습니다.")
        val commentDtoList = commentService.getCommentsGuestByTripId(id)
        return fromTripAndComments(findTrip, commentDtoList, "")
    }

    fun getTripAndCommentsIsLikedByTripIdMember(id: String, memberId:Long): TripAndCommentResponseDto {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        val findTrip = tripRepository.findByIdAndIsHiddenIsFalse(id)?: throw IllegalArgumentException("게시글이 존재하지 않습니다.")
        if(findTrip.isPublic==false&&findTrip.userId!=userId){
            throw ForbiddenException("해당 게시물에 접근 권한이 없습니다.")
        }
        val commentDtoList = commentService.getIsLikedByMemberIdForTrip(memberId,id)
        return fromTripAndComments(findTrip, commentDtoList, userId)
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
        val categorizedTrips = sortedTrips.map { fromTrip(it, "") }.toSet()

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

        return sortedTrips.map { fromTrip(it, "") }.toSet()
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
        return fromTrip(findTrip, userId)
    }

    fun getTripByMemberIdAndIdToUpdate(memberId: Long, id: String): TripUpdateResponseDto? {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        val findTrip = tripRepository.findByIdAndIsHiddenIsFalse(id)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        return fromTripToUpdate(findTrip, userId)
    }

    fun getTripById(id: String): TripDto? {
        val findTrip = tripRepository.findByIdAndIsHiddenIsFalse(id)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        return fromTrip(findTrip, "")
    }

    fun getTripIsPublicById(id: String): TripDto? {
        val findTrip = tripRepository.findByIdAndIsHiddenIsFalse(id)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        if (findTrip.isPublic == false) {
            throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        }
        return fromTrip(findTrip, "")
    }

//    fun getListFollowingTrips(memberId: Long): List<TripCardDto> {
//        val userId = userRepository.findByMemberId(memberId)?.id
//            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
//        val followingUsers = followRepository.findByFollower(userId)
//            .filter { it.following != userId }
//            .map { it.following }
//        if (followingUsers.isEmpty()) {
//            throw DataNotFoundException("구독한 게시물이 없습니다.")
//        }
//        val tripDtoList = mutableListOf<TripCardDto>()
//        followingUsers.forEach { followingUserId ->
//            val findLatestTrip = tripRepository.findFirstByUserIdAndIsHiddenIsFalseOrderByCreatedAtDesc(followingUserId)
//            if (findLatestTrip != null) {
//                tripDtoList.add(fromTripToTripCardDto(findLatestTrip, userId))
//            }
//        }
//        tripDtoList.sortWith(compareBy<TripCardDto> { it.views }.thenByDescending { it.createdAt })
//        return tripDtoList
//    }

    fun getListFollowingTrips(memberId: Long, pageable: Pageable): Map<String, Any> {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        val followingUsers = followRepository.findByFollower(userId)
            .filter { it.following != userId }
            .map { it.following }
        if (followingUsers.isEmpty()) {
            throw DataNotFoundException("구독한 게시물이 없습니다.")
        }
        val tripDtoList = mutableListOf<TripCardDto>()
        followingUsers.forEach { followingUserId ->
            val findLatestTrip = tripRepository.findFirstByUserIdAndIsHiddenIsFalseOrderByCreatedAtDesc(followingUserId)
            if (findLatestTrip != null) {
                tripDtoList.add(fromTripToTripCardDto(findLatestTrip, userId))
            }
        }
        tripDtoList.sortWith(compareBy<TripCardDto> { it.views }.thenByDescending { it.createdAt })

        // 페이징 처리
        val startIndex = pageable.pageNumber * pageable.pageSize
        val endIndex = Math.min(startIndex + pageable.pageSize, tripDtoList.size)
        val currentPage = pageable.pageNumber + 1
        val totalPage = Math.ceil(tripDtoList.size.toDouble() / pageable.pageSize).toInt()
        val postsPerPage = pageable.pageSize

        if (currentPage <= totalPage && tripDtoList.isEmpty()) {
            throw DataNotFoundException("작성한 게시글이 존재하지 않습니다.")
        } else if (currentPage > totalPage) {
            throw IllegalArgumentException("현재 페이지가 총 페이지 수보다 큽니다.")
        }

        // 페이지 정보와 자른 결과를 반환합니다.
        val pagedTripDtoList = tripDtoList.subList(startIndex, endIndex)

        return mapOf(
            "currentPage" to currentPage,
            "trips" to pagedTripDtoList,
            "postsPerPage" to postsPerPage,
            "totalPages" to totalPage
        )
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
                tripDtoList.add(fromTrip(trip, userId))
            }
            return tripDtoList
        } catch (ex: Exception) {
            println("에러 발생: ${ex.message}")
            throw ex
        }
    }

    fun updateTrip(memberId: Long, tripUpdateDto: TripUpdateDto): TripDto {
        val findTrip = tripRepository.findById(tripUpdateDto.id).orElse(null)
            ?: throw IllegalArgumentException("조회되는 게시물이 없습니다.")
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")

        if (findTrip.userId == userId) {
            findTrip.title = tripUpdateDto.title
            findTrip.content = tripUpdateDto.content
            findTrip.location = tripUpdateDto.location ?: findTrip.location
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
                    etc = tripUpdateDto.etc ?: emptySet()
                )
            }

            findTrip.isPublic = tripUpdateDto.isPublic ?: findTrip.isPublic

            // 이미지 처리 로직
            tripUpdateDto.images?.let { images ->
                // 기존 이미지와 새로운 이미지 리스트를 비교해서 삭제된 이미지를 찾습니다.
                val removedImages = (findTrip.images ?: emptyList()).filter { it !in images }
                for (oldImageUrl in removedImages) {
                    try {
                        imageService.deleteImage(oldImageUrl)
                    } catch (e: Exception) {
                        println("이미지 삭제에 실패했습니다. URL: $oldImageUrl, 오류: ${e.message}")
                    }
                }

                // 새로 추가된 이미지를 스토리지에 업로드합니다.
                val updatedImages = (findTrip.images ?: emptyList()).toMutableList()
                for (newImage in images) {
                    if (newImage is MultipartFile && !updatedImages.contains(newImage.originalFilename)) {
                        val newImageUrl = imageService.uploadImage("tripsketch/trip-sketching", newImage)
                        updatedImages.add(newImageUrl)
                    }
                }
                findTrip.images = updatedImages
            }


            val updatedTrip = tripRepository.save(findTrip)
            return fromTrip(updatedTrip, userId) // 'fromTrip' 함수는 DTO를 생성하는 함수라고 가정합니다.
        } else {
            throw IllegalAccessException("수정할 권한이 없습니다.")
        }
    }

    fun deleteTripById(memberId: Long, id: String) {
        val findTrip = tripRepository.findById(id).orElse(null)
            ?: throw IllegalArgumentException("삭제할 게시물이 존재하지 않습니다.")
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        val adminIdsStrings = EnvLoader.getProperty("ADMIN_IDS")?.split(",") ?: listOf()
        val adminIds = adminIdsStrings.mapNotNull { it.toLongOrNull() }
        if (memberId in adminIds) {
            commentService.deleteAllCommentsAdminByTripId(id)
            findTrip.isHidden = true
            findTrip.deletedAt = LocalDateTime.now()
            tripRepository.save(findTrip)
            return
        }
        if (findTrip.userId == userId) { // 'findTrip.userId == user.id' 조건은 항상 false입니다 ?
            findTrip.isHidden = true
            findTrip.deletedAt = LocalDateTime.now()
            tripRepository.save(findTrip)
        } else {
            throw IllegalAccessException("삭제할 권한이 없습니다.")
        }
    }

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
                val nonEmptyFields = listOf( country, city, municipality, name, road, address)
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
            images = trip.images
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
            images = trip.images
        )
    }

    fun fromTripAndComments(trip: Trip, comments: List<CommentDto>, currentUserId: String): TripAndCommentResponseDto {
        return TripAndCommentResponseDto(
            tripAndCommentPairDataByTripId = Pair(
                fromTrip(trip, currentUserId),
                comments)
        )
    }

    fun fromTripToTripCardDto(trip: Trip, currentUserId: String): TripCardDto {
        val tripUser = userService.findUserById(trip.userId) ?: throw IllegalArgumentException("해당 사용자가 존재하지 않습니다.")
        val profileImageUrl = tripUser.profileImageUrl
        val comments = commentRepository.countCommentsByTripId(trip.id!!) ?: 0
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
            isLiked = isLiked
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