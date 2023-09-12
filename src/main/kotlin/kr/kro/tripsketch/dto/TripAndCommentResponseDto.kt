package kr.kro.tripsketch.dto

data class TripAndCommentResponseDto(
    val tripAndCommentPairDataByTripId: Pair<TripDto, List<CommentDto>>
)
