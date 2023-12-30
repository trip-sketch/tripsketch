package kr.kro.tripsketch.trip.dtos

/**
 * 여행 국가별 게시물 빈도수 정보를 담은 DTO 클래스입니다.
 *
 * @property categoryName 국가 카테고리 이름
 * @property postsLength 게시물 빈도수
 * @author BYEONGUK KO
 */
data class TripCountryFrequencyDto(val categoryName: String, val postsLength: Int)
