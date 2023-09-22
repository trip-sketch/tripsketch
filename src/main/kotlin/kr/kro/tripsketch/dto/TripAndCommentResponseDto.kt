package kr.kro.tripsketch.dto

/**
 * 여행 정보와 댓글들의 목록을 담은 응답 DTO 클래스입니다.
 *
 * @property tripAndCommentPairDataByTripId 여행 정보와 댓글 목록의 쌍
 * @author BYEONGUK KO
 */
data class TripAndCommentResponseDto(
    val tripAndCommentPairDataByTripId: Pair<TripDto, List<CommentDto>>?,
)
