package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.Trip
import kr.kro.tripsketch.dto.TripCountryFrequencyDto
import kr.kro.tripsketch.dto.TripCreateDto
import kr.kro.tripsketch.dto.TripDto
import kr.kro.tripsketch.dto.TripUpdateDto
import kr.kro.tripsketch.dto.TripUpdateResponseDto
import kr.kro.tripsketch.repositories.FollowRepository
import kr.kro.tripsketch.repositories.TripRepository
import kr.kro.tripsketch.repositories.UserRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDateTime

// import org.springframework.data.domain.Sort

@Service
class TripService(
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository,
    private val followRepository: FollowRepository,
    private val userService: UserService,
    private val notificationService: NotificationService,
) {
    fun createTrip(memberId: Long, tripCreateDto: TripCreateDto): TripDto {
        val user = userService.findUserByMemberId(memberId) ?: throw IllegalArgumentException("해당 이메일의 사용자 존재하지 않습니다.")
        val newTrip = Trip(
            userId = user.id!!,
            title = tripCreateDto.title,
            content = tripCreateDto.content,
            location = tripCreateDto.location,
            startedAt = LocalDateTime.now(),
            endAt = LocalDateTime.now(),
            latitude = tripCreateDto.latitude,
            longitude = tripCreateDto.longitude,
            hashtagInfo = tripCreateDto.hashtagInfo,
            isPublic = tripCreateDto.isPublic,
            images = tripCreateDto.images,
        )
        val createdTrip = tripRepository.save(newTrip)

        // 나를 팔로우하는 사람들에게 알람 보내기 기능
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        val findUser = userRepository.findById(userId)
        if (findUser.isPresent) {
            val user = findUser.get()
            val userNickname = user.nickname ?: "Unknown user"
            val userProfileUrl = user.profileImageUrl ?: ""
        }

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
                userId,
            )
        return findTrips.map { fromTrip(it, userId, false) }.toSet()
    }

    fun getAllMyTripsByUser(memberId: Long): Set<TripDto> {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        val findTrips = tripRepository.findByIsHiddenIsFalseAndUserId(userId)
        return findTrips.map { fromTrip(it, userId, false) }.toSet()

//        val findTrips: Set<Trip> = if (email.isNotEmpty()) {
//            // to-do : 매개변수 이메일과 findTrips 에서의 email 과 동일하다면 비공개 포함하여 보여줌 - findByIsHiddenIsFalse
//            tripRepository.findTripByUserId(email) +
//                    tripRepository.findByIsPublicIsTrueAndIsHiddenIsFalse(email)
//        } else {
//            // to-do : 같지않다면 공개 게시물만 보여줌 - findByIsPublicIsTrueAndIsHiddenIsFalse
//            tripRepository.findByIsPublicIsTrueAndIsHiddenIsFalse(email)
//        }
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

    // to-do : (메인페이지-모바일(회원))내가 구독한 여행자의 스케치(following 한 nickname 에 대한 카드 1개씩 조회 - 카드 갯수는 설정할 수 있게끔 하자)
    // 구독 유무를 변수로 받아줄 수 있으면 그렇게 하자.
    fun getListFollowingByUser(memberId: Long): List<TripDto> {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        // 내가 팔로잉하는 사람들
//        val followingUsers = followRepository.findByFollower(userId).map { it.following }.toSet()
        val followingUsers = followRepository.findByFollower(userId)
            .filter { it.following != userId && it.following.isNotEmpty() }
            .map { it.following }

        // 만약 filteredFollowingEmails가 빈 집합이라면 빈 리스트 반환
        if (followingUsers.isEmpty()) {
            return emptyList()
        }

        return tripRepository.findTripsByUserId(followingUsers).map { trip -> fromTrip(trip, userId, false) }
    }

    // to-do : 쿼리스트링으로 sort 에 대한 조건을 받을 수 있을까? -> ex. 최신순(1)/오래된순(-1), 인기순(2)
    // to-do : 구독유무에 따라 위로 올리는 순? 아니면 구독한 내용만 따로 받아올 수도 있겠다.
    fun getSearchTripsByKeyword(memberId: Long, keyword: String, sorting: Int): List<TripDto> {
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        // 검색 기준: 제목, 글 내용, 위치(나라, 도시이름 등) (cf. 닉네임은 getTripByNickname)
        val sort: Sort = when (sorting) {
            1 -> Sort.by(Sort.Order.desc("createdAt")) // 최신순으로 정렬
            -1 -> Sort.by(Sort.Order.asc("createdAt")) // 오래된순으로 정렬
            else -> Sort.unsorted() // 정렬하지 않음
        }
        val findTrips = tripRepository.findTripsByKeyword(keyword, sort)
        val tripDtoList = mutableListOf<TripDto>()
        findTrips.forEach { trip ->
            tripDtoList.add(fromTrip(trip, userId, false))
        }
        println(tripDtoList)
        return tripDtoList
    }

    fun updateTrip(memberId: Long, tripUpdateDto: TripUpdateDto): TripDto {
        val findTrip = tripRepository.findById(tripUpdateDto.id).orElseThrow {
            EntityNotFoundException("수정할 게시글이 존재하지 않습니다.")
        }
        val userId = userRepository.findByMemberId(memberId)?.id
            ?: throw IllegalArgumentException("조회되는 사용자가 없습니다.")
        if (findTrip.userId == userId) {
            findTrip.apply {
                title = tripUpdateDto.title
                content = tripUpdateDto.content
                location = tripUpdateDto.location
                startedAt = tripUpdateDto.startedAt ?: startedAt
                endAt = tripUpdateDto.endAt ?: endAt
                latitude = tripUpdateDto.latitude
                longitude = tripUpdateDto.longitude
                hashtagInfo = tripUpdateDto.hashtagInfo
                isPublic = tripUpdateDto.isPublic
                updatedAt = LocalDateTime.now()
                images = tripUpdateDto.images
            }
            val updatedTrip = tripRepository.save(findTrip)
            return fromTrip(updatedTrip, "", false)
        } else {
            throw IllegalAccessException("수정할 권한이 없습니다.")
        }
    }

    fun deleteTripById(memberId: Long, id: String) {
        val findTrip = tripRepository.findById(id).orElseThrow {
            EntityNotFoundException("삭제할 게시글이 존재하지 않습니다.")
        }
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
                images = trip.images,
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
                images = trip.images,
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
            images = trip.images,
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
        "postsPerPage" to pageSize,
    )
}
