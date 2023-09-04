package kr.kro.tripsketch.services

import kr.kro.tripsketch.domain.Trip
import kr.kro.tripsketch.dto.TripCreateDto
import kr.kro.tripsketch.dto.TripDto
import kr.kro.tripsketch.dto.TripUpdateDto
import kr.kro.tripsketch.dto.TripUpdateResponseDto
import kr.kro.tripsketch.repositories.FollowRepository
import kr.kro.tripsketch.repositories.TripRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TripService(
    private val tripRepository: TripRepository,
    private val followRepository: FollowRepository,
    private val userService: UserService,
    private val notificationService: NotificationService,
) {
    fun createTrip(email: String, tripCreateDto: TripCreateDto): TripDto {
        val newTrip = Trip(
            email = email,
            title = tripCreateDto.title,
            content = tripCreateDto.content,
            location = tripCreateDto.location,
            startedAt = LocalDateTime.now(),
            endAt = LocalDateTime.now(),
            latitude = tripCreateDto.latitude,
            longitude = tripCreateDto.longitude,
            hashtagInfo = tripCreateDto.hashtagInfo,
            isPublic = tripCreateDto.isPublic,
            images = tripCreateDto.images
        )
        val createdTrip = tripRepository.save(newTrip)
        // 나를 팔로우하는 사람들에게 알람 보내기 기능
        val follower = followRepository.findByFollowing(email)
        val filteredFollower = follower.filter { it.follower != email }
        val followerEmails = filteredFollower.map { it.follower }
        val followingNickname = userService.findUserByEmail(email)?.nickname ?: "Unknown user"
        val followingProfileUrl = userService.findUserByEmail(email)?.profileImageUrl ?: ""
        notificationService.sendPushNotification(
            followerEmails,
            "새로운 여행의 시작, 트립스케치",
            "$followingNickname 님이 새로운 글을 작성하였습니다.",
            null,
            null,
            createdTrip.id,
            followingNickname,
            followingProfileUrl
        )
        return fromTrip(createdTrip, email, false)
    }

    fun getAllTrips(email: String): Set<TripDto> {
        val findTrips = tripRepository.findAll()
        return findTrips.map { fromTrip(it, email, false) }.toSet()
    }

    fun getAllTripsByUser(email: String): Set<TripDto> {
        val findTrips = tripRepository.findByIsHiddenIsFalseAndEmail(email) + tripRepository.findByIsPublicIsTrueAndIsHiddenIsFalseAndEmailNot(email)
        return findTrips.map { fromTrip(it, email, false) }.toSet()
    }

    fun getAllMyTripsByUser(email: String): Set<TripDto> {
        val findTrips = tripRepository.findByIsHiddenIsFalseAndEmail(email)
        return findTrips.map { fromTrip(it, email, false) }.toSet()
    }

    fun getAllTripsByGuest(): Set<TripDto> {
        val findTrips = tripRepository.findByIsPublicIsTrueAndIsHiddenIsFalse()
            ?: throw IllegalArgumentException("작성된 게시글이 존재하지 않습니다.")
        return findTrips.map { fromTrip(it, "", false) }.toSet()
    }

//    fun getAllFollowingTripsByUser(email: String): Set<TripDto> {
//        val findTrips = tripRepository.findByIsPublicIsTrueAndIsHiddenIsFalseAndEmailNot(email)
//        return findTrips.map { fromTrip(it, email, false) }.toSet()
//
//    }

    fun getTripByNickname(nickname: String): Set<TripDto> {
        val user = userService.findUserByNickname(nickname)
        val findTrips = tripRepository.findTripByEmailAndIsHiddenIsFalse(user!!.email)
            ?: throw IllegalArgumentException("작성한 게시글이 존재하지 않습니다.")
        return findTrips.map { fromTrip(it, "", false) }.toSet()
    }

    fun getTripCategoryByNickname(nickname: String): Pair<Map<String, Int>, Set<TripDto>> {
        val user = userService.findUserByNickname(nickname)
        val findTrips = tripRepository.findTripByEmailAndIsHiddenIsFalse(user!!.email)
        return findTrips.categorizeTripsByCountry()
    }

    fun getTripCategoryByNickname(nickname: String, page: Int, pageSize: Int): Map<String, Any> {
        val user = userService.findUserByNickname(nickname)
        val findTrips = tripRepository.findTripByEmailAndIsHiddenIsFalse(user!!.email)
        // 전체 여행 목록을 카테고리화
        val categorizedTrips = findTrips.categorizeTripsByCountry()
        // 페이지네이션 적용
        return paginateTrips(categorizedTrips.second, page, pageSize)
    }


    fun getTripsInCountry(nickname: String, country: String): Set<TripDto> {
        val user = userService.findUserByNickname(nickname)
        val findTrips = tripRepository.findTripByEmailAndIsHiddenIsFalse(user!!.email)
        val getTripsInCountry = findTrips.getTripsInCountry(country)
        return findTrips.getTripsInCountry(country)
    }

    fun getTripsInCountry(nickname: String, country: String, page: Int, pageSize: Int): Map<String, Any> {
        val user = userService.findUserByNickname(nickname)
        val findTrips = tripRepository.findTripByEmailAndIsHiddenIsFalse(user!!.email)
        val tripsInCountry = findTrips.getTripsInCountry(country)

        // 페이지네이션 적용
        return paginateTrips(tripsInCountry, page, pageSize)
    }


    fun getCountryFrequencies(nickname: String): Map<String, Int> {
        val user = userService.findUserByNickname(nickname)
        val findTrips = tripRepository.findTripByEmailAndIsHiddenIsFalse(user!!.email)
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

        return filteredTrips.map { fromTrip(it, "", false) }.toSet()
    }


    /**
     * 나라 기준으로 여행 횟수를 많은 순으로 정렬하여 반환합니다.
     *
     * @return 나라별 여행 횟수를 내림차순으로 정렬한 맵
     */
    fun Set<Trip>.sortTripsByCountryFrequency(): Map<String, Int> {
        // 나라별 여행 횟수를 계산하기 위한 맵
        val countryFrequencyMap = mutableMapOf<String, Int>()

        // 각 여행을 반복하면서 나라별 횟수를 업데이트
        for (trip in this) {
            trip.hashtagInfo?.country?.let { country ->
                countryFrequencyMap[country] = countryFrequencyMap.getOrDefault(country, 0) + 1
            }
        }

        // 나라별 횟수를 내림차순으로 정렬한 맵을 반환
        return countryFrequencyMap.entries
            .sortedByDescending { it.value }
            .associateBy({ it.key }, { it.value })
    }

    fun getTripByEmailAndId(email: String, id: String): TripDto? {
        val findTrip = tripRepository.findByIdAndIsHiddenIsFalse(id)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")

        // 조회수
        if (!findTrip.tripViews.contains(email) && findTrip.email != email) {
            findTrip.tripViews.add(email)
            findTrip.views += 1
            tripRepository.save(findTrip)
        }
        return fromTrip(findTrip, email, false)
    }

    fun getTripByEmailAndIdToUpdate(email: String, id: String): TripUpdateResponseDto? {
        val findTrip = tripRepository.findByIdAndIsHiddenIsFalse(id)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        return fromTripToUpdate(findTrip, email, false)
    }

    fun getTripById(id: String): TripDto? {
        val findTrip = tripRepository.findByIdAndIsHiddenIsFalse(id)
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        return fromTrip(findTrip, "", false)
    }

    // to-do : (메인페이지-모바일(회원))내가 구독한 여행자의 스케치(following 한 nickname 에 대한 카드 1개씩 조회 - 카드 갯수는 설정할 수 있게끔 하자)
    // 구독 유무를 변수로 받아줄 수 있으면 그렇게 하자.
    fun getListFollowingByUser(email: String): List<TripDto> {
        // 내가 팔로잉하는 사람들
        val following = followRepository.findByFollower(email).toSet()
        println(following)
        val filteredFollowing = following.filter { it.following != email && it.following.isNotEmpty() }
        println(filteredFollowing)
        val filteredFollowingEmails = filteredFollowing.map { it.following }.toSet()
        println(filteredFollowingEmails)

//        val findTrips = tripRepository.findByIsPublicIsTrueAndIsHiddenIsFalseAndEmail(filterdFollowingEmails)
//        println(findTrips)

        // 만약 filteredFollowingEmails가 빈 집합이라면 빈 리스트 반환
        if (filteredFollowingEmails.isEmpty()) {
            return emptyList()
        }

        val findTrips = tripRepository.findListFollowingByUser(filteredFollowingEmails)
        println(findTrips)

        val findTripDtos = findTrips.map { trip -> fromTrip(trip, email, false) }
        println(findTripDtos)
        return findTripDtos
    }

    // to-do : 쿼리스트링으로 sort 에 대한 조건을 받을 수 있을까? -> ex. 최신순(1)/오래된순(-1), 인기순(2)
    // to-do : 구독유무에 따라 위로 올리는 순? 아니면 구독한 내용만 따로 받아올 수도 있겠다.
    fun getSearchTripsByKeyword(email: String, keyword: String, sorting: Int): List<TripDto> {
        // 검색 기준: 제목, 글 내용, 위치(나라, 도시이름 등) (cf. 닉네임은 getTripByNickname)
        val sort: Sort = when (sorting) {
            1 -> Sort.by(Sort.Order.desc("createdAt")) // 최신순으로 정렬
            -1 -> Sort.by(Sort.Order.asc("createdAt")) // 오래된순으로 정렬
            else -> Sort.unsorted() // 정렬하지 않음
        }
        val findTrips = tripRepository.findTripsByKeyword(keyword, sort)
        if (findTrips != null) {
            val tripDtoList = mutableListOf<TripDto>()
            findTrips.forEach { trip ->
                tripDtoList.add(fromTrip(trip, email, false))
            }
            return tripDtoList
        } else {
            throw IllegalAccessException("조회되는 게시물이 없습니다.")
        }
    }


    fun updateTrip(email: String, tripUpdateDto: TripUpdateDto): TripDto {
        val findTrip = tripRepository.findById(tripUpdateDto.id).orElseThrow {
            EntityNotFoundException("수정할 게시글이 존재하지 않습니다.")
        }
        if (findTrip.email == email) {
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

    fun deleteTripById(email: String, id: String): Unit {
        val findTrip = tripRepository.findById(id).orElseThrow {
            EntityNotFoundException("삭제할 게시글이 존재하지 않습니다.")
        }
        if (findTrip.email == email) {
            findTrip.isHidden = true
            findTrip.deletedAt = LocalDateTime.now()
            tripRepository.save(findTrip)
        } else {
            throw IllegalAccessException("삭제할 권한이 없습니다.")
        }
    }


    fun fromTrip(trip: Trip, currentUserEmail: String, includeEmail: Boolean = true): TripDto {
        val user = userService.findUserByEmail(trip.email)
        val isLiked = trip.tripLikes.contains(currentUserEmail)
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
        return if (includeEmail) {
            TripDto(
                id = trip.id,
                email = trip.email,
                nickname = user!!.nickname,
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
                email = null,
                nickname = user!!.nickname,
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

    fun fromTripToUpdate(trip: Trip, currentUserEmail: String, includeEmail: Boolean = false): TripUpdateResponseDto {
        val user = userService.findUserByEmail(trip.email)
        val isLiked = trip.tripLikes.contains(currentUserEmail)
        return TripUpdateResponseDto(
            id = trip.id,
            email = trip.email,
            nickname = user!!.nickname,
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